package com.example.project.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MyMvcConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("initial.html");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //Static resource release
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/","classpath:/static/**","classpath:/templates/");
    }

}
