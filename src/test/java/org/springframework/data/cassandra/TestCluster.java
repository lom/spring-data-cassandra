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

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.convert.MappingCassandraEntityConverter;
import org.springframework.data.cassandra.crypto.transformer.bytes.BytesTransformerFactory;
import org.springframework.data.cassandra.crypto.transformer.bytes.DefaultBytesTransformerFactory;
import org.springframework.data.cassandra.crypto.transformer.value.ValueTransformerFactory;
import org.springframework.data.cassandra.entity.Comment;
import org.springframework.data.cassandra.entity.Post;
import org.springframework.data.cassandra.mapping.CassandraMappingContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Date: 13.09.13 14:38
 *
 * @author Alexandr V Solomatin
 */
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/cassandra-test-cluster.xml"})
public class TestCluster {
    @Autowired
    Session session;

    @Autowired
    DefaultBytesTransformerFactory defaultBytesTransformerFactory;

    @Autowired
    ValueTransformerFactory valueTransformerFactory;

    @Autowired
    BytesTransformerFactory bytesTransformerFactory;

    MappingCassandraEntityConverter converter;

    @Before
    public void setUp() throws Exception {
        converter = new MappingCassandraEntityConverter(new CassandraMappingContext());
        converter.setValueTransformerFactory(valueTransformerFactory);
        converter.setBytesTransformerFactory(bytesTransformerFactory);
    }

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
    @Ignore
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
