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

import org.springframework.data.mapping.context.AbstractMappingContext;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.util.TypeInformation;

/**
 * Default implementation of a {@link MappingContext} for Cassandra using {@link BasicCassandraPersistentEntity} and
 * {@link BasicCassandraPersistentProperty} as primary abstractions.
 * 
 * @author Alexandr V Solomatin
 */
public class CassandraMappingContext
        extends AbstractMappingContext<BasicCassandraPersistentEntity<?>, CassandraPersistentProperty> {

	/**
	 * Creates a new {@link CassandraMappingContext}.
	 */
	public CassandraMappingContext() {
		setSimpleTypeHolder(CassandraSimpleTypeHolder.HOLDER);
	}

	protected <T> BasicCassandraPersistentEntity<T> createPersistentEntity(TypeInformation<T> typeInformation) {
		return new BasicCassandraPersistentEntity<T>(typeInformation);
	}

	@Override
    protected CassandraPersistentProperty createPersistentProperty(Property property,
            BasicCassandraPersistentEntity<?> owner, SimpleTypeHolder simpleTypeHolder) {

        return new BasicCassandraPersistentProperty(property, owner, simpleTypeHolder);
	}
}
