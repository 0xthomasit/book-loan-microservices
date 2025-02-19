package com.ion.employee_service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                title = "Employee Api Specification - ION Mobility",
                description = "Api documentation for Employee Service",
                version = "1.0",
                contact = @Contact(
                        name = "Thomas Ng",
                        email = "tuan.ngo.se@gmail.com",
                        url = "https://ion.vercel.app"
                ),
                license = @License(
                        name = "MIT License",
                        url = "https://ion.vercel.app/licenses"
                ),
                termsOfService = "https://ion.vercel.app/terms"
        ),
        servers = {
                @Server(
                        description = "Local ENV",
                        url = "http://localhost:9002"
                ),
                @Server(
                        description = "Dev ENV",
                        url = "https://employee-service.dev.com"
                ),
                @Server(
                        description = "Prod ENV",
                        url = "https://employee-service.prod.com"
                ),
        }
)
public class OpenApiConfig {
}