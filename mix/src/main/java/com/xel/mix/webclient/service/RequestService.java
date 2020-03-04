package com.xel.mix.webclient.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.action.get.GetResponse;
import org.springframework.beans.factory.annotation.Value;

import com.xel.mix.controller.ServerResponse;
import com.xel.mix.webclient.bean.ClientConnectionBean;
import com.xel.mix.webclient.client.GenericWebClient;

public class RequestService {
	
	@Value("${request.service.timeout.ms:5000}")
	private int timeout;

	private enum DataMode {
		URL, IP, PORT, ENDPOINT;
	}

	@SuppressWarnings("unchecked")
	public Object requestUrl(String url) {
		Map<DataMode, Object> data = new LinkedHashMap<>();
		data.put(DataMode.URL, url);
		return getResponse(data);
	}

	@SuppressWarnings("unchecked")
	public Object requestUrl(String ip, int port, String endpoint) {
		Map<DataMode, Object> data = new LinkedHashMap<>();
		data.put(DataMode.IP, ip);
		data.put(DataMode.PORT, port);
		data.put(DataMode.ENDPOINT, endpoint);
		return getResponse(data);
	}

	@SuppressWarnings("unchecked")
	private ServerResponse getResponse(Map<DataMode, Object> data) {
		ServerResponse response = new ServerResponse();
		createClient(data).getFlux().subscribe(res -> response.setMessage(res));
		int counter = 0;
		while(response.getMessage()==null) {
			counter++;
			try {
				TimeUnit.MILLISECONDS.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(counter == timeout) {
				break;
			}
		}
		return response;
	}
	
	@SuppressWarnings("rawtypes")
	private GenericWebClient createClient(Map<DataMode, Object> data) {
		ClientConnectionBean ccb = new ClientConnectionBean();
		data.forEach((k, v) -> {
			switch (k) {
			case URL:
				String url = (String) v;
				ccb.setThis(checkUrl(url, ccb));
				break;
			case IP:
				ccb.setIp((String) v);
				break;
			case PORT:
				ccb.setPort(Integer.valueOf((String) v));
				if(ccb.getPort()!=443) {
					ccb.setPrefix("http://");
				}else {
					ccb.setPrefix("https://");
				}
				break;
			case ENDPOINT:
				ccb.setEndpoint((String) v);
				break;

			default:
				break;
			}
		});
		GenericWebClient<ServerResponse> webClient = new GenericWebClient<>(ccb.getEndpoint(), ServerResponse.class);
		webClient.createWebClient(ccb.getIp(), ccb.getPort());
		System.err.println(ccb.toString());
		return webClient;
	}

	private ClientConnectionBean checkUrl(String url, ClientConnectionBean ccb) {
		// Check prefix
		String prefix= null;
		if (url.contains("https://")) {
			prefix = "https://";
			url = url.substring(8);
		}else if(url.contains("http://")){
			prefix = "http://";
			url = url.substring(7);
		}
		ccb.setPrefix(prefix);
		// Check endpoint
		if (url.contains("/")) {
			ccb.setEndpoint(url.substring(url.indexOf("/")));
			url = url.substring(0, url.indexOf("/"));
		}else {
			ccb.setEndpoint("/");
		}
		// Check port
		if (url.contains(":")) {
			ccb.setPort(Integer.valueOf(url.substring(url.indexOf(":") + 1)));
			url = url.substring(0, url.indexOf(":"));
		}
		// Check IP
		if (prefix.equals("https://")) {
			ccb.setIp(url);
			if(ccb.getPort()==null) {
				ccb.setPort(443);
			}
			ccb.setPrefix("https://");
		} else if (prefix.equals("http://")) {
			ccb.setIp(url);
			if(ccb.getPort()==null) {
				ccb.setPort(80);
			}
			ccb.setPrefix("http://");
		} else {
			ccb.setIp(url);
			if(ccb.getPort()==null) {
				ccb.setPort(80);
			}
			ccb.setPrefix("http://");
		}
		return ccb;
	}
}
