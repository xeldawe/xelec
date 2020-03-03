package com.xel.mix.cassandra.controller;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xel.mix.cassandra.entity.Test;
import com.xel.mix.cassandra.service.TestService;

@RestController
@RequestMapping("test")
public class TestController {

	@Autowired
	TestService testService;

	@PostConstruct
	public void TestControllerPost() {
	}

	@GetMapping("/list/{val}/{val2}")
	public List<Test> getAllTests(@PathVariable String val, @PathVariable String val2) {
		List<Test> tests = testService.getAllTests(val, val2);
		return tests;
	}
}
