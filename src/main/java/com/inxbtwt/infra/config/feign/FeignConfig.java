package com.inxbtwt.infra.config.feign;

import com.inxbtwt.infra.client.search.SearchApiClient;
import com.inxbtwt.infra.client.auth.AuthApiClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableFeignClients(clients = {SearchApiClient.class, AuthApiClient.class})
public class FeignConfig {
}
