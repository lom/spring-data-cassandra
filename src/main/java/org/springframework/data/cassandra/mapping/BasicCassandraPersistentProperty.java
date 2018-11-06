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
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.model.AnnotationBasedPersistentProperty;
import org.springframework.data.mapping.model.Property;
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

    private final String columnName;
    private final String columnCryptoState;
    private final Class<?> columnCryptoDbType;
    private final Boolean isCrypto;
    private Boolean isEntity;

    /**
     * Creates a new {@link BasicCassandraPersistentProperty}.
     *
     */
    public BasicCassandraPersistentProperty(Property property, CassandraPersistentEntity<?> owner,
                                            SimpleTypeHolder simpleTypeHolder) {

        super(property, owner, simpleTypeHolder);

        if (property.getField().isPresent()) {
            final Column annotation = findAnnotation(Column.class);
            columnName = annotation != null && StringUtils.hasText(annotation.value())
                    ? annotation.value()
                    : CassandraNamingUtils.guessColumnName(property.getField().get().getName());

            final Crypto crypto = findAnnotation(Crypto.class);
            if (crypto != null) {
                isCrypto = Boolean.TRUE;
                columnCryptoState = crypto.columnState();
                columnCryptoDbType = crypto.columnDbType();
            } else {
                isCrypto = null;
                columnCryptoState = null;
                columnCryptoDbType = null;
            }
        } else {
            isCrypto = null;
            columnName = null;
            columnCryptoState = null;
            columnCryptoDbType = null;
        }
    }

    /**
     * Returns the column name to be used to store the columnCryptoState of the property inside the Cassandra.
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
    public boolean isCrypto() {
        return Boolean.TRUE.equals(isCrypto);
    }

    public String getColumnCryptoState() {
        return columnCryptoState;
    }

    public Class<?> getColumnCryptoDbType() {
        return columnCryptoDbType;
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
