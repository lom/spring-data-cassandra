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

import org.junit.Ignore;
import org.springframework.data.cassandra.mapping.CassandraPersistentProperty;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.Row;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.Charset;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Date: 13.12.13 14:14
 *
 * @author Alexandr V Solomatin
 */
@Ignore
public class TestCassandraPropertyValueProvider {

    CassandraPersistentProperty property;

    @Before
    public void setUp() {
        property = createNiceMock(CassandraPersistentProperty.class);
        expect(property.getColumnName()).andReturn("column").anyTimes();
        replay(property);
    }

    @Test
    public void rowIsNull() {
        Row row = createStrictMock(Row.class);
        expect(row.getColumnDefinitions()).andReturn(null);
        expect(row.isNull("column")).andReturn(true);
        replay(row);

        CassandraPropertyValueProvider provider = new CassandraPropertyValueProvider(row);

        assertNull(provider.getPropertyValue(property));

        verify(row);
    }

    @Test
    public void rowHasItem() {
        Row row = createStrictMock(Row.class);
        ColumnDefinitions cd = createNiceMock(ColumnDefinitions.class);

        expect(row.getColumnDefinitions()).andReturn(cd);
        expect(row.isNull("column")).andReturn(false);
        expect(row.isNull(0)).andReturn(false);
        expect(cd.getType(0)).andReturn(DataType.ascii());
        expect(row.getString(0)).andReturn("some text");

        replay(row, cd);

        CassandraPropertyValueProvider provider = new CassandraPropertyValueProvider(row);

        assertEquals("some text", provider.getPropertyValue(property));

        verify(row, cd);
    }

}
