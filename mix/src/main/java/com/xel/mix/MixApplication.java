package com.xel.mix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.xel.mix.controller.GenericWebClient;
import com.xel.mix.controller.ServerResponse;

@SpringBootApplication
public class MixApplication {

	public static void main(String[] args) {
		SpringApplication.run(MixApplication.class, args);
		GenericWebClient<ServerResponse> userWebClient = new GenericWebClient<>("/user", ServerResponse.class);
		userWebClient.createWebClient("localhost", 8097);
		userWebClient.getFlux().subscribe(res->System.out.println(res));
	}

}
