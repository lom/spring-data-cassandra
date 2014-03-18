package org.springframework.data.cassandra.convert;

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
 * @author lom
 */
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
        expect(row.isNull("column")).andReturn(true);
        replay(row);

        CassandraPropertyValueProvider provider = new CassandraPropertyValueProvider(row);

        assertNull(provider.getPropertyValue(property));

        verify(row);
    }

    @Test
    public void rowHasItem() {
        Row row = createStrictMock(Row.class);
        expect(row.isNull("column")).andReturn(false);

        ColumnDefinitions cd = createNiceMock(ColumnDefinitions.class);
        expect(cd.getType("column")).andReturn(DataType.ascii());
        expect(row.getColumnDefinitions()).andReturn(cd);
        expect(row.getBytesUnsafe("column")).andReturn(Charset.forName("UTF-8").encode("some text"));

        replay(row, cd);

        CassandraPropertyValueProvider provider = new CassandraPropertyValueProvider(row);

        assertEquals("some text", provider.getPropertyValue(property));

        verify(row, cd);
    }

}
