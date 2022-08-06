package org.readcat.ReadCat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@SpringBootApplication
public class ReadCatApplication {
	public static void main(String[] args) {
		SpringApplication.run(ReadCatApplication.class, args);
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurerAdapter() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
//				registry.addMapping("/products").allowedOrigins("http://localhost:8081");
				registry.addMapping("/**")
						//放行哪些原始域
//						.allowedOrigins("*")
						.allowedOrigins("http://localhost:8081")
						//是否发送Cookie信息
						.allowCredentials(true)
						//放行哪些原始域(请求方式)
						.allowedMethods("GET", "POST", "PUT", "DELETE")
						//放行哪些原始域(头部信息)
						.allowedHeaders("*")
						//暴露哪些头部信息（因为跨域访问默认不能获取全部头部信息）
						.exposedHeaders("Header1", "Header2");
			}
		};
	}
}
