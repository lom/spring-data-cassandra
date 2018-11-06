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
import org.easymock.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.cassandra.crypto.transformer.bytes.BytesEncryptor;
import org.springframework.data.cassandra.crypto.transformer.bytes.BytesTransformerFactory;
import org.springframework.data.cassandra.crypto.transformer.value.ValueEncryptor;
import org.springframework.data.cassandra.crypto.transformer.value.ValueTransformerFactory;
import org.springframework.data.cassandra.entity.Comment;
import org.springframework.data.cassandra.entity.CommentEmbedded;
import org.springframework.data.cassandra.entity.CommentPk;
import org.springframework.data.cassandra.entity.Post;
import org.springframework.data.cassandra.mapping.CassandraPersistentEntity;
import org.springframework.data.cassandra.mapping.CassandraPersistentProperty;
import org.springframework.data.mapping.MappingException;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Date: 13.03.14 14:40
 *
 * @author Alexandr V Solomatin
 */

@RunWith(EasyMockRunner.class)
public class TestMappingCassandraEntityConverter extends EasyMockSupport {
    final private UUID commentId = UUID.fromString("5ff6eb04-6c0b-4aaa-9a12-f51af7a7d6dc");
    final private UUID postId = UUID.fromString("b90fcb58-4e53-4908-9e80-4683049362dd");
    final private CommentPk commentPk = new CommentPk(postId, commentId);

    @Mock
    ValueTransformerFactory valueTransformerFactory;
    @Mock
    BytesTransformerFactory bytesTransformerFactory;
    @TestSubject
    MappingCassandraEntityConverter converter = new MappingCassandraEntityConverter();

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
            "INSERT INTO c (body_text,field_double,comment_id,post_id) VALUES ('some text',123.12,5ff6eb04-6c0b-4aaa-9a12-f51af7a7d6dc,b90fcb58-4e53-4908-9e80-4683049362dd);",
            query.toString()
        );

        Insert query1 = QueryBuilder.insertInto("c");
        converter.writeInsert(makePost(), query1);

        assertEquals(
                "INSERT INTO c (body_text,id,title,type) VALUES ('some body',b90fcb58-4e53-4908-9e80-4683049362dd,'some title','TYPE2');",
                query1.toString()
        );
    }

    @Test
    public void writeCryptoDisabledInsert() {
        Insert query1 = QueryBuilder.insertInto("c");

        ValueEncryptor valueEncryptor = createMock(ValueEncryptor.class);
        expect(valueEncryptor.encode(new Long("12345678"))).andReturn(ByteBuffer.wrap(new byte[]{0x11, 0x22, 0x33}));

        Capture<CassandraPersistentProperty> persistentPropertyCapture = new Capture<>();
        expect(valueTransformerFactory.encryptor(capture(persistentPropertyCapture)))
                .andReturn(valueEncryptor);

        replayAll();
        
        converter.writeInsert(makeCryptoPost(Boolean.FALSE), query1);

        assertEquals(
                "INSERT INTO c (body_text,crypto,crypto_string,crypto_value,id,title,type) VALUES ('some body',false,'some crypto string',0x112233,b90fcb58-4e53-4908-9e80-4683049362dd,'some title','TYPE2');",
                query1.toString()
        );

        CassandraPersistentProperty cassandraPersistentProperty = persistentPropertyCapture.getValue();
        assertTrue(cassandraPersistentProperty.isCrypto());
        assertEquals("crypto_value", cassandraPersistentProperty.getColumnName());
        assertEquals("cryptoValue", cassandraPersistentProperty.getName());

        verifyAll();
    }

    @Test
    public void writeCryptoEnabledInsert() {
        Insert query1 = QueryBuilder.insertInto("c");

        BytesEncryptor bytesEncryptor = createMock(BytesEncryptor.class);
        expect(bytesTransformerFactory.encryptor()).andReturn(bytesEncryptor).anyTimes();

        ValueEncryptor valueEncryptor = createMock(ValueEncryptor.class);
        expect(valueEncryptor.encrypt(eq(bytesEncryptor), anyObject(String.class))).andReturn("CryptedStringEncodedToBase64String");
        expect(valueEncryptor.encrypt(eq(bytesEncryptor), anyObject(Long.class))).andReturn(ByteBuffer.wrap(new byte[]{0x33, 0x22, 0x11}));
        
        expect(valueTransformerFactory.encryptor(anyObject(CassandraPersistentProperty.class)))
                .andReturn(valueEncryptor).anyTimes();

        replayAll();

        converter.writeInsert(makeCryptoPost(Boolean.TRUE), query1);

        assertEquals(
                "INSERT INTO c (body_text,crypto,crypto_string,crypto_value,id,title,type) VALUES ('some body',true,'CryptedStringEncodedToBase64String',0x332211,b90fcb58-4e53-4908-9e80-4683049362dd,'some title','TYPE2');",
                query1.toString()
        );

        verifyAll();
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
            "DELETE FROM c WHERE comment_id=5ff6eb04-6c0b-4aaa-9a12-f51af7a7d6dc AND post_id=b90fcb58-4e53-4908-9e80-4683049362dd;",
            query.toString()
        );

        query = QueryBuilder.delete().from("c");
        converter.writeIdClause(Post.class, postId, query);

        assertEquals(
            "DELETE FROM c WHERE id=b90fcb58-4e53-4908-9e80-4683049362dd;",
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
                "DELETE FROM c WHERE id IN (b90fcb58-4e53-4908-9e80-4683049362dd,5ff6eb04-6c0b-4aaa-9a12-f51af7a7d6dc);",
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
            "SELECT body_text,crypto,crypto_string,crypto_value,id,title,type FROM c;",
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

    private Post makeCryptoPost(Boolean bool) {
        Post post = makePost();
        post.setCryptoValue(new Long("12345678"));
        post.setCryptoString("some crypto string");
        post.setCrypto(bool);

        return post;
    }

}
