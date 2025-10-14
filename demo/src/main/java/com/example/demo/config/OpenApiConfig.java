package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI openAPI() {
		final String schemeName = "bearerAuth";
		return new OpenAPI()
			.info(new Info().title("API").version("v1"))
			.components(new Components()
				.addSecuritySchemes(schemeName, new SecurityScheme()
					.type(SecurityScheme.Type.HTTP)
					.scheme("bearer")
					.bearerFormat("JWT")))
			.addSecurityItem(new SecurityRequirement().addList(schemeName));
	}

	@Bean
	public GroupedOpenApi publicApi() {
		return GroupedOpenApi.builder()
			.group("public")
			.pathsToMatch("/api/**", "/api/payments/**")
			.build();
	}

	@Bean
	public GroupedOpenApi internalApi() {
		return GroupedOpenApi.builder()
			.group("internal")
			.pathsToMatch("/internal/**")
			.build();
	}
}




