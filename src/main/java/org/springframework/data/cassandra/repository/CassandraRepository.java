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

import com.datastax.driver.core.ConsistencyLevel;
import org.springframework.data.repository.CrudRepository;

import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Date: 04.02.14 17:31
 *
 * @author Alexandr V Solomatin
 */
public interface CassandraRepository<T, ID extends Serializable> extends CrudRepository<T, ID>  {
    <S extends T> CompletableFuture<S> saveAsync(S entity);
    CompletableFuture<T> findOneAsync(ID id);
    Optional<T> findById(ID id, ConsistencyLevel level);
    CompletableFuture<Boolean> existsAsync(ID id);
    CompletableFuture<Iterable<T>> findAllAsync();
    CompletableFuture<Iterable<T>> findAllAsync (Iterable<ID> ids);
    CompletableFuture<Long> countAsync();
    CompletableFuture<Void> deleteAsync(ID id);
    CompletableFuture<Void> deleteAsync(T entity);
    CompletableFuture<Void> deleteAsync(Iterable<? extends T> entities);
    CompletableFuture<Void> deleteAllAsync();
    /**
     * @deprecated use org.springframework.data.repository.CrudRepository#saveAll(java.lang.Iterable)
     */
    default <S extends T> Iterable<S> save(Iterable<S> entities) {return saveAll(entities);}
    /**
     * @deprecated use org.springframework.data.repository.CrudRepository#findById(java.lang.Object)
     */
    default T findOne(ID id) { return findById(id).orElse(null);}
    /**
     * @deprecated use org.springframework.data.repository.CrudRepository#existsById(java.lang.Object)
     */
    default boolean exists(ID id) {return existsById(id);}
    /**
     * @deprecated use org.springframework.data.repository.CrudRepository#findAllById(java.lang.Iterable)
     */
    default Iterable<T> findAll(Iterable<ID> ids) {return findAllById(ids);}
    /**
     * @deprecated use org.springframework.data.repository.CrudRepository#deleteById(java.lang.Object)
     */
    default void delete(ID id) {deleteById(id);}
    /**
     * @deprecated use org.springframework.data.repository.CrudRepository#deleteAll(java.lang.Iterable)
     */
    default void delete(Iterable<? extends T> entities) {deleteAll(entities);}

    /**
     * @deprecated use org.springframework.data.cassandra.repository.CassandraRepository#findById(java.io.Serializable, com.datastax.driver.core.ConsistencyLevel)
     */
    default T findOne(ID id, ConsistencyLevel level) {return findById(id, level).orElse(null);}
}
