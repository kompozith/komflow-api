package com.kompozith.komflow.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration du client HTTP dédié aux appels vers l'API WhatsApp Cloud (Meta).
 * Un bean nommé {@code whatsappRestTemplate} est fourni avec les timeouts
 * définis dans {@link WhatsAppProperties}.
 */
@Configuration
public class WhatsAppConfig {

    @Bean("whatsappRestTemplate")
    public RestTemplate whatsappRestTemplate(WhatsAppProperties props) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(props.getConnectTimeoutMs());
        factory.setReadTimeout(props.getReadTimeoutMs());
        return new RestTemplate(factory);
    }
}
