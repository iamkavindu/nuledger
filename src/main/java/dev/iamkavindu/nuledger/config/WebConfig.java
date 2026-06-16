package dev.iamkavindu.nuledger.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.RequestPath;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configureApiVersioning(ApiVersionConfigurer configurer) {
        configurer.usePathSegment(1, WebConfig::isVersionedApiPath).setDefaultVersion("1.0");
    }

    private static boolean isVersionedApiPath(RequestPath path) {
        return path.pathWithinApplication().value().startsWith("/api/");
    }
}
