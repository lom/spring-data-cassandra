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

import org.springframework.data.repository.CrudRepository;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;

/**
 * Date: 04.02.14 17:31
 *
 * @author Alexandr V Solomatin
 */
public interface CassandraRepository<T, ID extends Serializable> extends CrudRepository<T, ID>  {
    <S extends T> CompletableFuture<S> saveAsync(S entity);
    CompletableFuture<T> findOneAsync(ID id);
    CompletableFuture<Boolean> existsAsync(ID id);
    CompletableFuture<Iterable<T>> findAllAsync();
    CompletableFuture<Iterable<T>> findAllAsync (Iterable<ID> ids);
    CompletableFuture<Long> countAsync();
    CompletableFuture<Void> deleteAsync(ID id);
    CompletableFuture<Void> deleteAsync(T entity);
    CompletableFuture<Void> deleteAsync(Iterable<? extends T> entities);
    CompletableFuture<Void> deleteAllAsync();
}
