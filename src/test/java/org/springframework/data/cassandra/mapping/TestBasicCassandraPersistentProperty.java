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

import org.junit.Before;
import org.junit.Test;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

/**
 * Date: 13.12.13 12:33
 *
 * @author Alexandr V Solomatin
 */
public class TestBasicCassandraPersistentProperty {
    private CassandraPersistentEntity<SomeEntity> entity;

    @Before
    public void before() {
        entity = new BasicCassandraPersistentEntity<>(ClassTypeInformation.from(SomeEntity.class));
    }

    @Test
    public void isIdProperty() {
        assertTrue(getPropertyFor("id").isIdProperty());
        assertFalse(getPropertyFor("number").isIdProperty());
    }

    @Test
    public void isIndexed() {
        assertTrue(getPropertyFor("number").isIndexed());
        assertFalse(getPropertyFor("id").isIndexed());
    }

    @Test
    public void getColumnName() {
        assertEquals("column1", getPropertyFor("someColumn").getColumnName());
        assertEquals("another_column", getPropertyFor("anotherColumn").getColumnName());
    }

    @Test
    public void isTransient() {
        assertTrue(getPropertyFor("transientColumn").isTransient());
        assertFalse(getPropertyFor("id").isTransient());
    }

    @Test
    public void isCrypto() {
        assertTrue(getPropertyFor("cryptoColumn").isCrypto());
        assertTrue(getPropertyFor("cryptoColumn2").isCrypto());
        assertFalse(getPropertyFor("someColumn").isCrypto());
    }

    private CassandraPersistentProperty getPropertyFor(String fieldName) {
        Field field = ReflectionUtils.findField(SomeEntity.class, fieldName);
        return new BasicCassandraPersistentProperty(field, null, entity, new SimpleTypeHolder());
    }

    class SomeEntity {
        @Id
        private String id;

        @Index
        private Long number;

        @Column("column1")
        private String someColumn;

        private String anotherColumn;

        @Crypto(columnState = "crypto")
        private String cryptoColumn;

        @Crypto(columnState = "crypto")
        private String cryptoColumn2;

        private Boolean crypto;

        @Transient
        private String transientColumn;

    }
}
