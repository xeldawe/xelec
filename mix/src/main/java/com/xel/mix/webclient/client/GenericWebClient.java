package com.xel.mix.webclient.client;

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

	public GenericWebClient() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void createWebClient(String host, int port) {
		if (port != 443) {
			client = WebClient.builder().baseUrl("http://" + host + ":" + port).build();
		} else {
			client = WebClient.builder().baseUrl("https://" + host + ":" + port).build();
		}
	}

	public void createWebClient(String url) {
		client = WebClient.create(url);
	}

	public WebClient getClient() {
		return client;
	}

	public Flux<T> getFlux() throws WebClientException {
		return client.get().uri(endpoint).retrieve().bodyToFlux(clazz).doOnError(t -> new WebClientException("Unknow error"));
	}

}
