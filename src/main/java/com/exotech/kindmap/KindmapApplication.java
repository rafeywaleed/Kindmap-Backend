package com.exotech.kindmap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KindmapApplication {

	public static void main(String[] args) {
		SpringApplication.run(KindmapApplication.class, args);
	}

}
