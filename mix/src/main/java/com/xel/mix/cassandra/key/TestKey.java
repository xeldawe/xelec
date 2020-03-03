package com.xel.mix.cassandra.key;

import static org.springframework.data.cassandra.core.cql.Ordering.DESCENDING;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

@PrimaryKeyClass
public class TestKey implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4575538285123513118L;

	public TestKey(final String name, final UUID testId, final LocalDateTime createTime) {
		super();
		this.name = name;
		this.testId = testId;
		this.createTime = createTime;
	}

	@PrimaryKeyColumn(name = "test_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	private UUID testId;

	@PrimaryKeyColumn(name = "name", ordinal = 1, type = PrimaryKeyType.CLUSTERED, ordering = DESCENDING)
	private String name;
	
	@PrimaryKeyColumn(name = "create_time", ordinal = 2, type = PrimaryKeyType.CLUSTERED, ordering = DESCENDING)
	private LocalDateTime createTime;

	public UUID getTestId() {
		return testId;
	}

	public void setTestId(UUID testId) {
		this.testId = testId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LocalDateTime getCreateTime() {
		return createTime;
	}

	public void setCreateTime(LocalDateTime createTime) {
		this.createTime = createTime;
	}
	

}
