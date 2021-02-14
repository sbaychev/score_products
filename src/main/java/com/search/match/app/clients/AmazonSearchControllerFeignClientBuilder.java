package com.search.match.app.clients;

import feign.Feign;
import feign.Logger.Level;
import feign.Retryer.Default;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;
import feign.slf4j.Slf4jLogger;
import lombok.Getter;

@Getter
public class AmazonSearchControllerFeignClientBuilder {

    //    https://completion.amazon.com/search?search-alias=aps&client=amazon-search-ui&mkt=1&q=canon

    private AmazonSearchClient amazonSearchClient = createClient(AmazonSearchClient.class,
        "https://completion.amazon.com/search");

    private static <T> T createClient(Class<T> type, String uri) {

        return Feign.builder()
            .client(new OkHttpClient())
            .encoder(new JacksonEncoder())
            .decoder(new JacksonDecoder())
            .logger(new Slf4jLogger(type))
            .logLevel(Level.FULL)
            .retryer(new Default())
            .target(type, uri);
    }
}
