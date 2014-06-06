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
package org.springframework.data.cassandra.convert;

import com.datastax.driver.core.querybuilder.*;
import org.junit.Test;
import org.springframework.data.cassandra.entity.Comment;
import org.springframework.data.cassandra.entity.CommentEmbedded;
import org.springframework.data.cassandra.entity.CommentPk;
import org.springframework.data.cassandra.entity.Post;
import org.springframework.data.cassandra.mapping.CassandraPersistentEntity;
import org.springframework.data.mapping.model.MappingException;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Date: 13.03.14 14:40
 *
 * @author Alexandr V Solomatin
 */
public class TestMappingCassandraEntityConverter {
    private MappingCassandraEntityConverter converter = new MappingCassandraEntityConverter();

    final private UUID commentId = UUID.fromString("5ff6eb04-6c0b-4aaa-9a12-f51af7a7d6dc");
    final private UUID postId = UUID.fromString("b90fcb58-4e53-4908-9e80-4683049362dd");
    final private CommentPk commentPk = new CommentPk(postId, commentId);


    @Test
    public void getEntityId() {
        Comment comment = new Comment();

        assertNull(converter.getEntityId(comment));

        comment.setId(commentPk);

        CommentPk commentPk1 = (CommentPk)converter.getEntityId(comment);
        assertNotNull(commentPk1);
        assertEquals(commentPk, commentPk1);

        Post post = new Post();

        assertNull(converter.getEntityId(post));

        post.setId(postId);

        UUID postId1 = (UUID)converter.getEntityId(post);
        assertNotNull(postId1);
        assertEquals(postId, postId1);
    }

    @Test
    public void writeInsert() {
        Insert query = QueryBuilder.insertInto("c");
        converter.writeInsert(makeComment(), query);

        assertEquals(
            "INSERT INTO c(body_text,field_double,comment_id,post_id) VALUES ('some text',123.12,5ff6eb04-6c0b-4aaa-9a12-f51af7a7d6dc,b90fcb58-4e53-4908-9e80-4683049362dd);",
            query.toString()
        );

        Insert query1 = QueryBuilder.insertInto("c");
        converter.writeInsert(makePost(), query1);

        assertEquals(
                "INSERT INTO c(body_text,id,title,type) VALUES ('some body',b90fcb58-4e53-4908-9e80-4683049362dd,'some title','TYPE2');",
                query1.toString()
        );
    }

    @Test
    public void writeSelectIdClause() {
        Select query = QueryBuilder.select().all().from("c");
        converter.writeIdClause(Comment.class, commentPk, query);

        assertEquals(
            "SELECT * FROM c WHERE comment_id=5ff6eb04-6c0b-4aaa-9a12-f51af7a7d6dc AND post_id=b90fcb58-4e53-4908-9e80-4683049362dd;",
            query.toString()
        );

        query = QueryBuilder.select().all().from("c");
        converter.writeIdClause(Post.class, postId, query);

        assertEquals(
            "SELECT * FROM c WHERE id=b90fcb58-4e53-4908-9e80-4683049362dd;",
            query.toString()
        );
    }

    @Test
    public void writeUpdateIdClause() {
        Update query = QueryBuilder.update("c");
        query.with(QueryBuilder.set("a", "b"));
        converter.writeIdClause(Comment.class, commentPk, query);

        assertEquals(
            "UPDATE c SET a='b' WHERE comment_id=5ff6eb04-6c0b-4aaa-9a12-f51af7a7d6dc AND post_id=b90fcb58-4e53-4908-9e80-4683049362dd;",
            query.toString()
        );

        query = QueryBuilder.update("c");
        query.with(QueryBuilder.set("a", "b"));
        converter.writeIdClause(Post.class, postId, query);

        assertEquals(
            "UPDATE c SET a='b' WHERE id=b90fcb58-4e53-4908-9e80-4683049362dd;",
            query.toString()
        );
    }

    @Test
    public void writeDeleteIdClause() {
        Delete query = QueryBuilder.delete().from("c");
        converter.writeIdClause(Comment.class, commentPk, query);

        assertEquals(
            "DELETE  FROM c WHERE comment_id=5ff6eb04-6c0b-4aaa-9a12-f51af7a7d6dc AND post_id=b90fcb58-4e53-4908-9e80-4683049362dd;",
            query.toString()
        );

        query = QueryBuilder.delete().from("c");
        converter.writeIdClause(Post.class, postId, query);

        assertEquals(
            "DELETE  FROM c WHERE id=b90fcb58-4e53-4908-9e80-4683049362dd;",
            query.toString()
        );
    }

