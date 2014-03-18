/*
 * Copyright 2011-2013 the original author or authors.
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
package org.springframework.data.cassandra.core;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;

/**
 * Convenient factory for configuring a Cassandra Session.
 * Session is a thread safe singleton and created per a keyspace.
 * So, it is enough to have one session per application.
 * 
 * @author Alex Shvid
 */

public class CassandraSessionFactoryBean implements FactoryBean<Session>,
        InitializingBean, DisposableBean, PersistenceExceptionTranslator  {

	private static final Logger log = LoggerFactory.getLogger(CassandraSessionFactoryBean.class);

	private Cluster cluster;
	private Session session;
	private String keyspace;

    private final PersistenceExceptionTranslator exceptionTranslator = new CassandraExceptionTranslator();

	public Session getObject() throws Exception {
		return session;
	}
	
	public Class<? extends Session> getObjectType() {
		return Session.class;
	}

	public boolean isSingleton() {
		return true;
	}
	
	public DataAccessException translateExceptionIfPossible(RuntimeException ex) {
		return exceptionTranslator.translateExceptionIfPossible(ex);
	}
	
	public void afterPropertiesSet() throws Exception {
		if (cluster == null)
			throw new IllegalArgumentException("cluster is required");

        log.debug("opening new session");
		final Session session = keyspace == null ? cluster.connect() : cluster.connect(keyspace);

		// initialize property
		this.session = session;
	}

	public void destroy() throws Exception {
        log.debug("shutting down session");
		session.close();
	}

	public void setKeyspace(String keyspace) {
		this.keyspace = keyspace;
	}

	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}

}
