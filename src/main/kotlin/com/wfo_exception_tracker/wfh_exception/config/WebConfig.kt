package com.wfo_exception_tracker.wfh_exception.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:uploads/")
    }
}