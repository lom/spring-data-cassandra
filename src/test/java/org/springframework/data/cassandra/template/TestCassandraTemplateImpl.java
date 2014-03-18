package org.springframework.data.cassandra.template;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
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
 * @author lom
 */
@RunWith(EasyMockRunner.class)
public class TestCassandraTemplateImpl {
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

}
