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

import com.datastax.driver.core.*;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.data.cassandra.convert.CassandraEntityConverter;
import org.springframework.data.cassandra.core.CassandraExceptionTranslator;
import org.springframework.data.cassandra.mapping.CassandraPersistentEntity;
import org.springframework.data.cassandra.template.CassandraTemplate;
import com.datastax.driver.core.querybuilder.*;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Date: 28.01.14 17:27
 *
 * @author Alexandr V Solomatin
 */
abstract public class BaseCassandraRepository<T, ID extends Serializable> implements CassandraRepository<T, ID> {
    final static private PersistenceExceptionTranslator EXCEPTION_TRANSLATOR = new CassandraExceptionTranslator();

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

    protected void beforeInsert(T entity) {
        //
    }

    protected void afterFetch(T entity) {
        //
    }

    @Override
    public <S extends T> S save(S entity) {
        final Insert query = baseInsert();

        beforeInsert(entity);
        converter.writeInsert(entity, query);
        template.execute(query);

        return entity;
    }

    @Override
    public <S extends T> CompletableFuture<S> saveAsync(final S entity) {
        final Insert query = baseInsert();

        beforeInsert(entity);
        converter.writeInsert(entity, query);

        return executeQueryAsyncAndTransformResult(query, rs -> entity);
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
    public CompletableFuture<T> findOneAsync(ID id) {
        final Select query = baseSelect();

        converter.writeIdClause(getEntityClass(), id, query);

        return executeQueryAsyncAndTransformResult(query, this::getByResultSet);
    }

    @Override
    public boolean exists(ID id) {
        final Select query = QueryBuilder.select().countAll().from(getTable());
        queryReadOptions(query);

        converter.writeIdClause(getEntityClass(), id, query);

        return !template.execute(query).isExhausted();
    }

    @Override
    public CompletableFuture<Boolean> existsAsync(ID id) {
        final Select query = QueryBuilder.select().countAll().from(getTable());
        queryReadOptions(query);

        converter.writeIdClause(getEntityClass(), id, query);

        return executeQueryAsyncAndTransformResult(query, rs -> !rs.isExhausted());
    }

    @Override
    public Iterable<T> findAll() {
        final Select query = baseSelect();

        return getListByQuery(query);
    }

    @Override
    public CompletableFuture<Iterable<T>> findAllAsync() {
        final Select query = baseSelect();

        return executeQueryAsyncAndTransformResult(query, this::getListByResultSet);
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
    public CompletableFuture<Iterable<T>> findAllAsync(Iterable<ID> ids) {
        if (persistentEntity.getIdProperty().isEntity())
            throw new IllegalStateException("only non-composite ids supported");

        final Select query = baseSelect();
        converter.writeIdsClause(getEntityClass(), ids, query);

        return executeQueryAsyncAndTransformResult(query, this::getListByResultSet);
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
    public CompletableFuture<Long> countAsync() {
        final Select query = QueryBuilder.select().countAll().from(getTable());
        queryReadOptions(query);

        final CompletableFuture<Long> resultFuture = new CompletableFuture<>();

        Futures.addCallback(template.executeAsync(query), new FutureCallback<ResultSet>() {
            @Override
            public void onSuccess(ResultSet result) {
                if (result.isExhausted()) {
                    resultFuture.completeExceptionally(new DataAccessResourceFailureException("empty resultSet for count query"));
                    return;
                }

                resultFuture.complete(result.one().getLong(0));
            }

            @Override
            public void onFailure(Throwable t) {
                resultFuture.completeExceptionally(t);
            }
        });

        return resultFuture;
    }

    @Override
    public void delete(ID id) {
        final Delete query = baseDelete();

        converter.writeIdClause(getEntityClass(), id, query);

        template.execute(query);
    }

    @Override
    public CompletableFuture<Void> deleteAsync(ID id) {
        final Delete query = baseDelete();

        converter.writeIdClause(getEntityClass(), id, query);

        return executeQueryAsyncAndTransformResult(query, rs -> null);
    }

    @Override
    public void delete(T entity) {
        delete((ID) converter.getEntityId(entity));
    }

    @Override
    public CompletableFuture<Void> deleteAsync(T entity) {
        return deleteAsync((ID) converter.getEntityId(entity));
    }

    @Override
    public void delete(Iterable<? extends T> entities) {
        if (persistentEntity.getIdProperty().isEntity()) {
            // ID is complex, need multiple requests
            for (final T entity: entities) {
                delete(entity);
            }
        } else {
            template.execute(buildDeleteByIdsQuery(entities));
        }
    }

    @Override
    public CompletableFuture<Void> deleteAsync(Iterable<? extends T> entities) {
        if (persistentEntity.getIdProperty().isEntity())
            throw new IllegalStateException("only non-composite ids supported");

        final Delete query = buildDeleteByIdsQuery(entities);

        return executeQueryAsyncAndTransformResult(query, rs -> null);
    }

    private Delete buildDeleteByIdsQuery(Iterable<? extends T> entities) {
        final ArrayList<ID> ids = new ArrayList<>(20);
        for (final T entity: entities) {
            ids.add((ID)converter.getEntityId(entity));
        }

        Assert.notEmpty(ids);

        final Delete query = baseDelete();
        converter.writeIdsClause(getEntityClass(), ids, query);

        return query;
    }

    @Override
    public void deleteAll() {
        final Statement query = QueryBuilder.truncate(getTable());
        queryWriteOptions(query);

        template.execute(query);
    }

    @Override
    public CompletableFuture<Void> deleteAllAsync() {
        final Statement query = QueryBuilder.truncate(getTable());
        queryWriteOptions(query);

        return executeQueryAsyncAndTransformResult(query, rs -> null);
    }

    protected String c(String path) {
        return converter.getColumn(path, persistentEntity);
    }

    protected <N> CompletableFuture<N> executeQueryAsyncAndTransformResult(
            Statement query, Function<ResultSet, N> resultTransformer) {

        final CompletableFuture<N> resultFuture = new CompletableFuture<>();

        Futures.addCallback(template.executeAsync(query), new FutureCallback<ResultSet>() {
            @Override
            public void onSuccess(ResultSet result) {
                resultFuture.complete(resultTransformer.apply(result));
            }

            @Override
            public void onFailure(Throwable t) {
                resultFuture.completeExceptionally(t);
            }
        });

        return resultFuture;
    }

    protected T getByResultSet(final ResultSet rs) {
        if (rs.isExhausted())
            return null;

        final T entity = (T)converter.read(getEntityClass(), rs.one());

        afterFetch(entity);

        return entity;
    }

    protected T readEntity(Row row) {
        final T entity = (T)converter.read(getEntityClass(), row);

        afterFetch(entity);

        return entity;
    }

    protected T getByQuery(Statement query) {
        return getByResultSet(template.execute(query));
    }

    protected Iterable<T> getListByResultSet(final ResultSet rs) {
        return resultSetToEntityIterator(rs);
    }

    protected Iterable<T> getListByQuery(Statement query) {
        return getListByResultSet(template.execute(query));
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
        query.setConsistencyLevel(ConsistencyLevel.QUORUM);
    }

    protected void queryWriteOptions(final Statement query) {
        query.setConsistencyLevel(ConsistencyLevel.QUORUM);
    }

    protected Iterable<T> resultSetToEntityIterator(final ResultSet rs) {
        return () -> new ResultSetToEntityIterator<>(rs);
    }

    private class ResultSetToEntityIterator<TT extends T> implements Iterator<TT> {
        final private Iterator<Row> delegate;

        ResultSetToEntityIterator(ResultSet rs) {
            this.delegate = rs.iterator();
        }

        @Override
        public boolean hasNext() {
            try {
                return delegate.hasNext();
            } catch (RuntimeException e) {
                final RuntimeException translated = EXCEPTION_TRANSLATOR.translateExceptionIfPossible(e);
                throw  translated == null ? e : translated;
            }
        }

        @Override
        public TT next() {
            try {
                final Row row = delegate.next();
                if (row == null)
                    return null;

                final TT entity = (TT)converter.read(getEntityClass(), row);

                afterFetch(entity);

                return entity;
            } catch (RuntimeException e) {
                final RuntimeException translated = EXCEPTION_TRANSLATOR.translateExceptionIfPossible(e);
                throw  translated == null ? e : translated;
            }
        }

        @Override
        public void remove() {
            delegate.remove();
        }
    }

}
