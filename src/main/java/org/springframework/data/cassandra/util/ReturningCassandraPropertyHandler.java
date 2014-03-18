package org.springframework.data.cassandra.util;

import org.springframework.data.cassandra.mapping.CassandraPersistentProperty;
import org.springframework.data.mapping.PropertyHandler;

/**
 * Date: 17.03.14 15:44
 *
 * @author lom
 */
public abstract class ReturningCassandraPropertyHandler<T> implements PropertyHandler<CassandraPersistentProperty> {
    protected T result;

    public T getResult() {
        return result;
    }

}
