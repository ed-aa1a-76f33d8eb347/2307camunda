package ru.spb.ivsamokhvalov.example.demo.camunda.configuations

import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter


@Configuration
class Configurations {

    @Bean
    fun processCorsFilter(): FilterRegistrationBean<*> {
        val source = UrlBasedCorsConfigurationSource()
        val config = CorsConfiguration().also {
            it.allowCredentials = true
            it.allowedOriginPatterns = listOf("*")
            it.addAllowedHeader("*")
            it.addAllowedMethod("*")
        }
        source.registerCorsConfiguration("/**", config)
        val bean = FilterRegistrationBean(CorsFilter(source))
        bean.order = 0
        return bean
    }
}
