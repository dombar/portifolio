package br.com.portifolio.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "portfolio.external-members")
public class ExternalMembersProperties {

    private String baseUrl;
    private Duration connectTimeout = Duration.ofSeconds(5);
    private Duration readTimeout = Duration.ofSeconds(10);
    private int maxConnections = 50;
    private Duration maxIdleTime = Duration.ofSeconds(30);
}
