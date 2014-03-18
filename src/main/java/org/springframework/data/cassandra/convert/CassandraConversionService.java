package org.springframework.data.cassandra.convert;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.convert.JodaTimeConverters;

/**
 * Date: 17.12.13 17:01
 *
 * @author lom
 */
final public class CassandraConversionService extends DefaultConversionService {

    public CassandraConversionService() {
        // register jodaTime converters
        for (final Converter jodaConverter: JodaTimeConverters.getConvertersToRegister()) {
            addConverter(jodaConverter);
        }

        // register byteBuf converters
        addConverter(new ArrayOfBytesToByteBufConverter());
        addConverter(new ByteBufToArrayOfBytesConverter());
    }

}
