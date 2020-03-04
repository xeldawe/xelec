package com.xel.mix.webclient.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.xel.mix.controller.ServerResponse;
import com.xel.mix.webclient.bean.ClientConnectionBean;
import com.xel.mix.webclient.client.GenericWebClient;
import com.xel.mix.webclient.client.WebClientException;

@Service
@PropertySource(name = "rest", value = "rest.properties")
public class RequestService {

	private static Map<String, GenericWebClient<ServerResponse>> wc = new LinkedHashMap<>();
	private ExecutorService es = Executors.newFixedThreadPool(1);
	private ClientConnectionBean ccb = new ClientConnectionBean();
	private ExecutorService esCore = Executors.newCachedThreadPool();
	private static Map<UUID, ServerResponse> rData = new LinkedHashMap<>();

	@Value("${request.service.timeout:1000}")
	private int timeout;

	private enum DataMode {
		URL, IP, PORT, ENDPOINT;
	}

	@SuppressWarnings("unchecked")
	public UUID requestUrl(String url) {
		UUID id = UUID.randomUUID();
		Runnable r =()->{
			Map<DataMode, Object> data = new LinkedHashMap<>();
			data.put(DataMode.URL, url);
			rData.put(id, getResponse(data));
		};
		esCore.submit(r);
		return id;
	}

	@SuppressWarnings("unchecked")
	public UUID requestUrl(String ip, int port, String endpoint) {
		UUID id = UUID.randomUUID();
		Runnable r =()->{
			Map<DataMode, Object> data = new LinkedHashMap<>();
			data.put(DataMode.IP, ip);
			data.put(DataMode.PORT, port);
			data.put(DataMode.ENDPOINT, endpoint);
			rData.put(id, getResponse(data));
		};
		esCore.submit(r);
		return id;
	}

	@SuppressWarnings("unchecked")
	private ServerResponse getResponse(Map<DataMode, Object> data) {
		ServerResponse response = new ServerResponse();
		try {
			createClient(data).getFlux().subscribe(res -> response.setMessage(res));
			int counter = 0;
			while (response.getMessage() == null) {
				counter++;
				try {
					TimeUnit.MILLISECONDS.sleep(1);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (counter == timeout) {
					break;
				}
			}
		} catch (NullPointerException e1 ) {
			System.out.println("HI");
			response.setHttpStatus(HttpStatus.BAD_GATEWAY);
			System.err.println("Unable to connect! "+ccb.getPrefix()+" "+ccb.getIp()+" "+ccb.getPort()+" "+ccb.getEndpoint());
		} catch (WebClientException e1) {
			System.out.println("HI");
			response.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
			//System.err.println(e1.getMessage());
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
				if (ccb.getPort() != 443) {
					ccb.setPrefix("http://");
				} else {
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
		GenericWebClient<ServerResponse> webClient = wc.get(ccb.getIp() + ccb.getPort());
		if (webClient == null) {
			webClient = new GenericWebClient<>(ccb.getEndpoint(), ServerResponse.class);
			webClient.createWebClient(ccb.getIp(), ccb.getPort());
			wc.put(ccb.getIp() + ccb.getPort(), webClient);
		}
		this.ccb = ccb;
		es.submit(wcCheck);
		return webClient;
	}

	Runnable wcCheck = () -> {
		if (wc.size() > 1000) {
			wc.clear();
		}
	};

	private ClientConnectionBean checkUrl(String url, ClientConnectionBean ccb) {
		// Check prefix
		String prefix = null;
		if (url.contains("https://")) {
			prefix = "https://";
			url = url.substring(8);
		} else if (url.contains("http://")) {
			prefix = "http://";
			url = url.substring(7);
		}
		ccb.setPrefix(prefix);
		// Check endpoint
		if (url.contains("/")) {
			ccb.setEndpoint(url.substring(url.indexOf("/")));
			url = url.substring(0, url.indexOf("/"));
		} else {
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
			if (ccb.getPort() == null) {
				ccb.setPort(443);
			}
			ccb.setPrefix("https://");
		} else if (prefix.equals("http://")) {
			ccb.setIp(url);
			if (ccb.getPort() == null) {
				ccb.setPort(80);
			}
			ccb.setPrefix("http://");
		} else {
			ccb.setIp(url);
			if (ccb.getPort() == null) {
				ccb.setPort(80);
			}
			ccb.setPrefix("http://");
		}
		return ccb;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	public ServerResponse getData(UUID id) {
		int counter = 0;
		ServerResponse response = null;
		while ((response=rData.get(id)) == null) {
			counter++;
			try {
				TimeUnit.MILLISECONDS.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (counter == timeout+1000) {
				break;
			}
		}
		rData.remove(id);
		return response;
	}
}

