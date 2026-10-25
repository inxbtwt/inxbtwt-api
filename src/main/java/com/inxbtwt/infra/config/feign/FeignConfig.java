package com.inxbtwt.infra.config.feign;

import com.inxbtwt.infra.client.search.SearchApiClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableFeignClients(clients = SearchApiClient.class)
public class FeignConfig {
}