    @Test
    public void writeSelectIdsClause() {
        Select query = QueryBuilder.select().all().from("c");
        List<UUID> postIds = Arrays.asList(postId, UUID.fromString("5ff6eb04-6c0b-4aaa-9a12-f51af7a7d6dc"));
        converter.writeIdsClause(Post.class, postIds, query);

        assertEquals(
            "SELECT * FROM c WHERE id IN (b90fcb58-4e53-4908-9e80-4683049362dd,5ff6eb04-6c0b-4aaa-9a12-f51af7a7d6dc);",
            query.toString()
        );
    }

    @Test
    public void writeUpdateIdsClause() {
        Update query = QueryBuilder.update("c");
        query.with(QueryBuilder.set("a", "b"));

        List<UUID> postIds = Arrays.asList(postId, UUID.fromString("5ff6eb04-6c0b-4aaa-9a12-f51af7a7d6dc"));
        converter.writeIdsClause(Post.class, postIds, query);

        assertEquals(
            "UPDATE c SET a='b' WHERE id IN (b90fcb58-4e53-4908-9e80-4683049362dd,5ff6eb04-6c0b-4aaa-9a12-f51af7a7d6dc);",
            query.toString()
        );
    }

    @Test
    public void writeDeleteIdsClause() {
        Delete query = QueryBuilder.delete().from("c");
        List<UUID> postIds = Arrays.asList(postId, UUID.fromString("5ff6eb04-6c0b-4aaa-9a12-f51af7a7d6dc"));
        converter.writeIdsClause(Post.class, postIds, query);

        assertEquals(
                "DELETE  FROM c WHERE id IN (b90fcb58-4e53-4908-9e80-4683049362dd,5ff6eb04-6c0b-4aaa-9a12-f51af7a7d6dc);",
                query.toString()
        );
    }

    @Test
    public void addAllEntityColumns() {
        Select.Selection selection = QueryBuilder.select();
        converter.addAllEntityColumns(converter.getMappingContext().getPersistentEntity(Comment.class), selection);
        Select query = selection.from("c");

        assertEquals(
            "SELECT body_text,field_double,field_timestamp,field_blob,field_inet,field_map,field_set,fielt_list,comment_id,post_id FROM c;",
            query.toString()
        );

        selection = QueryBuilder.select();
        converter.addAllEntityColumns(converter.getMappingContext().getPersistentEntity(Post.class), selection);
        query = selection.from("c");

        assertEquals(
            "SELECT body_text,id,title,type FROM c;",
            query.toString()
        );
    }

    @Test
    public void getColumn() {
        CassandraPersistentEntity persistentEntityPost = converter.getMappingContext().getPersistentEntity(Post.class);

        assertEquals("body_text", converter.getColumn("body", persistentEntityPost));
        assertEquals("id", converter.getColumn("id", persistentEntityPost));
        assertEquals("title", converter.getColumn("title", persistentEntityPost));

        try {
            converter.getColumn("notExistingProperty", persistentEntityPost);
            fail("expected IllegalArgumentException");
        } catch (MappingException e) {}

        CassandraPersistentEntity persistentEntityComment = converter.getMappingContext().getPersistentEntity(Comment.class);

        assertEquals("body_text", converter.getColumn("text", persistentEntityComment));
        assertEquals("field_blob", converter.getColumn("fieldBlob", persistentEntityComment));
        assertEquals("fielt_list", converter.getColumn("fieldList", persistentEntityComment));
        assertEquals("post_id", converter.getColumn("id.postId", persistentEntityComment));
        assertEquals("comment_id", converter.getColumn("id.commentId", persistentEntityComment));

        assertEquals("field_timestamp", converter.getColumn("commentEmbedded.fieldTimestamp", persistentEntityComment));
        assertEquals("field_double", converter.getColumn("commentEmbedded.fieldDouble", persistentEntityComment));

        try {
            converter.getColumn("notExistingProperty", persistentEntityComment);
            fail("expected IllegalArgumentException");
        } catch (MappingException e) {}

        try {
            converter.getColumn("notExisting.Property", persistentEntityComment);
            fail("expected IllegalArgumentException");
        } catch (MappingException e) {}

        try {
            // transient
            converter.getColumn("title", persistentEntityComment);
            fail("expected IllegalArgumentException");
        } catch (MappingException e) {}

    }

    private Comment makeComment() {
        Comment comment = new Comment();
        comment.setId(commentPk);
        comment.setText("some text");

        comment.setCommentEmbedded(new CommentEmbedded());
        comment.getCommentEmbedded().setFieldDouble(123.12D);

        return comment;
    }

    private Post makePost() {
        Post post = new Post();
        post.setId(postId);
        post.setTitle("some title");
        post.setBody("some body");
        post.setType(Post.PostType.TYPE2);

        return post;
    }

}
