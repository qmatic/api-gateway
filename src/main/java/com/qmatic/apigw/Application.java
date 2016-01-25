package com.qmatic.apigw;

import io.undertow.Undertow.Builder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.undertow.UndertowBuilderCustomizer;
import org.springframework.boot.context.embedded.undertow.UndertowEmbeddedServletContainerFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableZuulProxy
@EnableCaching
public class Application {

	// TODO: verify if these settings are relevant
	//	@Value("${undertow.max-connections}")
	//	private int maxConnections; (used to be set to 100)

	//	@Value("${undertow.backlog}")
	//	private int backlog; (used to be set to 20)

	@Bean
	UndertowEmbeddedServletContainerFactory embeddedServletContainerFactory() {
		UndertowEmbeddedServletContainerFactory factory = new UndertowEmbeddedServletContainerFactory();
		
		factory.addBuilderCustomizers(new UndertowBuilderCustomizer() {
			
			@Override
			public void customize(Builder builder) {
				builder.setIoThreads(Runtime.getRuntime().availableProcessors() * 2);
					   //.setServerOption(Options.CONNECTION_HIGH_WATER, maxConnections)
					   //.setServerOption(Options.BACKLOG, backlog);
			}
		});

		return factory;
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
