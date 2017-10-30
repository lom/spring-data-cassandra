/*
 * Copyright 2013 by the original author(s).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.cassandra.convert;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.*;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.data.cassandra.crypto.transformer.bytes.BytesTransformerFactory;
import org.springframework.data.cassandra.crypto.transformer.value.ValueDecryptor;
import org.springframework.data.cassandra.crypto.transformer.value.ValueEncryptor;
import org.springframework.data.cassandra.crypto.transformer.value.ValueTransformerFactory;
import org.springframework.data.cassandra.mapping.CassandraMappingContext;
import org.springframework.data.cassandra.mapping.CassandraPersistentEntity;
import org.springframework.data.cassandra.mapping.CassandraPersistentProperty;
import org.springframework.data.cassandra.util.ReturningCassandraPropertyHandler;
import org.springframework.data.convert.EntityInstantiator;
import org.springframework.data.convert.EntityInstantiators;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.PropertyHandler;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.ConvertingPropertyAccessor;
import org.springframework.data.mapping.model.MappingException;
import org.springframework.data.mapping.model.PersistentEntityParameterValueProvider;
import org.springframework.data.mapping.model.PropertyValueProvider;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.Assert;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * {@link CassandraEntityConverter} that uses a {@link MappingContext} to do sophisticated mapping of domain objects to
 * {@link Row}.
 * 
 * @author Alexandr V Solomatin
 */
public class MappingCassandraEntityConverter implements CassandraEntityConverter {
	protected MappingContext<? extends CassandraPersistentEntity<?>, CassandraPersistentProperty> mappingContext =
            new CassandraMappingContext();
    protected GenericConversionService conversionService = new CassandraConversionService();
    protected EntityInstantiators instantiators = new EntityInstantiators();
    protected CassandraPersistentTypeResolver persistentTypeResolver = new CassandraPersistentTypeResolver();

    protected ConcurrentMap<Class, TypeInformation> typeInfoMap = Maps.newConcurrentMap();
    protected ValueTransformerFactory valueTransformerFactory;
    protected BytesTransformerFactory bytesTransformerFactory;

    /**
     * Creates default mapping converter
     */
    public MappingCassandraEntityConverter() {
        //
    }

    /**
	 * Creates a new {@link MappingCassandraEntityConverter} given the new {@link MappingContext}.
	 * 
	 * @param mappingContext must not be {@literal null}.
	 */
	public MappingCassandraEntityConverter(MappingContext<? extends CassandraPersistentEntity<?>, CassandraPersistentProperty> mappingContext) {
		this.mappingContext = mappingContext;
	}

    public void setInstantiators(EntityInstantiators instantiators) {
        this.instantiators = instantiators;
    }

    public GenericConversionService getConversionService() {
        return conversionService;
    }

    public void setConversionService(GenericConversionService conversionService) {
        this.conversionService = conversionService;
    }

    public CassandraPersistentTypeResolver getPersistentTypeResolver() {
        return persistentTypeResolver;
    }

    public void setPersistentTypeResolver(CassandraPersistentTypeResolver persistentTypeResolver) {
        this.persistentTypeResolver = persistentTypeResolver;
    }

    public ValueTransformerFactory getValueTransformerFactory() {
        return valueTransformerFactory;
    }

    public void setValueTransformerFactory(ValueTransformerFactory valueTransformerFactory) {
        this.valueTransformerFactory = valueTransformerFactory;
    }

    public BytesTransformerFactory getBytesTransformerFactory() {
        return bytesTransformerFactory;
    }

    public void setBytesTransformerFactory(BytesTransformerFactory bytesTransformerFactory) {
        this.bytesTransformerFactory = bytesTransformerFactory;
    }

