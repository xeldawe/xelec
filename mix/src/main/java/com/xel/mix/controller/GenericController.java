package com.xel.mix.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xel.mix.elastic.model.User;
import com.xel.mix.elastic.repository.GenericRepository;
import com.xel.mix.webclient.service.RequestService;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping
public class GenericController {

	@Autowired
	GenericRepository<User> gr;
	
	@Autowired
	RequestService rs;
	
	@PostConstruct
	private void init() {
		gr.setClazz(User.class);
	}
	
	@GetMapping("/user")
	public Flux<ServerResponse> findWithFilters() {
		//TMP
		Map<String, String> map = new LinkedHashMap<>();
		map.put("test1", "alma");
		//
		return gr.findWithFilters(map,null);
	}
	
	@GetMapping("/test")
	public Flux<ServerResponse> test() {
		return Flux.just(rs.getData(rs.requestUrl("http://localhost:8097/user")));
	}


}