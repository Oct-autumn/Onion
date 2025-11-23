package com.onion.onionserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * springboot跨域携带cookie
 * @author 杨红杰
 * @date 2020-8-12 16:49
 */
@Configuration
public class CorsConfig {
    
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        //config.addAllowedOrigin("http://localhost:5173");
        //config.addAllowedOrigin("http://8.138.14.149:5173");
        //config.addAllowedOrigin("http://8.138.14.149:8080");
        //config.addAllowedOrigin("http://172.18.36.202:8080");
        
        config.setAllowCredentials(true);
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}
