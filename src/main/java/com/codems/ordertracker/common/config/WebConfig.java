package com.codems.ordertracker.common.config;

import com.codems.ordertracker.common.constants.ApplicationConstants;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(ApplicationConstants.API_PREFIX,
                controllerType -> controllerType.getPackageName().startsWith(ApplicationConstants.APPLICATION_PACKAGE));
    }

    @Override
    public void configureApiVersioning(ApiVersionConfigurer configurer) {
        configurer
                .useRequestHeader(ApplicationConstants.API_VERSION_HEADER)
                .setDefaultVersion(ApplicationConstants.DEFAULT_API_VERSION)
                .addSupportedVersions(ApplicationConstants.DEFAULT_API_VERSION);
    }
}
