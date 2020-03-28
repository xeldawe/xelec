package com.xel.mix.cassandra.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xel.mix.cassandra.model.Test;
import com.xel.mix.cassandra.repository.CassandraBaseRepository;
import com.xel.mix.cassandra.repository.CassandraRepositoryImpl;

import reactor.core.publisher.Flux;

@Service
public class TestService {

	@Autowired
	CassandraRepositoryImpl testRepository;
	
	@Autowired
	CassandraBaseRepository basicRepository;

//	public void initializeTests(List<Test> tests) {
//		basicRepository.insert(tests).subscribe();
//	        Flux<Test> savedTests = basicRepository.saveAll(tests);
//	        savedTests.subscribe();	       
//	    }
	
	public Test save(Test t) {
		return testRepository.save(t);
	}

	public List<Test> getAllTests(String val, String val2) {
		Map<String, String> map = new HashMap<>();
		map.put("address", val);
		map.put("email", val2);
		List<Test> result = testRepository.findAllBy(map);
		return result;
	}

}
