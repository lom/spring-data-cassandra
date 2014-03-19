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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.framework.ProxyConfig;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ClassUtils;

/**
 * Date: 18.03.14 16:08
 *
 * @author Alexandr V Solomatin
 */
final public class BatchAnnotationBeanPostProcessor extends ProxyConfig implements BeanPostProcessor,
        BeanClassLoaderAware, BeanFactoryAware {

    final private static Logger log = LoggerFactory.getLogger(BatchAnnotationBeanPostProcessor.class);

    final private Pointcut pointcut = new AnnotationMatchingPointcut(null, Batch.class);

    private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();
    private BeanFactory beanFactory;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof AopInfrastructureBean) {
            return bean;
        }

        final Class<?> targetClass = AopUtils.getTargetClass(bean);

        if (!AopUtils.canApply(pointcut, targetClass)) {
            return bean;
        }

        final BatchMethodInterceptor interceptor = new BatchMethodInterceptor(targetClass, beanFactory);
        final PointcutAdvisor advisor = new DefaultPointcutAdvisor(pointcut, interceptor);

        if (bean instanceof Advised) {
            if (log.isDebugEnabled()) {
                log.debug("Bean " + beanName + " is already proxied, adding Advisor to existing proxy");
            }

            ((Advised) bean).addAdvisor(0, advisor);
            return bean;
        }

        if (log.isDebugEnabled()) {
            log.debug("Proxying bean " + beanName + " of type " + targetClass.getCanonicalName());
        }

        final ProxyFactory proxyFactory = new ProxyFactory(bean);
        proxyFactory.copyFrom(this);
        proxyFactory.addAdvisor(advisor);

        return proxyFactory.getProxy(beanClassLoader);
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.beanClassLoader = classLoader;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

}
