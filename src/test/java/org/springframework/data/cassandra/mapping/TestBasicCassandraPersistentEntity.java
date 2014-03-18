package org.springframework.data.cassandra.mapping;

import org.springframework.data.cassandra.entity.Comment;
import org.springframework.data.cassandra.entity.Post;
import org.junit.Test;
import org.springframework.data.util.ClassTypeInformation;

import static org.junit.Assert.*;

/**
 * Date: 12.12.13 18:06
 *
 * @author lom
 */
public class TestBasicCassandraPersistentEntity {

    @Test
    public void tablePost() {
        BasicCassandraPersistentEntity<Post> pe = new BasicCassandraPersistentEntity<>(
                ClassTypeInformation.from(Post.class));

        assertEquals("post", pe.getTable());
    }

    @Test
    public void tableComment() {
        BasicCassandraPersistentEntity<Comment> pe = new BasicCassandraPersistentEntity<>(
                ClassTypeInformation.from(Comment.class));

        assertEquals("comments", pe.getTable());
    }

    @Test
    public void camelToUnderscore() {
        BasicCassandraPersistentEntity<CamelTableName> pe = new BasicCassandraPersistentEntity<>(
                ClassTypeInformation.from(CamelTableName.class));

        assertEquals("camel_table_name", pe.getTable());
    }

    @Table
    final class CamelTableName {

    }

}
