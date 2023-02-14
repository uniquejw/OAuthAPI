package com.oauth.totaloauthapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class TotalOAuthApiApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(TotalOAuthApiApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(TotalOAuthApiApplication.class, args);
    }
}
