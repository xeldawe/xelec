package com.xel.mix.elastic.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.xel.mix.controller.ServerResponse;
import com.xel.mix.elastic.client.ElasticClient;
import com.xel.mix.elastic.client.ElasticClient.Mode;

import reactor.core.publisher.Flux;

@Repository
public class ElasticRepositoryImpl implements ElasticRepository {

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	private ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	ElasticClient ec;

	@PostConstruct
	private void init() {
		objectMapper.configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, false);
	}

	@Override
	public Flux<ServerResponse> insert(String index, Object obj) {
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
			LOGGER.info(e.getMessage());
		}
		sr.setMessage(response);
		sr.setHttpStatus(HttpStatus.CREATED);
		return Flux.just(sr);
	}

	@Override
	public Flux<ServerResponse> findWithFilters(Map<String, Object> map, Map<String, Object> configMap) {
		ServerResponse sr = new ServerResponse();
		SearchRequest searchRequest = new SearchRequest();
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		sourceBuilder.from(0);
		sourceBuilder.size(5);
		sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
		sourceBuilder.query(getQuery(map));
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
			LOGGER.info(e.getMessage());
		}
		sr.setMessage(response);
		sr.setHttpStatus(HttpStatus.OK);
		return Flux.just(sr);
	}

	private QueryBuilder getQuery(Map<String, Object> map) {
		BoolQueryBuilder qb = QueryBuilders.boolQuery();
		map.forEach((k, v) -> {
			qb.must(QueryBuilders.matchQuery(k, v).fuzziness(Fuzziness.AUTO).prefixLength(3).maxExpansions(10));
		});
		QueryBuilder query = qb;
		return query;
	}

	@Override
	public Flux<ServerResponse> update(String json, Map<String, Object> map, Map<String, Object> configMap) {
		ServerResponse sr = new ServerResponse();
		UpdateByQueryRequest request = new UpdateByQueryRequest();
		request.setConflicts("proceed");
		request.setQuery(getQuery(map));
		request.indices((String[]) configMap.get("indices"));
		request.setMaxDocs(10);
		request.setBatchSize(100);
		request.setSlices(2);
		request.setScroll(TimeValue.timeValueMinutes(10));
		try {
			request.setScript(genScript(json));
		} catch (JsonProcessingException e1) {
			LOGGER.info(e1.getMessage());
		}
//		Object response = null;
		ec.execute(request, Mode.UPDATE);
//		UUID uuid = ec.execute(request, Mode.UPDATE);
//		Future<Object> futureResponse = ec.getData(uuid);
//		try {
//			while (!futureResponse.isDone()) {
//				TimeUnit.MILLISECONDS.sleep(1);
//			}
//			response = futureResponse.get();
//		} catch (InterruptedException | ExecutionException e) {
//			sr.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
//			LOGGER.info(e.getMessage());
//		}
//		sr.setMessage(response);
//		sr.setHttpStatus(HttpStatus.ACCEPTED);
		return Flux.just(sr);
	}

	@SuppressWarnings("unchecked")
	private Script genScript(String json) throws JsonMappingException, JsonProcessingException {
		StringBuilder sb = new StringBuilder();
		objectMapper.readValue(json, HashMap.class).forEach((k, v) -> {
			if(v.toString().contains("{")) {
				try {
					sb.append(genWithRootScript(k.toString(), new JSONObject(json).get(k.toString()).toString()));
				} catch (JsonProcessingException | JSONException e) {
					LOGGER.info(e.getMessage());
				}
			}else {
				sb.append("ctx._source.").append(k).append(" = ").append(conv(v)).append(";");
			}
		});
		return new Script(sb.toString());
	}

	@SuppressWarnings("unchecked")
	private String genWithRootScript(String root, String json) throws JsonMappingException, JsonProcessingException {
		StringBuilder sb = new StringBuilder();
		json = json.replace("=", ":");
		objectMapper.readValue(json, HashMap.class).forEach((k, v) -> {
			sb.append("ctx._source."+root+".").append(k).append(" = ").append(conv(v)).append(";");
		});
		return sb.toString();
	}
	
	@SuppressWarnings("unused")
	@Deprecated
	private String getFieldValue(String fieldName, String json) {
		JSONObject jsonObject = null;
		String value = null;
		try {
			jsonObject = new JSONObject(json);
		} catch (JSONException e) {
			LOGGER.info(e.getMessage());
		}
		try {
			value = conv(jsonObject.get(fieldName).toString());
		} catch (JSONException e) {
			LOGGER.info(e.getMessage());
		}
		return value;
	}

	private String conv(Object obj) {
//this will cause spring json exception
//		try {
//			Long tmp = Long.valueOf(obj);
//			return tmp.toString();
//		} catch (Exception e) {
//			String tmp = obj.toString().toLowerCase();
//			if (tmp.equals("true") || tmp.equals("false")) {
//				return tmp.toString();
//			}
//		}
		return "'" + obj.toString() + "'";
	}

	@Override
	public Flux<ServerResponse> delete(Map<String, Object> map, Map<String, Object> configMap) {
		ServerResponse sr = new ServerResponse();
		DeleteByQueryRequest request = new DeleteByQueryRequest();
		request.indices((String[]) configMap.get("indices"));
		request.setQuery(getQuery(map));
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
			LOGGER.info(e.getMessage());
		}
		sr.setMessage(response);
		sr.setHttpStatus(HttpStatus.OK);
		return Flux.just(sr);
	}
}