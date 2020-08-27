package com.qmatic.apigw;

import io.undertow.Undertow.Builder;
import io.undertow.UndertowOptions;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.undertow.UndertowBuilderCustomizer;
import org.springframework.boot.context.embedded.undertow.UndertowEmbeddedServletContainerFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableZuulProxy
@EnableCaching
@Import(CorsSpringConfig.class)
public class Application {

	@Value("${server.undertow.accesslog.pattern:}")
	String accessLogPattern;

	@Bean
	UndertowEmbeddedServletContainerFactory embeddedServletContainerFactory() {
		UndertowEmbeddedServletContainerFactory factory = new UndertowEmbeddedServletContainerFactory();
		
		factory.addBuilderCustomizers(new UndertowBuilderCustomizer() {
			
			@Override
			public void customize(Builder builder) {
				builder.setIoThreads(Runtime.getRuntime().availableProcessors() * 2);
			}
		});

		if (logRequestProcessingTiming()) {
			factory.addBuilderCustomizers(builder -> {
				builder.setServerOption(UndertowOptions.RECORD_REQUEST_START_TIME, true);
			});
		}

		return factory;
	}

	private boolean logRequestProcessingTiming() {
		if (StringUtils.isBlank(accessLogPattern)) {
			return false;
		}
		return accessLogPattern.contains("%D") || accessLogPattern.contains("%T");
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
