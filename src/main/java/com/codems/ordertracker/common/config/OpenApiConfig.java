package com.codems.ordertracker.common.config;

import com.codems.ordertracker.common.constants.ApplicationConstants;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.ArrayList;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
		info = @Info(
				title = "OrderTracker API",
				version = ApplicationConstants.DEFAULT_API_VERSION,
				description = "E-commerce order management and webhook REST API.",
				contact = @Contact(name = "DevLab")
		),
		security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
		name = "bearerAuth",
		type = SecuritySchemeType.HTTP,
		scheme = "bearer",
		bearerFormat = "JWT",
		in = SecuritySchemeIn.HEADER,
		description = "Provide a JWT access token obtained from /api/auth/login"
)
public class OpenApiConfig {

	@Bean
	GroupedOpenApi orderTrackerApiGroup() {
		return GroupedOpenApi.builder()
				.group("order-tracker")
				.packagesToScan(ApplicationConstants.APPLICATION_PACKAGE)
				.pathsToMatch(ApplicationConstants.API_PREFIX + "/**")
				.build();
	}

	@Bean
	OpenApiCustomizer apiVersionHeaderCustomizer() {
		return openApi -> openApi.getPaths().values().forEach(pathItem ->
				pathItem.readOperations().forEach(this::addApiVersionHeader)
		);
	}

	private void addApiVersionHeader(Operation operation) {
		if (operation.getParameters() == null) {
			operation.setParameters(new ArrayList<>());
		}

		boolean alreadyExists = operation.getParameters().stream()
				.anyMatch(parameter -> ApplicationConstants.API_VERSION_HEADER.equals(parameter.getName()));
		if (alreadyExists) {
			return;
		}

		operation.addParametersItem(new Parameter()
				.in("header")
				.name(ApplicationConstants.API_VERSION_HEADER)
				.required(false)
				.description("API version")
				.schema(new StringSchema()
						._default(ApplicationConstants.DEFAULT_API_VERSION)
						.example(ApplicationConstants.DEFAULT_API_VERSION)));
	}
}
