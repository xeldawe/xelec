package com.xel.mix.elastic.repository;

import java.util.Map;

import org.springframework.stereotype.Repository;

import com.xel.mix.controller.ServerResponse;

import reactor.core.publisher.Flux;

@Repository
public interface ElasticRepository {

	Flux<ServerResponse> findWithFilters(Map<String, Object> map,
			Map<String, Object> configMap);
	Flux<ServerResponse> insert(String index, Object o);
	Flux<ServerResponse> delete(Map<String, Object> map, Map<String, Object> configMap);
	Flux<ServerResponse> insert(Class<?> clazz, Object obj);
	Flux<ServerResponse> update(String json, Map<String, Object> map, Map<String, Object> configMap);
	
}