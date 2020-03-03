package com.xel.mix.cassandra.repository;

import java.util.List;
import java.util.Map;

import com.xel.mix.cassandra.model.Test;

public interface TestRepository {

	  public List<Test> findAllBy(Map<String, ?> map);
	  
}
