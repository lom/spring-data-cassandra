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

import com.datastax.driver.core.querybuilder.*;
import org.springframework.data.cassandra.mapping.CassandraMappingContext;
import org.springframework.data.cassandra.mapping.CassandraPersistentEntity;
import org.springframework.data.cassandra.mapping.CassandraPersistentProperty;
import org.springframework.data.cassandra.util.ReturningCassandraPropertyHandler;
import com.datastax.driver.core.Row;
import com.google.common.base.Splitter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.data.convert.EntityInstantiator;
import org.springframework.data.convert.EntityInstantiators;
import org.springframework.data.mapping.PropertyHandler;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.BeanWrapper;
import org.springframework.data.mapping.model.MappingException;
import org.springframework.data.mapping.model.PersistentEntityParameterValueProvider;
import org.springframework.data.mapping.model.PropertyValueProvider;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Iterator;

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

    @SuppressWarnings("unchecked")
	public <R> R read(Class<R> clazz, Row row) {
		final TypeInformation<? extends R> typeToUse = ClassTypeInformation.from(clazz);
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

	private <S extends Object> S read(final CassandraPersistentEntity<S> entity, final Row row) {
		final PropertyValueProvider<CassandraPersistentProperty> propertyProvider = new CassandraPropertyValueProvider(row);
		final PersistentEntityParameterValueProvider<CassandraPersistentProperty> parameterProvider =
                new PersistentEntityParameterValueProvider<>(entity, propertyProvider, null);
		
		final EntityInstantiator instantiator = instantiators.getInstantiatorFor(entity);
		final S instance = instantiator.createInstance(entity, parameterProvider);

		final BeanWrapper<CassandraPersistentEntity<S>, S> wrapper = BeanWrapper.create(instance, conversionService);
		final S result = wrapper.getBean();

		// Set properties not already set in the constructor
		entity.doWithProperties(new PropertyHandler<CassandraPersistentProperty>() {
			public void doWithPersistentProperty(CassandraPersistentProperty prop) {

                // embedded org.springframework.data.cassandra.entity support
                if (prop.isEntity()) {
                    final CassandraPersistentEntity embeddedEntity = mappingContext.getPersistentEntity(prop.getActualType());
                    if (embeddedEntity == null)
                        throw new MappingException("No mapping metadata found for " + prop.getRawType().getName());

                    wrapper.setProperty(prop, read(embeddedEntity, row));
                    return;
                }

				if (!row.getColumnDefinitions().contains(prop.getColumnName()) || entity.isConstructorArgument(prop)) {
					return;
				}

				wrapper.setProperty(prop, propertyProvider.getPropertyValue(prop));
			}
		});
		
		return result;
	}

    @Override
    public Object getEntityId(Object source) {
        final CassandraPersistentEntity persistentEntity = getPersistentEntity(source.getClass());
        final CassandraPersistentProperty idProperty = (CassandraPersistentProperty)persistentEntity.getIdProperty();

        return BeanWrapper.create(source, null).getProperty(idProperty);
    }

    @Override
    public void writeInsert(final Object source, final Insert query) {
        final CassandraPersistentEntity persistentEntity = getPersistentEntity(source.getClass());
        final BeanWrapper<CassandraPersistentEntity<Object>, Object> wrapper =
                BeanWrapper.create(source, conversionService);

        persistentEntity.doWithProperties(new PropertyHandler<CassandraPersistentProperty>() {
            @Override
            public void doWithPersistentProperty(CassandraPersistentProperty prop) {
                final Object value = wrapper.getProperty(prop, prop.getType(), false);
                if (value == null)
                    return;

                if (prop.isEntity()) {
                    writeInsert(value, query);
                } else {
                    query.value(prop.getColumnName(), value);
                }
            }
        });
    }

    @Override
    public void writeIdClause(Class<?> clazz, Object id, Select query) {
        final CassandraPersistentEntity persistentEntity = getPersistentEntity(clazz);
        final CassandraPersistentProperty idProperty = (CassandraPersistentProperty)persistentEntity.getIdProperty();

        if (idProperty.isEntity()) {
            writeIdClauseEntity(id, query.where());
        } else {
            query.where().and(
                    QueryBuilder.eq(idProperty.getColumnName(), conversionService.convert(id, idProperty.getType())));
        }
    }

    private void writeIdClauseEntity(Object value, final Select.Where clause) {
        final CassandraPersistentEntity persistentEntity = getPersistentEntity(value.getClass());
        final BeanWrapper<CassandraPersistentEntity<Object>, Object> wrapper =
                BeanWrapper.create(value, conversionService);

        persistentEntity.doWithProperties(new PropertyHandler<CassandraPersistentProperty>() {
            @Override
            public void doWithPersistentProperty(CassandraPersistentProperty prop) {
                final Object convertedValue = wrapper.getProperty(prop, prop.getType(), false);
                if (convertedValue == null)
                    return;

                if (prop.isEntity()) {
                    writeIdClauseEntity(convertedValue, clause);
                } else {
                    clause.and(QueryBuilder.eq(prop.getColumnName(), convertedValue));
                }
            }
        });
    }


    @Override
    public void writeIdClause(Class<?> clazz, Object id, Update query) {
        final CassandraPersistentEntity persistentEntity = getPersistentEntity(clazz);
        final CassandraPersistentProperty idProperty = (CassandraPersistentProperty)persistentEntity.getIdProperty();

        if (idProperty.isEntity()) {
            writeIdClauseEntity(id, query.where());
        } else {
            query.where().and(
                    QueryBuilder.eq(idProperty.getColumnName(), conversionService.convert(id, idProperty.getType())));
        }
    }

    private void writeIdClauseEntity(Object value, final Update.Where clause) {
        final CassandraPersistentEntity persistentEntity = getPersistentEntity(value.getClass());
        final BeanWrapper<CassandraPersistentEntity<Object>, Object> wrapper =
                BeanWrapper.create(value, conversionService);

        persistentEntity.doWithProperties(new PropertyHandler<CassandraPersistentProperty>() {
            @Override
            public void doWithPersistentProperty(CassandraPersistentProperty prop) {
                final Object convertedValue = wrapper.getProperty(prop, prop.getType(), false);
                if (convertedValue == null)
                    return;

                if (prop.isEntity()) {
                    writeIdClauseEntity(convertedValue, clause);
                } else {
                    clause.and(QueryBuilder.eq(prop.getColumnName(), convertedValue));
                }
            }
        });
    }

    @Override
    public void writeIdClause(Class<?> clazz, Object id, Delete query) {
        final CassandraPersistentEntity persistentEntity = getPersistentEntity(clazz);
        final CassandraPersistentProperty idProperty = (CassandraPersistentProperty)persistentEntity.getIdProperty();

        if (idProperty.isEntity()) {
            writeIdClauseEntity(id, query.where());
        } else {
            query.where().and(
                    QueryBuilder.eq(idProperty.getColumnName(), conversionService.convert(id, idProperty.getType())));
        }
    }

    private void writeIdClauseEntity(Object value, final Delete.Where clause) {
        final CassandraPersistentEntity persistentEntity = getPersistentEntity(value.getClass());
        final BeanWrapper<CassandraPersistentEntity<Object>, Object> wrapper =
                BeanWrapper.create(value, conversionService);

        persistentEntity.doWithProperties(new PropertyHandler<CassandraPersistentProperty>() {
            @Override
            public void doWithPersistentProperty(CassandraPersistentProperty prop) {
                final Object convertedValue = wrapper.getProperty(prop, prop.getType(), false);
                if (convertedValue == null)
                    return;

                if (prop.isEntity()) {
                    writeIdClauseEntity(convertedValue, clause);
                } else {
                    clause.and(QueryBuilder.eq(prop.getColumnName(), convertedValue));
                }
            }
        });
    }

    @Override
    public void writeIdsClause(Class<?> clazz, Iterable ids, Select query) {
        final CassandraPersistentProperty idProperty = (CassandraPersistentProperty)getPersistentEntity(clazz).getIdProperty();

        query.where().and(QueryBuilder.in(idProperty.getColumnName(), getConvertedIds(idProperty, ids)));
    }

    @Override
    public void writeIdsClause(Class<?> clazz, Iterable ids, Update query) {
        final CassandraPersistentProperty idProperty = (CassandraPersistentProperty)getPersistentEntity(clazz).getIdProperty();

        query.where().and(QueryBuilder.in(idProperty.getColumnName(), getConvertedIds(idProperty, ids)));
    }

    @Override
    public void writeIdsClause(Class<?> clazz, Iterable ids, Delete query) {
        final CassandraPersistentProperty idProperty = (CassandraPersistentProperty)getPersistentEntity(clazz).getIdProperty();

        query.where().and(QueryBuilder.in(idProperty.getColumnName(), getConvertedIds(idProperty, ids)));
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
            convertedIds.add(conversionService.convert(id, idProperty.getType()));
        }

        return convertedIds.toArray();
    }

    private CassandraPersistentEntity getPersistentEntity(Class<?> clazz) {
        return (CassandraPersistentEntity) mappingContext.getPersistentEntity(clazz);
    }

}
