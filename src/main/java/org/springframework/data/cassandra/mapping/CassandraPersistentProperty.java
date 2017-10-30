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

import org.springframework.data.mapping.PersistentProperty;

/**
 * Cassandra specific {@link org.springframework.data.mapping.PersistentProperty} extension.
 * 
 * @author Alex Shvid
 */
public interface CassandraPersistentProperty extends PersistentProperty<CassandraPersistentProperty> {

	/**
	 * Returns the name of the field a property is persisted to.
	 * 
	 * @return
	 */
	String getColumnName();
	
	/**
	 * Returns true if the property has secondary index on this column. 
	 * 
	 * @return
	 */
	boolean isIndexed();


	/**
	 * Returns true if the property has encryption on this column.
	 *
	 * @return
	 */
	boolean isCrypto();

	String getColumnCryptoState();
	Class<?> getColumnCryptoDbType();
}
