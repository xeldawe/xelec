package com.xel.mix.controller;

import org.springframework.http.HttpStatus;

public class ServerResponse {

	private Object message;
	private HttpStatus httpStatus;

	public Object getMessage() {
		return message;
	}

	public void setMessage(Object message) {
		this.message = message;
	}

	public HttpStatus getHttpStatus() {
		return httpStatus;
	}

	public void setHttpStatus(HttpStatus httpStatus) {
		this.httpStatus = httpStatus;
	}

	@Override
	public String toString() {
		return "ServerResponse [message=" + message + ", httpStatus=" + httpStatus + "]";
	}

}
