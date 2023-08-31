package it.evoluta.fabrickaccount.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "cash")
public class CashProperties {

    private String baseUrl;

    private String schema;

    private String key;

    private String accountId;
}
