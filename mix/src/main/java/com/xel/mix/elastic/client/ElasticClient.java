package com.xel.mix.elastic.client;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.http.HttpHost;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ElasticClient {

	public enum Mode {
		INDEX, GET, SEARCH, UPDATE, DELETE;
	}

	@Value("${elastic.search.http.hosts}")
	private List<String> hosts;

	private HttpHost[] httpHost;

	public ElasticClient(HttpHost[] httpHost) {
		super();
		this.httpHost = httpHost;
	}

	private ExecutorService es = Executors.newFixedThreadPool(100);
	private RestHighLevelClient client = null;
	private ActionListener<IndexResponse> indexListener;
	private ActionListener<GetResponse> getListener;
	private ActionListener<SearchResponse> searchListener;
	private ActionListener<BulkByScrollResponse> updateListener;
	private ActionListener<BulkByScrollResponse> deleteListener;
	private Map<UUID, Object> responseMap = new LinkedHashMap<>();
	private UUID currentRequestor;

	@PostConstruct
	private void init() {
		createClient(genHttpHosts());
		createGetListener();
		createIndexListener();
		createSearchListener();
		createUpdateListener();
		createDeleteListener();
	}

	private HttpHost[] genHttpHosts() {
		httpHost = new HttpHost[hosts.size()];
		for (int i = 0; i < httpHost.length; i++) {
			String[] tokens = hosts.get(i).split(":");
			String address = null;
			int port = 0;
			String mode = null;
			for (int j = 0; j < tokens.length; j++) {
				switch (j) {
				case 0:
					address = tokens[j];
					break;
				case 1:
					port = Integer.valueOf(tokens[j]);
					break;

				case 2:
					mode = tokens[j];
					break;

				default:
					break;
				}
			}
			httpHost[i] = new HttpHost(address, port, mode);
		}
		return this.httpHost;
	}

	private void createIndexListener() {
		indexListener = new ActionListener<IndexResponse>() {
			@Override
			public void onResponse(IndexResponse indexResponse) {
				responseMap.put(currentRequestor, indexResponse);
			}

			@Override
			public void onFailure(Exception e) {
				responseMap.put(currentRequestor, e.getMessage());
			}
		};
	}

	private void createGetListener() {
		getListener = new ActionListener<GetResponse>() {
			@Override
			public void onResponse(GetResponse getResponse) {
				responseMap.put(currentRequestor, getResponse);
			}

			@Override
			public void onFailure(Exception e) {
				responseMap.put(currentRequestor, e.getMessage());
			}
		};
	}

	private void createSearchListener() {
		searchListener = new ActionListener<SearchResponse>() {
			@Override
			public void onResponse(SearchResponse searchResponse) {
				responseMap.put(currentRequestor, searchResponse);
			}

			@Override
			public void onFailure(Exception e) {
				responseMap.put(currentRequestor, e.getMessage());
			}
		};
	}
	
	private void createUpdateListener() {
		updateListener = new ActionListener<BulkByScrollResponse>() {
			@Override
			public void onResponse(BulkByScrollResponse updateResponse) {
				//responseMap.put(currentRequestor, updateResponse);
			}

			@Override
			public void onFailure(Exception e) {
				responseMap.put(currentRequestor, e.getMessage());
			}
		};
	}
	
	private void createDeleteListener() {
		deleteListener = new ActionListener<BulkByScrollResponse>() {
			@Override
			public void onResponse(BulkByScrollResponse deleteResponse) {
				responseMap.put(currentRequestor, deleteResponse);
			}

			@Override
			public void onFailure(Exception e) {
				responseMap.put(currentRequestor, e.getMessage());
			}
		};
	}

	private void createClient(HttpHost... hosts) {
		client = new RestHighLevelClient(RestClient.builder(hosts));
	}

	public RestHighLevelClient getClient() throws UnknownHostException {
		return client;
	}

	public void closeClient() {
		try {
			client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public synchronized UUID execute(Object request, Mode mode) {
		currentRequestor = UUID.randomUUID();
		switch (mode) {
		case INDEX:
			client.indexAsync((IndexRequest) request, RequestOptions.DEFAULT, getListenerByEnum(mode));
			break;
		case GET:
			client.getAsync((GetRequest) request, RequestOptions.DEFAULT, getListenerByEnum(mode));
			break;
		case SEARCH:
			client.searchAsync((SearchRequest) request, RequestOptions.DEFAULT, getListenerByEnum(mode));
			break;
		case UPDATE:
			client.updateByQueryAsync((UpdateByQueryRequest) request, RequestOptions.DEFAULT, getListenerByEnum(mode));
			break;
		case DELETE:
			client.deleteByQueryAsync((DeleteByQueryRequest) request, RequestOptions.DEFAULT, getListenerByEnum(mode));
			break;
		default:
			break;
		}
		return currentRequestor;
	}

	@SuppressWarnings("rawtypes")
	private ActionListener getListenerByEnum(Mode mode) {
		switch (mode) {
		case INDEX:
			return this.indexListener;
		case GET:
			return this.getListener;
		case SEARCH:
			return this.searchListener;
		case DELETE:
			return this.deleteListener;

		default:
			return null;
		}
	}

	public Future<Object> getData(UUID uuid) {
		return es.submit(() -> {
			Object o = getDataFromMap(uuid);
			int counter = 0;
			if (o == null) {
				while (counter < 5000) {
					counter++;
					try {
						TimeUnit.MILLISECONDS.sleep(1);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					o = getDataFromMap(uuid);
					if (o != null) {
						return o;
					}
				}
				if (counter == 5000) {
					return null;
				}
			}
			return null;
		});
	}

	private Object getDataFromMap(UUID uuid) {
		Object o = responseMap.get(uuid);
		if (o != null) {
			responseMap.remove(uuid);
		}
		return o;
	}
}
