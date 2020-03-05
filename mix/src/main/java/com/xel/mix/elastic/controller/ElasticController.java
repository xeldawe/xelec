package com.xel.mix.elastic.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xel.mix.controller.ServerResponse;
import com.xel.mix.elastic.repository.ElasticRepository;
import com.xel.mix.webclient.service.RequestService;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping
public class ElasticController {

	@Autowired
	ElasticRepository gr;

	@PostConstruct
	private void init() {
	}

	@GetMapping("elastic")
	public Flux<ServerResponse> findWithFilters() {
		// TMP
		Map<String, String> map = new LinkedHashMap<>();
		map.put("manual", "manual");
		//
		return gr.findWithFilters(map, null);
	}
	
	@PostMapping("elastic/{index}")
	public Flux<ServerResponse> post(@PathVariable String index, @RequestBody Object obj) {
		return gr.insert(index, obj);
	}

	@DeleteMapping("elastic")
	public Flux<ServerResponse> delete(@RequestParam String[] values) {
		// TMP
		
		Map<String, String> map = new LinkedHashMap<>();
		map.put("test1", "alma");
		
		Map<String, Object> configMap = new LinkedHashMap<>();
		configMap.put("indices", values);
		//
		return gr.delete(map, configMap);
	}

}
