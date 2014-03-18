/*
 * Copyright 2011-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.cassandra.mapping;

import org.springframework.data.cassandra.util.CassandraNamingUtils;
import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.StringUtils;

import java.util.Comparator;

/**
 * Cassandra specific {@link BasicPersistentEntity} implementation that adds Cassandra specific meta-data such as the
 * table name.
 * 
 * @author Alex Shvid
 */
public class BasicCassandraPersistentEntity<T> extends BasicPersistentEntity<T, CassandraPersistentProperty>
        implements CassandraPersistentEntity<T> {

	private final String table;

	/**
	 * Creates a new {@link BasicCassandraPersistentEntity} with the given {@link TypeInformation}. Will default the
	 * table name to the entities simple type name.
	 * 
	 * @param typeInformation
	 */
	public BasicCassandraPersistentEntity(TypeInformation<T> typeInformation) {
		super(typeInformation, CassandraPersistentPropertyComparator.INSTANCE);

		final Class<?> rawType = typeInformation.getType();
		final String fallback = CassandraNamingUtils.guessTableName(rawType);

		if (rawType.isAnnotationPresent(Table.class)) {
			final Table d = rawType.getAnnotation(Table.class);
			this.table = StringUtils.hasText(d.value()) ? d.value() : fallback;
		} else {
			this.table = fallback;
		}
	}
	
	/**
	 * Returns the table the org.springframework.data.cassandra.entity shall be persisted to.
	 * 
	 * @return
	 */
	public String getTable() {
		return table;
	}
	
	/**
	 * {@link Comparator} implementation inspecting the {@link CassandraPersistentProperty}'s order.
	 * 
	 * @author Alex Shvid
	 */
	static enum CassandraPersistentPropertyComparator implements Comparator<CassandraPersistentProperty> {
		INSTANCE;

		public int compare(CassandraPersistentProperty o1, CassandraPersistentProperty o2) {
            return o1.getColumnName().compareTo(o2.getColumnName());
		}
	}
	
}
