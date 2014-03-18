/*
 * Copyright 2013 the original author or authors.
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
package org.springframework.data.cassandra.convert;

import org.springframework.data.cassandra.mapping.CassandraPersistentProperty;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.Row;
import org.springframework.data.mapping.model.PropertyValueProvider;
import org.springframework.util.Assert;
import java.nio.ByteBuffer;

/**
 * {@link PropertyValueProvider} to read property values from a {@link Row}.
 * 
 * @author Alex Solomatin
 */
public class CassandraPropertyValueProvider implements PropertyValueProvider<CassandraPersistentProperty> {

	private final Row source;

	/**
	 * Creates a new {@link CassandraPropertyValueProvider} with the given {@link Row}.
	 * 
	 * @param source must not be {@literal null}.
	 */
	public CassandraPropertyValueProvider(Row source) {
		Assert.notNull(source);
		this.source = source;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getPropertyValue(CassandraPersistentProperty property) {
		final String columnName = property.getColumnName();

		if (source.isNull(columnName)) {
			return null;
		}

		final DataType columnType = source.getColumnDefinitions().getType(columnName);
		final ByteBuffer bytes = source.getBytesUnsafe(columnName);

		return (T) columnType.deserialize(bytes);
	}
	
}
