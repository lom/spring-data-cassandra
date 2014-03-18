package org.springframework.data.cassandra.template;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Statement;

/**
 * Date: 27.12.13 12:05
 *
 * @author lom
 */
public interface CassandraTemplate {

    ResultSet execute(String query);
    ResultSet execute(Statement statement);

}
