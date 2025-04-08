package com.polarbookshop.catalog_service.infrastructure.persistence;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.beans.factory.annotation.Autowired;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.lang.NonNull;

@Configuration
@EnableR2dbcAuditing
public class DataConfig extends AbstractR2dbcConfiguration {

    @Autowired
    private ConnectionFactory connectionFactory;

    @Override
    @NonNull
    public ConnectionFactory connectionFactory() {
        return this.connectionFactory;
    }
}
