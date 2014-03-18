package org.springframework.data.cassandra.mapping;

import org.junit.Test;
import org.springframework.data.annotation.Id;

import static org.junit.Assert.*;

/**
 * Date: 13.12.13 13:08
 *
 * @author lom
 */
public class TestCassandraMappingContext {

    @Test
    public void getPersistentEntity() {
        CassandraMappingContext ctx = new CassandraMappingContext();
        BasicCassandraPersistentEntity<?> pe = ctx.getPersistentEntity(SomeEntity.class);
        assertNotNull(pe);

        assertNotNull(pe.getIdProperty());
        assertEquals("identifier", pe.getIdProperty().getName());

        assertNotNull(pe.getPersistentProperty("someProperty"));
    }


    class SomeEntity {
        @Id
        private Integer identifier;
        private Integer someProperty;
    }

}
