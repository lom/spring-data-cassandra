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
package org.springframework.data.cassandra.mapping;

import org.springframework.data.cassandra.entity.Comment;
import org.springframework.data.cassandra.entity.Post;
import org.junit.Test;
import org.springframework.data.util.ClassTypeInformation;

import static org.junit.Assert.*;

/**
 * Date: 12.12.13 18:06
 *
 * @author Alexandr V Solomatin
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
