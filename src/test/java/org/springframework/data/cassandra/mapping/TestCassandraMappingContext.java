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

import org.junit.Test;
import org.springframework.data.annotation.Id;

import static org.junit.Assert.*;

/**
 * Date: 13.12.13 13:08
 *
 * @author Alexandr V Solomatin
 */
public class TestCassandraMappingContext {

    @Test
    public void getPersistentEntity() {
        CassandraMappingContext ctx = new CassandraMappingContext();
        BasicCassandraPersistentEntity<?> pe = ctx.getPersistentEntity(SomeEntity0.class);
        assertNotNull(pe);

        assertNotNull(pe.getIdProperty());
        assertEquals("identifier", pe.getIdProperty().getName());

        assertNotNull(pe.getPersistentProperty("someProperty"));
    }

    class SomeEntity0 {
        @Id
        private Integer identifier;
        private Integer someProperty;
    }

}
