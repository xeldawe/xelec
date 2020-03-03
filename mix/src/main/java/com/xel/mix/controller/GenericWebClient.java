package com.xel.mix.controller;

import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;

public class GenericWebClient<T> {

	private Class<T> clazz;
	private WebClient client;
	private String endpoint;

	public GenericWebClient(String endpoint, Class<T> clazz) {
		super();
		this.endpoint = endpoint;
		this.clazz = clazz;
	}

	public void createWebClient(String host, int port) {
		client = WebClient.create("http://" + host + ":" + port);
	}

	public WebClient getClient() {
		return client;
	}

	public Flux<T> getFlux(){
		 Flux<T> flux = client.get()
				  .uri(endpoint)
				  .retrieve()
				  .bodyToFlux(clazz);
				return flux;
	 }
	
}
