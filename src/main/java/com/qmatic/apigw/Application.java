package com.qmatic.apigw;

import io.undertow.Undertow.Builder;
import org.springframework.beans.factory.annotation.Value;
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

	@Value("${orchestra.disableSslCertificateChecks:false}")
	public void setDisableSslCertificateChecks (boolean disableSslCertificateChecks) {
		if (disableSslCertificateChecks) {
			SslCertificateManager.disableSslCertificateChecks();
		}
	}

	@Bean
	UndertowEmbeddedServletContainerFactory embeddedServletContainerFactory() {
		UndertowEmbeddedServletContainerFactory factory = new UndertowEmbeddedServletContainerFactory();
		
		factory.addBuilderCustomizers(new UndertowBuilderCustomizer() {
			
			@Override
			public void customize(Builder builder) {
				builder.setIoThreads(Runtime.getRuntime().availableProcessors() * 2);
			}
		});

		return factory;
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
