package com.xel.mix.cassandra.repository;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;

import com.xel.mix.cassandra.key.TestKey;
import com.xel.mix.cassandra.model.Test;

@Repository
public interface CassandraBaseRepository extends ReactiveCassandraRepository<Test, TestKey>{

}
