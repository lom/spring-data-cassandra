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
 * @author lom
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

        @Transient
        private String transientColumn;

    }
}
