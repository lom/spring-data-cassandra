/*
 * Copyright 2010-2011 the original author or authors.
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

import org.springframework.data.cassandra.mapping.CassandraPersistentEntity;
import org.springframework.data.cassandra.mapping.CassandraPersistentProperty;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.Select;
import org.springframework.core.convert.ConversionService;
import com.datastax.driver.core.Row;
import org.springframework.data.convert.EntityReader;
import org.springframework.data.mapping.context.MappingContext;

/**
 * Central Cassandra specific converter interface from Object to Row.
 * 
 * @author Alex Shvid
 */
public interface CassandraEntityConverter extends EntityReader<Object, Row> {

    /**
     * Returns the underlying {@link org.springframework.data.mapping.context.MappingContext} used by the converter.
     *
     * @return never {@literal null}
     */
    MappingContext<? extends CassandraPersistentEntity, CassandraPersistentProperty> getMappingContext();

    /**
     * Returns the underlying {@link org.springframework.core.convert.ConversionService} used by the converter.
     *
     * @return never {@literal null}.
     */
    ConversionService getConversionService();

    Object getEntityId(Object source);

    void writeInsert(final Object source, final Insert query);

    void writeIdClause(Class<?> clazz, Object id, Select query);

    void writeIdClause(Class<?> clazz, Object id, Delete query);

    void writeIdsClause(Class<?> clazz, Iterable ids, Select query);

    void writeIdsClause(Class<?> clazz, Iterable ids, Delete query);

    void addAllEntityColumns(CassandraPersistentEntity persistentEntity, Select.Selection query);

    String getColumn(String propertyPath, CassandraPersistentEntity persistentEntity);

}
