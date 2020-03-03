package com.xel.mix.cassandra.entity;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import com.xel.mix.cassandra.key.TestKey;

@Table("Test19")
public class Test {


	@PrimaryKey(value = "key")
	private TestKey key;
	@Column
	private String address;
	@Column
	private String email;
	
	
	public TestKey getKey() {
		return key;
	}
	public void setKey(TestKey key) {
		this.key = key;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
}
