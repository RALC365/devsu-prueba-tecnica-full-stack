package com.devsu.banco.support;

import com.devsu.banco.config.BancoProperties;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
@EnableConfigurationProperties(BancoProperties.class)
public class WebMvcTestConfig {

    @Bean
    Clock clock() {
        return new RelojControlable(LocalDateTime.of(2022, 2, 10, 10, 0), Fixtures.ZONA);
    }
}
