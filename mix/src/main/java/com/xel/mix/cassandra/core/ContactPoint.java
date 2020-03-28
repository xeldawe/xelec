package com.xel.mix.cassandra.core;

public enum ContactPoint {

	LOCALHOST("127.0.0.1");
	
	private String value;
	 
	ContactPoint(String value) {
        this.value = value;
    }
 
    public String getValue() {
        return value;
    }
}
