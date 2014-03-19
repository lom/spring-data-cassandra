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
package org.springframework.data.cassandra.batch;

import com.datastax.driver.core.ConsistencyLevel;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.data.cassandra.template.BatchAttributes;
import org.springframework.data.cassandra.template.CassandraTemplate;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;
import org.springframework.util.ReflectionUtils.MethodFilter;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Date: 18.03.14 16:20
 *
 * @author Alexandr V Solomatin
 */
final public class BatchMethodInterceptor implements MethodInterceptor, Ordered, MethodCallback {
    final private static Logger log = LoggerFactory.getLogger(BatchMethodInterceptor.class);

    final static private MethodFilter ANNOTATION_BATCH_FILTER = new MethodFilter() {
        @Override
        public boolean matches(Method method) {
            return method.isAnnotationPresent(Batch.class);
        }
    };

    final private BeanFactory beanFactory;

    final private Map<String, BatchInfo> batchInfoMap = new HashMap<>();

    public BatchMethodInterceptor(final Class<?> targetClass, final BeanFactory beanFactory) {
        this.beanFactory = beanFactory;

        if (log.isDebugEnabled()) {
            log.debug("Creating method interceptor for class " + targetClass.getCanonicalName());
            log.debug("Scanning for @Batch annotated methods");
        }

        ReflectionUtils.doWithMethods(targetClass, this, ANNOTATION_BATCH_FILTER);
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        final BatchInfo batchInfo = batchInfoMap.get(invocation.getMethod().getName());
        Assert.notNull(batchInfo);

        final CassandraTemplate cassandraTemplate = batchInfo.getCassandraTemplate();

        try {
            cassandraTemplate.startBatch(batchInfo.getBatchAttributes());
            final Object result = invocation.proceed();
            cassandraTemplate.applyBatch();

            return result;
        } catch (Throwable e) {
            cassandraTemplate.cancelBatch();
            throw e;
        }
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

    private CassandraTemplate determineCassandraTemplate(Batch ann) {
        if (StringUtils.hasLength(ann.value())) {
            return beanFactory.getBean(ann.value(), CassandraTemplate.class);
        } else if (beanFactory instanceof ListableBeanFactory) {
            return BeanFactoryUtils.beanOfTypeIncludingAncestors(((ListableBeanFactory)beanFactory), CassandraTemplate.class);
        }

        throw new IllegalStateException("Can't retrieve CassandraTemplate");
    }

    private BatchAttributes determineBatchAttributes(Batch ann) {
        final BatchAttributes batchAttributes = new BatchAttributes();

        if (ann.consistencyLevel() != ConsistencyLevel.ANY)
            batchAttributes.setConsistencyLevel(ann.consistencyLevel());

        if (ann.timestamp() != -1L)
            batchAttributes.setTimestamp(ann.timestamp());

        batchAttributes.setUnlogged(ann.unlogged());

        return batchAttributes;
    }

    @Override
    public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
        final Batch ann = method.getAnnotation(Batch.class);

        final BatchInfo batchInfo = new BatchInfo(determineCassandraTemplate(ann), determineBatchAttributes(ann));

        batchInfoMap.put(method.getName(), batchInfo);
    }

    final private class BatchInfo {
        final private CassandraTemplate cassandraTemplate;
        final private BatchAttributes batchAttributes;

        protected BatchInfo(CassandraTemplate cassandraTemplate, BatchAttributes batchAttributes) {
            this.cassandraTemplate = cassandraTemplate;
            this.batchAttributes = batchAttributes;
        }

        private CassandraTemplate getCassandraTemplate() {
            return cassandraTemplate;
        }

        private BatchAttributes getBatchAttributes() {
            return batchAttributes;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("BatchInfo{");
            sb.append("cassandraTemplate=").append(cassandraTemplate);
            sb.append(", batchAttributes=").append(batchAttributes);
            sb.append('}');
            return sb.toString();
        }
    }

}
