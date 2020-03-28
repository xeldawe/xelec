package com.xel.mix.cassandra.core;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.cassandra.config.CassandraCqlClusterFactoryBean;
import org.springframework.data.cassandra.config.CassandraEntityClassScanner;
import org.springframework.data.cassandra.config.CassandraSessionFactoryBean;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.convert.CassandraConverter;
import org.springframework.data.cassandra.core.convert.MappingCassandraConverter;
import org.springframework.data.cassandra.core.mapping.CassandraMappingContext;
import org.springframework.data.cassandra.core.mapping.SimpleUserTypeResolver;

public class CassandraCore{

	private ContactPoint contactPoint;
	private Keyspace keyspace;
	private int port;
	private List<ContactPoint> contactPoints;
	private CassandraCqlClusterFactoryBean cluster;
	private static Map<CassandraCoreTemplate, CassandraOperations> sessions = new LinkedHashMap<CassandraCoreTemplate, CassandraOperations>();
	private CassandraCoreTemplate cassandraSession;

	public CassandraCore(CassandraCoreTemplate cassandraSession, ContactPoint contactPoint, Keyspace keyspace, int port) {
		super();
		this.cassandraSession = cassandraSession;
		this.contactPoint = contactPoint;
		this.keyspace = keyspace;
		this.port = port;
	}
	
	public CassandraCore(CassandraCoreTemplate cassandraSession, List<ContactPoint> contactPoints, Keyspace keyspace, int port) {
		super();
		this.cassandraSession = cassandraSession;
		this.contactPoints = contactPoints;
		this.keyspace = keyspace;
		this.port = port;
	}
	
	public void genCluster() {
		this.cluster = new CassandraCqlClusterFactoryBean();
		if(this.contactPoints != null) {
			cluster.setContactPoints(genContractPoints());
		}else {
			cluster.setContactPoints(this.contactPoint.getValue());
		}
		cluster.setJmxReportingEnabled(false);
		cluster.setPort(this.port);
		try {
			cluster.afterPropertiesSet();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String genContractPoints() {
		StringBuilder sb = new StringBuilder();
		for(ContactPoint cp:contactPoints) {
			sb.append(cp.getValue()).append(",");
		}
		String res = sb.toString();
		res.substring(0, res.length()-1);
		return res;
	}
	
	private CassandraMappingContext mappingContext() {
		CassandraMappingContext mappingContext = new CassandraMappingContext();
		try {
			mappingContext.setInitialEntitySet(CassandraEntityClassScanner.scan("com.xel.mix.cassandra"));
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mappingContext.setUserTypeResolver(new SimpleUserTypeResolver(this.cluster.getObject(), this.keyspace.getName()));
		mappingContext.afterPropertiesSet();
		return mappingContext;
	}

	private CassandraConverter converter() {
		return new MappingCassandraConverter(mappingContext());
	}

	private CassandraOperations cassandraTemplate() {
		try {
			CassandraTemplate templ = new CassandraTemplate(session().getObject());
			sessions.put(cassandraSession, templ);
			return templ;
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public CassandraSessionFactoryBean session() {
		CassandraSessionFactoryBean session = new CassandraSessionFactoryBean();
		genCluster();
		session.setCluster(this.cluster.getObject());
		session.setSchemaAction(SchemaAction.CREATE_IF_NOT_EXISTS);
		session.setKeyspaceName(this.keyspace.getName());
		session.setConverter(converter());
		try {
			session.afterPropertiesSet();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return session;
	}
	
	public static CassandraOperations getTemplate(CassandraCoreTemplate cassandraSession) {
		CassandraOperations result = sessions.get(cassandraSession);
		if(result == null) {
			CassandraCore cc = new CassandraCore(cassandraSession, cassandraSession.getContactPoint(), cassandraSession.getKeyspace(), cassandraSession.getPort());
			return cc.cassandraTemplate();
		}else {
			return result;
		}
	}
}
