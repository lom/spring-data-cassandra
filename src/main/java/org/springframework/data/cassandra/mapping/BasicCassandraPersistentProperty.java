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

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import org.springframework.data.cassandra.util.CassandraNamingUtils;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.model.AnnotationBasedPersistentProperty;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.util.StringUtils;

/**
 * Cassandra specific {@link org.springframework.data.mapping.model.AnnotationBasedPersistentProperty} implementation.
 * 
 * @author Alexandr V Solomatin
 *
 */
public class BasicCassandraPersistentProperty extends AnnotationBasedPersistentProperty<CassandraPersistentProperty>
        implements CassandraPersistentProperty {

    final private String columnName;
    private Boolean isEntity;

	/**
	 * Creates a new {@link BasicCassandraPersistentProperty}.
	 * 
	 * @param field
	 * @param propertyDescriptor
	 * @param owner
	 * @param simpleTypeHolder
	 */
	public BasicCassandraPersistentProperty(Field field, PropertyDescriptor propertyDescriptor,
			CassandraPersistentEntity<?> owner, SimpleTypeHolder simpleTypeHolder) {

		super(field, propertyDescriptor, owner, simpleTypeHolder);

        if (field != null) {
            final Column annotation = findAnnotation(Column.class);
            columnName = annotation != null && StringUtils.hasText(annotation.value())
                    ? annotation.value() : CassandraNamingUtils.guessColumnName(field.getName());
        } else {
            columnName = null;
        }
	}

	/**
	 * Returns the column name to be used to store the value of the property inside the Cassandra.
	 * 
	 * @return
	 */
    @Override
	public String getColumnName() {
        return columnName;
	}

	/**
	 * Returns true if the property has secondary index on this column. 
	 * 
	 * @return
	 */
    @Override
	public boolean isIndexed() {
        return isAnnotationPresent(Index.class);
	}

    @Override
    public boolean isEntity() {
        if (isEntity == null) {
            isEntity = super.isEntity();
        }

        return isEntity;
    }

    protected Association<CassandraPersistentProperty> createAssociation() {
		return new Association<CassandraPersistentProperty>(this, null);
	}

}
