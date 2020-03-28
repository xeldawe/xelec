package com.xel.mix.cassandra.controller;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xel.mix.cassandra.key.TestKey;
import com.xel.mix.cassandra.model.Test;
import com.xel.mix.cassandra.service.TestService;
import com.xel.mix.controller.DefaultController;
import com.xel.mix.controller.ServerResponse;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping("test")
public class TestController {

	@Autowired
	TestService testService;
	
	@Autowired
	DefaultController dc;
	
	@PostConstruct
	public void TestControllerPost() {
	}

	@GetMapping("/list/{val}/{val2}")
	public List<Test> getAllTests(@PathVariable String val, @PathVariable String val2) {
		List<Test> tests = testService.getAllTests(val, val2);
		return tests;
	}
	
	@PostMapping("/{index}/{name}")
	public Flux<ServerResponse> post(@RequestBody Test body, @PathVariable String index, @PathVariable String name) {
		body.setKey(new TestKey(name, UUID.randomUUID(), ZonedDateTime.now().toLocalDateTime()));
		ServerResponse sr = new ServerResponse();
		sr.setMessage(testService.save(body));
		dc.post("test", sr.getMessage());
		return Flux.just(sr);
	}
}
