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

import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.exceptions.DriverException;
import com.datastax.driver.core.querybuilder.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.data.cassandra.core.CassandraExceptionTranslator;

/**
 * Date: 05.02.14 16:29
 *
 * @author Alexandr V Solomatin
 */
public class CassandraTemplateImpl implements CassandraTemplate {
    final private static Logger log = LoggerFactory.getLogger(CassandraTemplateImpl.class);

    final static private PersistenceExceptionTranslator EXCEPTION_TRANSLATOR = new CassandraExceptionTranslator();

    protected Session session;

    protected ThreadLocal<BatchContext> batchContext = new ThreadLocal<>();

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
        final BatchContext bc = batchContext.get();

        if (bc != null && isModifyingStatement(statement)) {
            bc.addStatement(statement);

            return null;
        } else {
            log.trace("{}", statement);

            try {
                return session.execute(statement);
            } catch (DriverException e) {
                throw EXCEPTION_TRANSLATOR.translateExceptionIfPossible(e);
            }
        }
    }

    @Override
    public void startBatch(BatchAttributes batchAttributes) {
        if (log.isTraceEnabled())
            log.trace("starting batch with attributes {}", batchAttributes);

        if (batchContext.get() == null) {
            batchContext.set(new BatchContext(batchAttributes));
        } else {
            throw new IllegalStateException("Nested batch is not supported");
        }
    }

    @Override
    public void cancelBatch() {
        if (log.isTraceEnabled())
            log.trace("cancelling batch");

        batchContext.set(null);
    }

    @Override
    public void applyBatch() {
        if (log.isTraceEnabled())
            log.trace("applying batch");

        final BatchContext bc = batchContext.get();
        if (bc == null)
            throw new IllegalStateException("Trying to apply batch, but it is not started");

        execute(bc.getBatchStatement());
    }

    private boolean isModifyingStatement(Statement statement) {
        return statement instanceof Insert || statement instanceof Delete || statement instanceof Update;
    }

    final protected class BatchContext {
        final Batch batchStatement;

        protected BatchContext(BatchAttributes batchAttributes) {
            batchStatement = batchAttributes.isUnlogged()
                    ? QueryBuilder.unloggedBatch(new RegularStatement[]{})
                    : QueryBuilder.batch(new RegularStatement[]{});

            if (batchAttributes.getConsistencyLevel() != null)
                batchStatement.setConsistencyLevel(batchAttributes.getConsistencyLevel());

            if (batchAttributes.getTimestamp() != null)
                batchStatement.using(QueryBuilder.timestamp(batchAttributes.getTimestamp()));
        }

        protected void addStatement(Statement statement) {
            batchStatement.add((RegularStatement) statement);
        }

        public Batch getBatchStatement() {
            return batchStatement;
        }

    }

}
