package com.xel.mix.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.xel.mix.elastic.controller.ElasticController;
import com.xel.mix.elastic.repository.ElasticRepository;
import com.xel.mix.webclient.service.RequestService;
import com.xel.mix.webclient.service.RequestService.RequestMode;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping
public class DefaultController {

	@Autowired
	ElasticRepository gr;
	
	@Autowired
	ElasticController ec;
	
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
	
	@PostMapping("/default/{index}")
	public Flux<ServerResponse> post(@PathVariable String index, @RequestBody Object obj) {
		ServerResponse sr = (ServerResponse) rs.getData(rs.requestUrl("http://localhost:8097/elastic/"+index, RequestMode.POST, obj));
		return Flux.just(sr);
	}

	@GetMapping("/default")
	public Flux<ServerResponse> get(@RequestParam String jsonQuery) throws JsonMappingException, JsonProcessingException, UnsupportedEncodingException {
		ServerResponse sr = (ServerResponse) rs.getData(rs.requestUrl("http://localhost:8097/elastic?jsonQuery="+URLEncoder.encode(jsonQuery, StandardCharsets.UTF_8.toString()))).getMessage();
		return Flux.just(sr);
	}

}