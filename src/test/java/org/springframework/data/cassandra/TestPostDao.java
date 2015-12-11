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
        post.setId(UUIDs.timeBased());
        post.setTitle("title");
        post.setBody("body");
        post.setType(Post.PostType.TYPE1);

        dao.saveAsync(post)
                .thenAccept(System.out::println)
                .get();
    }

    @Test
    public void findOneAsync() throws ExecutionException, InterruptedException {
        assertNotNull(dao);

        dao.findOneAsync(UUID.fromString("c1c6eb20-9fee-11e5-8544-c36c45c72deb"))
                .thenAccept(System.out::println)
                .get();
    }

}
