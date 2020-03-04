package com.xel.mix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.xel.mix.webclient.service.RequestService;

@SpringBootApplication
public class MixApplication {

	public static void main(String[] args) {
		SpringApplication.run(MixApplication.class, args);
		RequestService rs = new RequestService();
		System.out.println(rs.requestUrl("http://localhost:8097/user"));
	}

}
