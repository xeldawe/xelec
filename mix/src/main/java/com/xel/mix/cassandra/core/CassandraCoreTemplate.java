package com.xel.mix.cassandra.core;

import org.springframework.data.cassandra.core.CassandraOperations;

public enum CassandraCoreTemplate {
	DEFAULT;
	
	private CassandraOperations session;
	private int port = 9042;
	private Keyspace keyspace = Keyspace.TEST;
	private ContactPoint contactPoint = ContactPoint.LOCALHOST;

	public CassandraOperations getSession() {
		return CassandraCore.getTemplate(this);
	};
	
	public void setSession(CassandraOperations session) {
        this.session = session;
    }

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public Keyspace getKeyspace() {
		return keyspace;
	}

	public void setKeyspace(Keyspace keyspace) {
		this.keyspace = keyspace;
	}

	public ContactPoint getContactPoint() {
		return contactPoint;
	}

	public void setContactPoint(ContactPoint contactPoint) {
		this.contactPoint = contactPoint;
	}
    
}
