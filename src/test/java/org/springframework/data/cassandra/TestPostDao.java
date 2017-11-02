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
package org.springframework.data.cassandra;

import com.datastax.driver.core.utils.UUIDs;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.entity.Post;
import org.springframework.data.cassandra.entity.PostDao;
import org.springframework.data.cassandra.entity.PostDaoImpl;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Date: 13.09.13 14:38
 *
 * @author Alexandr V Solomatin
 */
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/cassandra-test-repository.xml"})
public class TestPostDao {
    @Autowired
    PostDao dao;

    @Test
    public void saveAsync() throws ExecutionException, InterruptedException {
        assertNotNull(dao);

        Post post = new Post();
        post.setId(UUID.fromString("27b8b880-bb29-11e7-9467-eb5f74bc6fde"));
        post.setTitle("title");
        post.setBody("body");
        post.setType(Post.PostType.TYPE1);
        post.setCrypto(Boolean.TRUE);
        post.setCryptoString("Crypto String");
        post.setCryptoValue(333445566L);

        dao.saveAsync(post)
                .thenAccept(System.out::println)
                .get();

        Post post2 = new Post();
        post2.setId(UUID.fromString("27b8b880-bb29-11e7-9467-eb5f74bc6fdd"));
        post2.setTitle("title2");
        post2.setBody("body2");
        post2.setType(Post.PostType.TYPE2);
        post2.setCrypto(Boolean.FALSE);
        post2.setCryptoString("Crypto String 2");
        post2.setCryptoValue(66443322L);

        dao.saveAsync(post2)
                .thenAccept(System.out::println)
                .get();
    }

    @Test
    public void findOneAsync() throws ExecutionException, InterruptedException {
        assertNotNull(dao);

        Post post = dao.findOneAsync(UUID.fromString("27b8b880-bb29-11e7-9467-eb5f74bc6fde")).get();
        System.out.println(post);
        assertEquals("title", post.getTitle());
        assertEquals("body", post.getBody());
        assertEquals(Post.PostType.TYPE1, post.getType());
        assertEquals(Boolean.TRUE, post.getCrypto());
        assertEquals("Crypto String", post.getCryptoString());
        assertEquals(new Long("333445566"), post.getCryptoValue());

        Post post2 = dao.findOneAsync(UUID.fromString("27b8b880-bb29-11e7-9467-eb5f74bc6fdd")).get();
        System.out.println(post2);
        assertEquals("title2", post2.getTitle());
        assertEquals("body2", post2.getBody());
        assertEquals(Post.PostType.TYPE2, post2.getType());
        assertEquals(Boolean.FALSE, post2.getCrypto());
        assertEquals("Crypto String 2", post2.getCryptoString());
        assertEquals(new Long("66443322"), post2.getCryptoValue());
    }

}
