package ru.spb.ivsamokhvalov.example.demo.camunda.service

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.math.BigDecimal
import java.math.RoundingMode
import mu.KLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service

interface CurrenciesConverterService {

    fun convert(from: CurrencyPrice, to: CurrencyCode): CurrencyPrice
    fun convertToDefault(from: CurrencyPrice): CurrencyPrice
}


@Service
class CurrenciesConverterServiceImpl(
    private val cbrAdapter: CBRAdapter,
) : CurrenciesConverterService {

    @Value("\${currency.defaultCode}")
    private lateinit var defaultCurrency: CurrencyCode

    override fun convert(from: CurrencyPrice, to: CurrencyCode): CurrencyPrice {
        return try {
            val currencies = cbrAdapter.getAllValutes()
            val fromCurrency = currencies[from.currency]!!
            val inRubPrice = from.price * fromCurrency.price
            if (to == CurrencyCode.RUB) return CurrencyPrice(inRubPrice, CurrencyCode.RUB)
            val toCurrency = currencies[to]!!
            val toPrice = inRubPrice.divide(toCurrency.price, 4, RoundingMode.CEILING)
            CurrencyPrice(toPrice, to)
        } catch (e: Exception) {
            logger.error(e) { "Exception on convert! from:$from, to: $to" }
            throw e
        }
    }

    override fun convertToDefault(from: CurrencyPrice) = convert(from, defaultCurrency)

    private companion object : KLogging()
}

interface CBRAdapter {

    @Cacheable("valutes")
    fun getAllValutes(): Map<CurrencyCode, CurrencyPrice>
}

@Service
class CBRAdapterImpl : CBRAdapter {

    @Value("\${currency.mock}")
    private var useMock: Boolean = true

    private val mapper = XmlMapper(JacksonXmlModule().apply {
        setDefaultUseWrapper(false)
    })
        .registerKotlinModule()
        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)


    override fun getAllValutes(): Map<CurrencyCode, CurrencyPrice> {
        val stream = when (useMock) {
            true -> getMockValues()
            false -> getRealValues()
        }
        val parsedValue = mapper.readValue(stream, RootValCus::class.java)
        val result = (parsedValue.valutes
            .filter { it.charCode in CurrencyCode.valuesMap.keys }
            .map {
                val currencyCode = CurrencyCode.valueOf(it.charCode)
                val price = if (BigDecimal.ONE.compareTo(it.convertedNominal) != 0) {
                    it.convertedNominal.divide(it.nominal.toBigDecimal())
                } else {
                    it.convertedNominal
                }
                CurrencyPrice(price, currencyCode)
            } + CurrencyPrice(BigDecimal.ONE, CurrencyCode.RUB)).associateBy { it.currency }
        logger.info { "UseMock: ${useMock}, Load currencies for: ${result.keys.map { it.name }.sorted()}" }
        return result
    }

    @Value("classpath:mock/currencies.xml")
    private lateinit var mockValues: Resource

    private fun getMockValues(): String = String(mockValues.inputStream.readAllBytes())
    private fun getRealValues(): String = khttp.get("http://www.cbr.ru/scripts/XML_daily.asp").text

    private companion object : KLogging()
}

data class RootValCus(
    @field:JsonProperty("Valute")
    val valutes: List<ValuteDTO>,
    @field:JsonProperty("Date") val date: String,
)

data class ValuteDTO(
    @field:JsonProperty("CharCode") val charCode: String,
    @field:JsonProperty("Nominal") val nominal: Int,
    @field:JsonProperty("Value") val value: String,
) {
    val convertedNominal: BigDecimal = BigDecimal(value.replace(',', '.'))
}
