package com.xel.mix.cassandra.repository;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;

import com.xel.mix.cassandra.entity.Test;
import com.xel.mix.cassandra.key.TestKey;

@Repository
public interface BasicRepository extends ReactiveCassandraRepository<Test, TestKey>{

}
