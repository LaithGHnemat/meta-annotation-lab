package com.laithevolution.annotationlab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class MetaAnnotationDeepdiveLabApplication {
	public static void main(String[] args) {
		SpringApplication.run(MetaAnnotationDeepdiveLabApplication.class, args);
	}
}
