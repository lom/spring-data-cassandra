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

import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Date: 05.02.14 16:46
 *
 * @author Alexandr V Solomatin
 */
@RunWith(EasyMockRunner.class)
public class TestCassandraTemplateImpl {
    final static private BatchAttributes ba = new BatchAttributes();

    @Mock
    Session session;
    @TestSubject
    CassandraTemplateImpl template = new CassandraTemplateImpl();

    @Test
    public void executeString() {
        String query = "query";
        expect(session.execute(query)).andReturn(null);
        replay(session);

        assertNull(template.execute(query));

        verify(session);
    }

    @Test
    public void executeStatement() {
        Statement statement = new SimpleStatement("test statement");
        expect(session.execute(statement)).andReturn(null);
        replay(session);

        assertNull(template.execute(statement));

        verify(session);
    }

    @Test
    public void simpleBatch() {
        Statement statement = QueryBuilder.insertInto("c");
        Statement statement1 = new SimpleStatement("ss");
        expect(session.execute(statement1)).andReturn(null);
        replay(session);

        template.startBatch(ba);
        template.execute(statement);
        template.execute(statement1);

        verify(session);

        reset(session);
        expect(session.execute(anyObject(Statement.class))).andReturn(null);
        replay(session);

        template.applyBatch();

        verify(session);
    }

    @Test(expected = IllegalStateException.class)
    public void notStartedBatchApply() {
        template.applyBatch();
    }

    @Test(expected = IllegalStateException.class)
    public void notStartedBatchCancel() {
        template.cancelBatch();
    }

    @Test
    public void nestedBatchStartFail() {
        template.startBatch(ba);

        BatchAttributes ba1 = new BatchAttributes();
        ba1.setTimestamp(10000L);

        try {
            template.startBatch(ba1);
            fail("expected IllegalStateException");
        } catch (IllegalStateException e) {
            template.cancelBatch();
        }

    }

    @Test
    public void nestedBatch() {
        Statement statement0 = QueryBuilder.insertInto("c");
        Statement statement1 = QueryBuilder.insertInto("d");

        expect(session.execute(anyObject(Statement.class))).andReturn(null);
        replay(session);

        template.startBatch(ba);
        template.execute(statement0);

        template.startBatch(ba);
        template.execute(statement1);

        template.applyBatch();
        template.applyBatch();

        verify(session);
    }

}
