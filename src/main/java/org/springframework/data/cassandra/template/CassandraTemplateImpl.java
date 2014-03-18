package org.springframework.data.cassandra.template;

import org.springframework.data.cassandra.core.CassandraExceptionTranslator;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.exceptions.DriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.support.PersistenceExceptionTranslator;

/**
 * Date: 05.02.14 16:29
 *
 * @author lom
 */
public class CassandraTemplateImpl implements CassandraTemplate {
    final private static Logger log = LoggerFactory.getLogger(CassandraTemplateImpl.class);

    final static private PersistenceExceptionTranslator EXCEPTION_TRANSLATOR = new CassandraExceptionTranslator();

    protected Session session;

    /**
     * @todo подумать над batch, возможно через thread-local сделать?
     */

    public void setSession(Session session) {
        this.session = session;
    }

    @Override
    public ResultSet execute(String query) {
        log.trace(query);

        try {
            return session.execute(query);
        } catch (DriverException e) {
            throw EXCEPTION_TRANSLATOR.translateExceptionIfPossible(e);
        }
    }

    @Override
    public ResultSet execute(Statement statement) {
        log.trace("{}", statement);

        try {
            return session.execute(statement);
        } catch (DriverException e) {
            throw EXCEPTION_TRANSLATOR.translateExceptionIfPossible(e);
        }
    }

}
