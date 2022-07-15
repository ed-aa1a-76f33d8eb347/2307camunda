package ru.spb.ivsamokhvalov.example.demo.camunda.service

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.math.BigDecimal
import org.junit.jupiter.api.Test

internal class CBRAdapterImplTest {

    private val mapper = XmlMapper(JacksonXmlModule().apply {
        setDefaultUseWrapper(false)
    })
        .registerKotlinModule()
        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    @Test
    fun readXml() {
        val result = mapper.readValue<RootValCus>(source_xml, RootValCus::class.java)

        println(result)
        println("End")
        result.valutes.onEach { println(it.convertedNominal) }

    }


    @Test
    fun testCompareBigDecimal() {
        val a = CurrencyPrice(BigDecimal("2"), CurrencyCode.RUB.name)
        val b = CurrencyPrice(BigDecimal("2.0"), CurrencyCode.RUB.name)
        println(a == b)
        println(a.price.compareTo(b.price))
    }

    companion object {
        val source_xml = """<?xml version="1.0" encoding="windows-1251"?>
            <ValCurs Date="12.07.2022" name="Foreign Currency Market">
                <Valute ID="R01010">
                    <NumCode>036</NumCode>
                    <CharCode>AUD</CharCode>
                    <Nominal>1</Nominal>
                    <Name>Àâñòðàëèéñêèé äîëëàð</Name>
                    <Value>41,7668</Value>
                </Valute>
</ValCurs>
        """.trimIndent()
    }
}

