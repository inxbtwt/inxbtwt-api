package com.inxbtwt.infra.config.feign;

import com.inxbtwt.infra.client.search.SearchApiClient;
import com.inxbtwt.infra.client.auth.AuthApiClient;
import com.inxbtwt.infra.client.feed.FeedApiClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableFeignClients(clients = {SearchApiClient.class, AuthApiClient.class, FeedApiClient.class})
public class FeignConfig {
}
