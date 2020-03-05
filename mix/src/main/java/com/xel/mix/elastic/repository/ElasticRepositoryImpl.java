package com.xel.mix.elastic.repository;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.reindex.DeleteByQueryRequestBuilder;
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
public class ElasticRepositoryImpl implements ElasticRepository {

	private final Logger LOG = LoggerFactory.getLogger(getClass());

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
	public Flux<ServerResponse> findWithFilters(Map<String, String> map, Map<String, Object> configMap) {
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

	private BoolQueryBuilder getQuery(Map<String, String> map, Map<String, Object> configMap){
		BoolQueryBuilder qb = QueryBuilders.boolQuery();
		map.forEach((k,v)->{
			qb.must(QueryBuilders.matchQuery(k, v).fuzziness(Fuzziness.AUTO)
					.prefixLength(3).maxExpansions(10));
		});
		return qb;
	}

	@Override
	public Flux<ServerResponse> update(Object o) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Flux<ServerResponse> delete(Map<String, String> map, Map<String, Object> configMap) {
		ServerResponse sr = new ServerResponse();
		DeleteByQueryRequest request = new DeleteByQueryRequest();
		request.indices((String[]) configMap.get("indices"));
		request.setQuery(getQuery(map,configMap));
		request.setConflicts("proceed");
		request.setSlices(2); 
		request.setMaxDocs(10); 
		request.setBatchSize(100); 
		request.setScroll(TimeValue.timeValueMinutes(10)); 
		request.setTimeout(new TimeValue(60, TimeUnit.SECONDS));

		Object response = null;
		UUID uuid = ec.execute(request, Mode.DELETE);
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
}