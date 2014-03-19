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
package org.springframework.data.cassandra.entity;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Update;
import com.datastax.driver.core.utils.UUIDs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.batch.Batch;
import org.springframework.data.cassandra.convert.CassandraEntityConverter;
import org.springframework.data.cassandra.repository.BaseCassandraRepository;
import org.springframework.data.cassandra.template.CassandraTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Date: 18.03.14 16:39
 *
 * @author Alexandr V Solomatin
 */
@Repository
public class PostDaoImpl extends BaseCassandraRepository<Post,UUID> implements PostDao {
    @Autowired
    @Override
    public void setTemplate(CassandraTemplate template) {
        super.setTemplate(template);
    }

    @Autowired
    @Override
    public void setConverter(CassandraEntityConverter converter) {
        super.setConverter(converter);
    }

    @Override
    protected Class<?> getEntityClass() {
        return Post.class;
    }

    @Batch
    @Override
    public void someMethod() {
        Post p = new Post();
        p.setId(UUIDs.timeBased());
        p.setTitle("batched post 1");
        p.setBody("batched body 1");
        save(p);

        someMethod1();

        updatePost(UUID.fromString("9d0a2bd0-af54-11e3-a6f7-2f4fb9a8f805"));
    }

    public void updatePost(UUID id) {
        Update query = baseUpdate();
        query.with(QueryBuilder.set(c("title"), "batched post 3"));
        query.with(QueryBuilder.set(c("body"), "batched body 3"));

        query.where(QueryBuilder.eq(c("id"), id));



        template.execute(query);
    }

    @Override
    public void someMethod1() {
        findOne(UUID.fromString("7c6c94a0-592c-11e2-8080-808080808080"));
    }
}
