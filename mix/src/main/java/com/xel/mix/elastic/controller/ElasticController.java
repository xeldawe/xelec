package com.xel.mix.elastic.controller;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.xel.mix.controller.ServerResponse;
import com.xel.mix.elastic.client.ElasticClient;
import com.xel.mix.elastic.repository.ElasticRepository;
import com.xel.mix.webclient.service.RequestService;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping
public class ElasticController {

	private ObjectMapper objectMapper = new ObjectMapper();

	@PostConstruct
	private void init() {
		objectMapper.configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, false);
	}
	
	@Autowired
	ElasticRepository gr;
	
	@SuppressWarnings("unchecked")
	@GetMapping(value = "elastic",produces = "application/json" )
	public Flux<ServerResponse> findWithFilters(@RequestParam String jsonQuery)
			throws JsonMappingException, JsonProcessingException {
		HashMap<String, Object> result = objectMapper.readValue(jsonQuery, HashMap.class);
		return gr.findWithFilters(result, null);
	}

	@PostMapping("elastic/{index}")
	public Flux<ServerResponse> post(@PathVariable String index, @RequestBody Object obj) {
		return gr.insert(index, obj);
	}

	@SuppressWarnings("unchecked")
	@PutMapping("elastic")
	public Flux<ServerResponse> put(@RequestBody String json, @RequestParam String jsonQuery, @RequestParam String[] indices)
			throws JsonMappingException, JsonProcessingException {
		HashMap<String, Object> result = objectMapper.readValue(jsonQuery, HashMap.class);
		Map<String, Object> configMap = new LinkedHashMap<>();
		configMap.put("indices", indices);
		return gr.update(json, result, configMap);
	}

	@SuppressWarnings("unchecked")
	@DeleteMapping("elastic")
	public Flux<ServerResponse> delete(@RequestParam String[] indices, @RequestParam String jsonQuery) throws JsonMappingException, JsonProcessingException {
		// TMP
		HashMap<String, Object> result = objectMapper.readValue(jsonQuery, HashMap.class);
		Map<String, Object> configMap = new LinkedHashMap<>();
		configMap.put("indices", indices);
		//
		return gr.delete(result, configMap);
	}

}
