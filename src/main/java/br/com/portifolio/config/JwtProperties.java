package br.com.portifolio.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "portfolio.jwt")
public class JwtProperties {

    private String secret;
    private Duration expiration = Duration.ofHours(8);
}
