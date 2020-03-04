package com.xel.mix.webclient.bean;

public class ClientConnectionBean {

	private String prefix;
	private String ip;
	private Integer port;
	private String endpoint;

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
	
	public void setThis(ClientConnectionBean ccb) {
		this.ip = ccb.getIp();
		this.port = ccb.getPort();
		this.endpoint = ccb.getEndpoint();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Connection -> PREFIX: ")
		.append(prefix)
		.append(" IP: ")
		.append(ip)
		.append(" PORT: ")
		.append(port)
		.append(" ENDPOINT: ")
		.append(endpoint);
		return sb.toString();
	}
}
