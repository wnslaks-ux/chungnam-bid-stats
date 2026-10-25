package com.chungnam.bidstats.config;

import java.util.concurrent.TimeUnit;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        registry.jsp("/WEB-INF/jsp/", ".jsp");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**", "/js/**", "/images/**")
                .addResourceLocations("classpath:/static/css/", "classpath:/static/js/", "classpath:/static/images/")
                .setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic());
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /** Windows에서 JSP 컴파일 시 기본 인코딩(MS949) 대신 UTF-8 사용 */
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatUtf8Customizer() {
        return factory -> factory.addContextCustomizers(
                context -> context.addParameter("javaEncoding", "UTF-8"));
    }
}
