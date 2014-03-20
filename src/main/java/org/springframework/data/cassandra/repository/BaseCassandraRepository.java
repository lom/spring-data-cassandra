/*
 * Copyright 2014 the original author or authors.
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
package org.springframework.data.cassandra.repository;

import org.springframework.data.cassandra.convert.CassandraEntityConverter;
import org.springframework.data.cassandra.mapping.CassandraPersistentEntity;
import org.springframework.data.cassandra.template.CassandraTemplate;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.*;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.*;

/**
 * Date: 28.01.14 17:27
 *
 * @author Alexandr V Solomatin
 */
abstract public class BaseCassandraRepository<T, ID extends Serializable> implements CassandraRepository<T, ID> {
    protected CassandraTemplate template;
    protected CassandraEntityConverter converter;
    protected CassandraPersistentEntity persistentEntity;

    public void setTemplate(CassandraTemplate template) {
        this.template = template;
    }

    public void setConverter(CassandraEntityConverter converter) {
        this.converter = converter;
        persistentEntity = converter.getMappingContext().getPersistentEntity(getEntityClass());
    }

    abstract protected Class<?> getEntityClass();

    @Override
    public <S extends T> S save(S entity) {
        final Insert query = baseInsert();

        converter.writeInsert(entity, query);
        template.execute(query);

        return entity;
    }

    @Override
    public <S extends T> Iterable<S> save(Iterable<S> entities) {
        for (final S entity: entities) {
            save(entity);
        }

        return entities;
    }

    @Override
    public T findOne(ID id) {
        final Select query = baseSelect();

        converter.writeIdClause(getEntityClass(), id, query);

        return getByQuery(query);
    }

    @Override
    public boolean exists(ID id) {
        final Select query = QueryBuilder.select().countAll().from(getTable());
        queryReadOptions(query);

        converter.writeIdClause(getEntityClass(), id, query);

        return !template.execute(query).isExhausted();
    }

    @Override
    public Iterable<T> findAll() {
        final Select query = baseSelect();

        return getListByQuery(query);
    }

    @Override
    public Iterable<T> findAll(Iterable<ID> ids) {
        if (persistentEntity.getIdProperty().isEntity()) {
            // ID is complex, need multiple requests
            final ArrayList<T> result = new ArrayList<>(20);
            for (final ID id: ids) {
                final T persisted = findOne(id);
                if (persisted != null)
                    result.add(persisted);
            }

            return result;
        } else {
            final Select query = baseSelect();
            converter.writeIdsClause(getEntityClass(), ids, query);

            return getListByQuery(query);
        }
    }

    @Override
    public long count() {
        final Select query = QueryBuilder.select().countAll().from(getTable());
        queryReadOptions(query);

        final ResultSet rs = template.execute(query);
        if (rs.isExhausted())
            throw new DataAccessResourceFailureException("empty resultSet for count query");

        return rs.one().getLong(0);
    }

    @Override
    public void delete(ID id) {
        final Delete query = baseDelete();

        converter.writeIdClause(getEntityClass(), id, query);

        template.execute(query);
    }

    @Override
    public void delete(T entity) {
        delete((ID) converter.getEntityId(entity));
    }

    @Override
    public void delete(Iterable<? extends T> entities) {
        if (persistentEntity.getIdProperty().isEntity()) {
            // ID is complex, need multiple requests
            for (final T entity: entities) {
                delete(entity);
            }
        } else {
            final ArrayList<ID> ids = new ArrayList<>(20);
            for (final T entity: entities) {
                ids.add((ID)converter.getEntityId(entity));
            }

            Assert.notEmpty(ids);

            final Delete query = baseDelete();
            converter.writeIdsClause(getEntityClass(), ids, query);

            template.execute(query);
        }
    }

    @Override
    public void deleteAll() {
        template.execute(QueryBuilder.truncate(getTable()));
    }

    protected String c(String path) {
        return converter.getColumn(path, persistentEntity);
    }

    protected T getByQuery(Statement query) {
        final ResultSet rs = template.execute(query);
        if (rs.isExhausted())
            return null;

        return (T)converter.read(getEntityClass(), rs.one());
    }

    protected Iterable<T> getListByQuery(Statement query) {
        return resultSetToEntityIterator(template.execute(query));
    }

    protected String getTable() {
        return persistentEntity.getTable();
    }

    protected Select baseSelect() {
        final Select.Selection selection = QueryBuilder.select();
        converter.addAllEntityColumns(persistentEntity, selection);

        final Select query = selection.from(getTable());
        queryReadOptions(query);

        return query;
    }

    protected Insert baseInsert() {
        final Insert query = QueryBuilder.insertInto(getTable());
        queryWriteOptions(query);

        return query;
    }

    protected Update baseUpdate() {
        final Update query = QueryBuilder.update(getTable());
        queryWriteOptions(query);

        return query;
    }

    protected Update updateByIdQuery(ID id) {
        final Update query = baseUpdate();
        converter.writeIdClause(getEntityClass(), id, query);

        return query;
    }

    protected Delete baseDelete() {
        final Delete query = QueryBuilder.delete().from(getTable());
        queryWriteOptions(query);

        return query;
    }

    protected void queryReadOptions(final Statement query) {
        query.setConsistencyLevel(ConsistencyLevel.ONE);
    }

    protected void queryWriteOptions(final Statement query) {
        query.setConsistencyLevel(ConsistencyLevel.QUORUM);
    }

    protected Iterable<T> resultSetToEntityIterator(final ResultSet rs) {
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return new ResultSetToEntityIterator<T>(rs);
            }
        };
    }

    private class ResultSetToEntityIterator<T> implements Iterator<T> {
        final private Iterator<Row> delegate;

        ResultSetToEntityIterator(ResultSet rs) {
            this.delegate = rs.iterator();
        }

        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }

        @Override
        public T next() {
            final Row row = delegate.next();
            if (row == null)
                return null;

            return (T)converter.read(getEntityClass(), row);
        }

        @Override
        public void remove() {
            delegate.remove();
        }
    }

}
