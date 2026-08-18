package com.nomesh.projects.lovable_clone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LovableCloneApplication {

	public static void main(String[] args) {
		SpringApplication.run(LovableCloneApplication.class, args);
	}

}