    @SuppressWarnings("unchecked")
	public <R> R read(Class<R> clazz, Row row) {
        // внутри ClassTypeInformation synchronized map, пришлось кешить вызовы
        TypeInformation<? extends R> typeToUse = typeInfoMap.get(clazz);
        if (typeToUse == null) {
            typeToUse = ClassTypeInformation.from(clazz);
            typeInfoMap.putIfAbsent(clazz, typeToUse);
        }

		final Class<? extends R> rawType = typeToUse.getType();

		if (Row.class.isAssignableFrom(rawType)) {
			return (R) row;
		}
		
		final CassandraPersistentEntity<R> persistentEntity =
                (CassandraPersistentEntity<R>) mappingContext.getPersistentEntity(typeToUse);
		if (persistentEntity == null) {
			throw new MappingException("No mapping metadata found for " + rawType.getName());
		}

		return read(persistentEntity, row);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.convert.EntityConverter#getMappingContext()
	 */
	public MappingContext<? extends CassandraPersistentEntity<?>, CassandraPersistentProperty> getMappingContext() {
		return mappingContext;
	}

    @SuppressWarnings("unchecked")
	private <S extends Object> S read(final CassandraPersistentEntity<S> entity, final Row row) {
		final PropertyValueProvider<CassandraPersistentProperty> propertyProvider = new CassandraPropertyValueProvider(row);
		final PersistentEntityParameterValueProvider<CassandraPersistentProperty> parameterProvider =
                new PersistentEntityParameterValueProvider<>(entity, propertyProvider, null);
		
		final EntityInstantiator instantiator = instantiators.getInstantiatorFor(entity);
		final S instance = instantiator.createInstance(entity, parameterProvider);
        final ConvertingPropertyAccessor accessor = getConvertingPropertyAccessor(instance, entity);
		final S result = (S) accessor.getBean();

		// Set properties not already set in the constructor
		entity.doWithProperties(new PropertyHandler<CassandraPersistentProperty>() {
			public void doWithPersistentProperty(CassandraPersistentProperty prop) {

                // embedded org.springframework.data.cassandra.entity support
                if (prop.isEntity()) {
                    final CassandraPersistentEntity embeddedEntity = mappingContext.getPersistentEntity(prop.getActualType());
                    if (embeddedEntity == null)
                        throw new MappingException("No mapping metadata found for " + prop.getRawType().getName());

                    accessor.setProperty(prop, read(embeddedEntity, row));
                    return;
                }

				if (!row.getColumnDefinitions().contains(prop.getColumnName()) || entity.isConstructorArgument(prop)) {
					return;
				}

                if (prop.isCrypto()) {
                    if (!row.getColumnDefinitions().contains(prop.getColumnCryptoState()))
                        throw new MappingException("Can't find property that stored encryption state for "
                                + entity.getName() + "." + prop.getName());

                    final boolean tryDecrypt = row.getBool(prop.getColumnCryptoState());
                    final boolean tryDecode = ByteBuffer.class.isAssignableFrom(prop.getColumnCryptoDbType());
                    
                    if (tryDecrypt) {
                        final ValueDecryptor valueDecryptor = valueTransformerFactory.decryptor(prop);
                        final Object decryptedObj = valueDecryptor.decrypt(bytesTransformerFactory.decryptor(),
                                propertyProvider.getPropertyValue(prop));
                        accessor.setProperty(prop, decryptedObj);
                        return;
                    } else if (tryDecode) {
                        final ValueDecryptor valueDecryptor = valueTransformerFactory.decryptor(prop);
                        final Object decodedObj = valueDecryptor.decode(propertyProvider.getPropertyValue(prop));
                        accessor.setProperty(prop, decodedObj);
                        return;
                    }
                }

                accessor.setProperty(prop, propertyProvider.getPropertyValue(prop));
			}
		});
		
		return result;
	}

    private ConvertingPropertyAccessor getConvertingPropertyAccessor(
            Object source, CassandraPersistentEntity<?> entity) {

        final PersistentPropertyAccessor propertyAccessor = source instanceof PersistentPropertyAccessor
                ? (PersistentPropertyAccessor) source : entity.getPropertyAccessor(source);

        return new ConvertingPropertyAccessor(propertyAccessor, conversionService);
    }

    @Override
    public Object getEntityId(Object source) {
        final CassandraPersistentEntity persistentEntity = getPersistentEntity(source.getClass());
        final CassandraPersistentProperty idProperty = (CassandraPersistentProperty)persistentEntity.getIdProperty();

        return getConvertingPropertyAccessor(source, persistentEntity).getProperty(idProperty);
    }

    @Override
    public void writeInsert(final Object source, final Insert query) {
        final CassandraPersistentEntity persistentEntity = getPersistentEntity(source.getClass());
        final ConvertingPropertyAccessor accessor = getConvertingPropertyAccessor(source, persistentEntity);

        persistentEntity.doWithProperties(new PropertyHandler<CassandraPersistentProperty>() {
            @Override
            public void doWithPersistentProperty(CassandraPersistentProperty prop) {
                final Object value = accessor.getProperty(prop,
                        persistentTypeResolver.getPersistentType(prop.getType()));

                if (value == null)
                    return; //FIXME if you want save nulls to db

                if (prop.isEntity()) {
                    writeInsert(value, query);
                } else if (prop.isCrypto()) {
                    final PersistentProperty cryptoStateProperty =
                            prop.getOwner().getPersistentProperty(prop.getColumnCryptoState());

                    final boolean tryCrypt =
                            Boolean.TRUE.equals(accessor.getProperty(cryptoStateProperty,
                                    persistentTypeResolver.getPersistentType(cryptoStateProperty.getType())));

                    final boolean tryEncode = ByteBuffer.class.isAssignableFrom(prop.getColumnCryptoDbType());

                    if (tryCrypt) {
                        final ValueEncryptor valueEncryptor = valueTransformerFactory.encryptor(prop);
                        final Object encryptObj = valueEncryptor.encrypt(bytesTransformerFactory.encryptor(), value);
                        query.value(prop.getColumnName(), encryptObj);
                    } else if (tryEncode) {
                        final ValueEncryptor valueEncryptor = valueTransformerFactory.encryptor(prop);
                        query.value(prop.getColumnName(), valueEncryptor.encode(value));
                    } else {
                        query.value(prop.getColumnName(), value);
                    }
                } else {
                    query.value(prop.getColumnName(), value);
                }
            }
        });
    }

    @Override
    public void writeIdClause(Class<?> clazz, Object id, Select query) {
        for (final Clause clause: makeIdClauseList(clazz, id)) {
            query.where().and(clause);
        }
    }

    @Override
    public void writeIdClause(Class<?> clazz, Object id, Update query) {
        for (final Clause clause: makeIdClauseList(clazz, id)) {
            query.where().and(clause);
        }
    }

    @Override
    public void writeIdClause(Class<?> clazz, Object id, Delete query) {
        for (final Clause clause: makeIdClauseList(clazz, id)) {
            query.where().and(clause);
        }
    }

    private List<Clause> makeIdClauseList(Class<?> clazz, Object id) {
        final List<Clause> clauseList = new ArrayList<>();

        final CassandraPersistentEntity persistentEntity = getPersistentEntity(clazz);
        final CassandraPersistentProperty idProperty = (CassandraPersistentProperty)persistentEntity.getIdProperty();

        if (idProperty.isEntity()) {
            makeIdClauseListEntity(clauseList, id);
        } else {
            clauseList.add(
                    QueryBuilder.eq(
                            idProperty.getColumnName(),
                            conversionService.convert(id, getPersistentPropertyType(idProperty))
                    )
            );
        }

        return clauseList;
    }

    private void makeIdClauseListEntity(final List<Clause> clauseList, final Object value) {
        final CassandraPersistentEntity persistentEntity = getPersistentEntity(value.getClass());
        final ConvertingPropertyAccessor accessor = getConvertingPropertyAccessor(value, persistentEntity);

        persistentEntity.doWithProperties(new PropertyHandler<CassandraPersistentProperty>() {
            @Override
            public void doWithPersistentProperty(CassandraPersistentProperty prop) {
                final Object convertedValue = accessor.getProperty(prop, getPersistentPropertyType(prop));
                if (convertedValue == null)
                    return;

                if (prop.isEntity()) {
                    makeIdClauseListEntity(clauseList, convertedValue);
                } else {
                    clauseList.add(QueryBuilder.eq(prop.getColumnName(), convertedValue));
                }
            }
        });
    }

    @Override
    public void writeIdsClause(Class<?> clazz, Iterable ids, Select query) {
        query.where().and(makeIdsClause(clazz, ids));
    }

    @Override
    public void writeIdsClause(Class<?> clazz, Iterable ids, Update query) {
        query.where().and(makeIdsClause(clazz, ids));
    }

    @Override
    public void writeIdsClause(Class<?> clazz, Iterable ids, Delete query) {
        query.where().and(makeIdsClause(clazz, ids));
    }

    private Clause makeIdsClause(Class<?> clazz, Iterable ids) {
        final CassandraPersistentProperty idProperty = (CassandraPersistentProperty)getPersistentEntity(clazz).getIdProperty();

        return QueryBuilder.in(idProperty.getColumnName(), getConvertedIds(idProperty, ids));
    }

    @Override
    public void addAllEntityColumns(final CassandraPersistentEntity persistentEntity, final Select.Selection query) {
        persistentEntity.doWithProperties(new PropertyHandler<CassandraPersistentProperty>() {
            @Override
            public void doWithPersistentProperty(CassandraPersistentProperty prop) {
                if (prop.isEntity()) {
                    final CassandraPersistentEntity embeddedEntity = getPersistentEntity(prop.getActualType());
                    if (embeddedEntity == null)
                        throw new MappingException("No mapping metadata found for " + prop.getRawType().getName());

                    addAllEntityColumns(embeddedEntity, query);
                } else {
                    query.column(prop.getColumnName());
                }
            }
        });
    }

    public String getColumn(final String propertyPath, final CassandraPersistentEntity persistentEntity) {
        Assert.hasText(propertyPath);
        Assert.notNull(persistentEntity);

        final Iterator<String> parts = Splitter.on('.').split(propertyPath).iterator();

        final String result = getColumn(parts, persistentEntity);

        if (result == null) {
            throw new MappingException("column for path="+propertyPath+" not found");
        }

        return result;
    }

    private String getColumn(final Iterator<String> parts, final CassandraPersistentEntity persistentEntity) {
        if (!parts.hasNext()) {
            return null;
        }

        final String propertyName = parts.next();

        final ReturningCassandraPropertyHandler<String> propertyHandler = new ReturningCassandraPropertyHandler<String>() {
            @Override
            public void doWithPersistentProperty(CassandraPersistentProperty persistentProperty) {
                if (persistentProperty.getName().equals(propertyName)) {
                    if (persistentProperty.isEntity()) {
                        final CassandraPersistentEntity persistentEntity =
                                getPersistentEntity(persistentProperty.getActualType());
                        result = getColumn(parts, persistentEntity);
                    } else {
                        result = persistentProperty.getColumnName();
                    }
                }
            }
        };

        persistentEntity.doWithProperties(propertyHandler);

        return propertyHandler.getResult();
    }

    private Object[] getConvertedIds(CassandraPersistentProperty idProperty, Iterable ids) {
        final ArrayList convertedIds = new ArrayList(20);

        for (final Object id: ids) {
            convertedIds.add(conversionService.convert(id, getPersistentPropertyType(idProperty)));
        }

        return convertedIds.toArray();
    }

    private CassandraPersistentEntity getPersistentEntity(Class<?> clazz) {
        return (CassandraPersistentEntity) mappingContext.getPersistentEntity(clazz);
    }

    private Class<?> getPersistentPropertyType(CassandraPersistentProperty persistentProperty) {
        return persistentTypeResolver.getPersistentType(persistentProperty.getType());
    }

}
