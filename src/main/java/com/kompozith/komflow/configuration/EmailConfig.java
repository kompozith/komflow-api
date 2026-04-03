package com.kompozith.komflow.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class EmailConfig {

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private int port;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    /** Set spring.mail.debug=true in your profile yml to enable SMTP protocol tracing (dev only). */
    @Value("${spring.mail.debug:false}")
    private boolean debug;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.trust", "*");
        // Connection & I/O timeouts to avoid blocking threads forever
        props.put("mail.smtp.connectiontimeout", "15000");  // 15 s connection timeout
        props.put("mail.smtp.timeout", "15000");             // 15 s socket read timeout
        props.put("mail.smtp.writetimeout", "15000");        // 15 s socket write timeout
        props.put("mail.debug", String.valueOf(debug));

        return mailSender;
    }
}