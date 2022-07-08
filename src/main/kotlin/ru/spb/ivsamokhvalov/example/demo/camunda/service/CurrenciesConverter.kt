package ru.spb.ivsamokhvalov.example.demo.camunda.service

import java.math.BigDecimal
import java.math.RoundingMode
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

interface CurrenciesConverterService {

    fun convert(from: CurrencyPrice, to: CurrencyCode): CurrencyPrice
    fun convertToDefault(from: CurrencyPrice): CurrencyPrice
}


@Service
class CurrenciesConverterServiceImpl: CurrenciesConverterService {

    @Value("\${currency.defaultCode}")
    private lateinit var defaultCurrency: CurrencyCode

    private val currencies: Map<CurrencyCode, CurrencyPrice> = mapOf(
        CurrencyCode.USD to CurrencyPrice(BigDecimal("61.2664"), CurrencyCode.USD),
        CurrencyCode.EUR to CurrencyPrice(BigDecimal("62.0499"), CurrencyCode.EUR),
        CurrencyCode.RUB to CurrencyPrice(BigDecimal.ONE, CurrencyCode.RUB),
    )


    override fun convert(from: CurrencyPrice, to: CurrencyCode): CurrencyPrice {
        val fromCurrency = currencies[from.currency]!!
        val inRubPrice = from.price * fromCurrency.price
        val toCurrency = currencies[to]!!
        val toPrice = inRubPrice.divide(toCurrency.price, 4, RoundingMode.CEILING)
        return CurrencyPrice(toPrice, to)
    }

    override fun convertToDefault(from: CurrencyPrice) = convert(from, defaultCurrency)
}
//https://www.cbr.ru/scripts/XML_daily.asp
//<Valute ID="R01060">
//<NumCode>051</NumCode>
//<CharCode>AMD</CharCode>
//<Nominal>100</Nominal>
//<Name>Армянских драмов</Name>
//<Value>14,9186</Value>
//</Valute>