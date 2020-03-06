package com.xel.mix.controller;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xel.mix.elastic.repository.ElasticRepository;
import com.xel.mix.webclient.service.RequestService;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping
public class DefaultController {

	@Autowired
	ElasticRepository gr;
	
	@Autowired
	RequestService rs;
	
	@PostConstruct
	private void init() {
	}
	
	@GetMapping("/user")
	public Flux<ServerResponse> findWithFilters() {
		//TMP
		
		//
		return gr.findWithFilters(null,null);
	}
	
	@GetMapping("/test")
	public Flux<ServerResponse> test() {
		return Flux.just(rs.getData(rs.requestUrl("http://localhost:8097/elastic")));
	}

	@DeleteMapping("/test")
	public Flux<ServerResponse> delete(@RequestParam String[] values) {
		return Flux.just(rs.getData(rs.requestUrl("http://localhost:8097/elastic?values="+values.toString())));
	}

}