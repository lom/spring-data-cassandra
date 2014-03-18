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
 * @author Alexandr V Solomatin
 */
public class CassandraTemplateImpl implements CassandraTemplate {
    final private static Logger log = LoggerFactory.getLogger(CassandraTemplateImpl.class);

    final static private PersistenceExceptionTranslator EXCEPTION_TRANSLATOR = new CassandraExceptionTranslator();

    protected Session session;

    /**
     * @todo implement batch, maybe thread-local?
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
