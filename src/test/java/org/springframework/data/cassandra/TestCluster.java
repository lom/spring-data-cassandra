package org.springframework.data.cassandra;

import org.junit.Ignore;
import org.springframework.data.cassandra.convert.MappingCassandraEntityConverter;
import org.springframework.data.cassandra.mapping.CassandraMappingContext;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import org.springframework.data.cassandra.entity.Comment;
import org.springframework.data.cassandra.entity.Post;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 * Date: 13.09.13 14:38
 *
 * @author lom
 */
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/cassandra-test-cluster.xml"})
public class TestCluster {
    @Autowired
    Session session;

    MappingCassandraEntityConverter converter = new MappingCassandraEntityConverter(new CassandraMappingContext());

    @Test
    public void readPost() {
        assertNotNull(session);

        ResultSet resultSet = session.execute("select * from post limit 1;");
        if (resultSet.isExhausted())
            fail();

        Post post = converter.read(Post.class, resultSet.one());

        assertNotNull(post);
        System.out.println(post);
    }

    @Test
    public void readComment() {
        assertNotNull(session);

        ResultSet resultSet = session.execute("select * from comments limit 1;");
        if (resultSet.isExhausted())
            fail();

        Comment comment = converter.read(Comment.class, resultSet.one());

        assertNotNull(comment);
        System.out.println(comment);
    }

}
