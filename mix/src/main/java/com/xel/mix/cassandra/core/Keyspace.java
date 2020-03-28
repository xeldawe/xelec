package com.xel.mix.cassandra.core;

public enum Keyspace {

	TEST("test");
	
	private String name;
	 
	Keyspace(String name) {
        this.name = name;
    }
 
    public String getName() {
        return name;
    }
}
