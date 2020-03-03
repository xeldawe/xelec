package com.xel.mix.cassandra.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xel.mix.cassandra.entity.Test;
import com.xel.mix.cassandra.repository.BasicRepository;
import com.xel.mix.cassandra.repository.TestRepositoryImpl;

import reactor.core.publisher.Flux;

@Service
public class TestService {

	@Autowired
	TestRepositoryImpl testRepository;
	
	@Autowired
	BasicRepository basicRepository;

	public void initializeTests(List<Test> tests) {
		basicRepository.insert(tests).subscribe();
	        Flux<Test> savedTests = basicRepository.saveAll(tests);
	        savedTests.subscribe();	       
	    }

	public List<Test> getAllTests(String val, String val2) {
		Map<String, String> map = new HashMap<>();
		map.put("address", val);
		map.put("email", val2);
		List<Test> result = testRepository.findAllBy(map);
		return result;
	}

}
