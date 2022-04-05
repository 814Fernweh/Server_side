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
//        registry.addResourceHandler("/upload/**").addResourceLocations("file:D:\\year4\\bi\\pic/recipes/upload/");
        //静态资源释放
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/","classpath:/static/**","classpath:/templates/");
    }

//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(new LoginHandlerIntercepetor())
//                .addPathPatterns("/**")//添加拦截请求
//                .excludePathPatterns("/login.html","/","/user/*","/css/**","/js/**","/img/**","/layui/**","/layui/*");//排除拦截请求
//    }
}
