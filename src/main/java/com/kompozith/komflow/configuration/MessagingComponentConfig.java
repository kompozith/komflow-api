package com.kompozith.komflow.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.kompozith.komflow.features.messaging")
public class MessagingComponentConfig {
}
