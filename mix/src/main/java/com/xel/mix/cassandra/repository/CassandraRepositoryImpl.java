package com.xel.mix.cassandra.repository;

import static org.springframework.data.cassandra.core.query.Criteria.where;
import static org.springframework.data.cassandra.core.query.Query.query;

import java.util.List;
import java.util.Map;

import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.cassandra.core.query.CriteriaDefinition;
import org.springframework.data.cassandra.core.query.Query;
import org.springframework.stereotype.Repository;

import com.xel.mix.cassandra.model.Test;

@Repository
public class CassandraRepositoryImpl implements CassandraRepository {

	private CassandraTemplate template;
	private static final String TABLE = Test.class.getAnnotation(Table.class).value();


	public CassandraRepositoryImpl(CassandraTemplate template) {
		super();
		this.template = template;
	}


	public List<Test> findAllBy(Map<String, ?> map) {

		CriteriaDefinition def1 = where("address").is(map.get("address"));
		CriteriaDefinition def2 = where("email").is(map.get("email"));
		Query q = query(def1,def2).withAllowFiltering();
		List<Test> tests = template.select(q,Test.class);
		return tests;
	}
	
	

}
