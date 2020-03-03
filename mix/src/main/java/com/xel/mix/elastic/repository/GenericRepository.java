package com.xel.mix.elastic.repository;

import java.util.Map;

import org.springframework.stereotype.Repository;

import com.xel.mix.controller.ServerResponse;

import reactor.core.publisher.Flux;

@Repository
public interface GenericRepository<T> {

	void setClazz(Class<T> clazz);
	Flux<ServerResponse> findWithFilters(Map<String, String> map,
			Map<String, String> configMap);
	
	
}