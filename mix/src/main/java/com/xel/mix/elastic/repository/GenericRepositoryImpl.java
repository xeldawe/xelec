package com.xel.mix.elastic.repository;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xel.mix.controller.ServerResponse;
import com.xel.mix.elastic.client.ElasticClient;
import com.xel.mix.elastic.client.ElasticClient.Mode;

import reactor.core.publisher.Flux;

@Repository
public class GenericRepositoryImpl<T> implements GenericRepository<T> {

	private final Logger LOG = LoggerFactory.getLogger(getClass());
	private Class<T> clazz;

	@Autowired
	ElasticClient ec;

	public Flux<ServerResponse> insert(String index, Object obj){
		ServerResponse sr = new ServerResponse();
		IndexRequest indexRequest = null;
		try {
			byte[] json = new ObjectMapper().writeValueAsBytes(obj);
			indexRequest = new IndexRequest(index).source(json, XContentType.JSON);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Object response = null;
		UUID uuid = ec.execute(indexRequest, Mode.INDEX);
		Future<Object> futureResponse = ec.getData(uuid);
		try {
			while (!futureResponse.isDone()) {
				TimeUnit.MILLISECONDS.sleep(1);
			}
			response = futureResponse.get();
		} catch (InterruptedException | ExecutionException e) {
			sr.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
			e.printStackTrace();
		}
		sr.setMessage(response);
		sr.setHttpStatus(HttpStatus.OK);
		return Flux.just(sr);
	}

	
	@Override
	public Flux<ServerResponse> findWithFilters(Map<String, String> map, Map<String, String> configMap) {
		ServerResponse sr = new ServerResponse();
		SearchRequest searchRequest = new SearchRequest();
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		sourceBuilder.from(0);
		sourceBuilder.size(5);
		sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
		sourceBuilder.query(getQuery(map,configMap));

		searchRequest.source(sourceBuilder);
		Object response = null;
		UUID uuid = ec.execute(searchRequest, Mode.SEARCH);
		Future<Object> futureResponse = ec.getData(uuid);
		try {
			while (!futureResponse.isDone()) {
				TimeUnit.MILLISECONDS.sleep(1);
			}
			response = futureResponse.get();
		} catch (InterruptedException | ExecutionException e) {
			sr.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
			e.printStackTrace();
		}
		sr.setMessage(response);
		sr.setHttpStatus(HttpStatus.OK);
		return Flux.just(sr);
	}

	@Override
	public void setClazz(Class<T> clazz) {
		this.clazz = clazz;
	}

	private BoolQueryBuilder getQuery(Map<String, String> map, Map<String, String> configMap){
		BoolQueryBuilder qb = QueryBuilders.boolQuery();
		map.forEach((k,v)->{
			qb.must(QueryBuilders.matchQuery(k, v).fuzziness(Fuzziness.AUTO)
					.prefixLength(3).maxExpansions(10));
		});
		return qb;
	}
}