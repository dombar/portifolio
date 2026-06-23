package br.com.portifolio.config;

import io.netty.channel.ChannelOption;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Configuration
@EnableConfigurationProperties(ExternalMembersProperties.class)
public class WebClientConfig {

    @Bean
    public WebClient memberExternalWebClient(ExternalMembersProperties props) {
        ConnectionProvider provider = ConnectionProvider.builder("member-external-pool")
                .maxConnections(props.getMaxConnections())
                .maxIdleTime(props.getMaxIdleTime())
                .build();

        HttpClient httpClient = HttpClient.create(provider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) props.getConnectTimeout().toMillis())
                .responseTimeout(props.getReadTimeout());

        return WebClient.builder()
                .baseUrl(props.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
