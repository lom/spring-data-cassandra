package org.springframework.data.cassandra.repository;

import org.springframework.data.repository.CrudRepository;

import java.io.Serializable;

/**
 * Date: 04.02.14 17:31
 *
 * @author lom
 */
public interface CassandraRepository<T, ID extends Serializable> extends CrudRepository<T, ID>  {
}
