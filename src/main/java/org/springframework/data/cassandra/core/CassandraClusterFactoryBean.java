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

import com.datastax.driver.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.util.StringUtils;

import com.datastax.driver.core.ProtocolOptions.Compression;
import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.datastax.driver.core.policies.ReconnectionPolicy;
import com.datastax.driver.core.policies.RetryPolicy;

import java.util.Optional;

/**
 * Convenient factory for configuring a Cassandra Cluster.
 * 
 * @author Alex Shvid
 */

public class CassandraClusterFactoryBean implements FactoryBean<Cluster>,
		InitializingBean, DisposableBean, PersistenceExceptionTranslator {

    final private static Logger log = LoggerFactory.getLogger(CassandraClusterFactoryBean.class);

	private static final int DEFAULT_PORT = 9042;
	
	private Cluster cluster;
	
	private String contactPoints;
	private int port = DEFAULT_PORT;
	private Integer maxSchemaAgreementWaitSeconds;
	private Boolean allowBetaProtocolVersion;
	private ProtocolVersion protocolVersion;
	private LoadBalancingPolicy loadBalancingPolicy;
	private ReconnectionPolicy reconnectionPolicy;
	private RetryPolicy retryPolicy;
	private AuthProvider authProvider;
	private Compression compressionType;
	private boolean metricsEnabled = true;
	private PoolingOptions poolingOptions;
	private SocketOptions socketOptions;
	private QueryOptions queryOptions;
	private ThreadingOptions threadingOptions;
	private NettyOptions nettyOptions;

	private final PersistenceExceptionTranslator exceptionTranslator = new CassandraExceptionTranslator();

	public Cluster getObject() throws Exception {
		return cluster;
	}
	
	public Class<? extends Cluster> getObjectType() {
		return Cluster.class;
	}

	public boolean isSingleton() {
		return true;
	}
	
	public DataAccessException translateExceptionIfPossible(RuntimeException ex) {
		return exceptionTranslator.translateExceptionIfPossible(ex);
	}
	
	public void afterPropertiesSet() throws Exception {
        log.debug("building cluster for contact points={}, port={}...", contactPoints, port);
		
		if (!StringUtils.hasText(contactPoints)) {
			throw new IllegalArgumentException(
					"at least one server is required");
		}
		
		final Cluster.Builder builder = Cluster.builder();

		builder.addContactPoints(StringUtils.commaDelimitedListToStringArray(contactPoints)).withPort(port);

		Optional.ofNullable(maxSchemaAgreementWaitSeconds).ifPresent(builder::withMaxSchemaAgreementWaitSeconds);
		Optional.ofNullable(allowBetaProtocolVersion).ifPresent(b -> builder.allowBetaProtocolVersion());
        Optional.ofNullable(protocolVersion).ifPresent(builder::withProtocolVersion);
        Optional.ofNullable(loadBalancingPolicy).ifPresent(builder::withLoadBalancingPolicy);
        Optional.ofNullable(reconnectionPolicy).ifPresent(builder::withReconnectionPolicy);
        Optional.ofNullable(retryPolicy).ifPresent(builder::withRetryPolicy);
        Optional.ofNullable(authProvider).ifPresent(builder::withAuthProvider);
        Optional.ofNullable(compressionType).ifPresent(builder::withCompression);
        if (!metricsEnabled)
            builder.withoutMetrics();
        Optional.ofNullable(poolingOptions).ifPresent(builder::withPoolingOptions);
        Optional.ofNullable(socketOptions).ifPresent(builder::withSocketOptions);
        Optional.ofNullable(queryOptions).ifPresent(builder::withQueryOptions);
        Optional.ofNullable(threadingOptions).ifPresent(builder::withThreadingOptions);
        Optional.ofNullable(nettyOptions).ifPresent(builder::withNettyOptions);

        // initialize property
		this.cluster = builder.build();

        log.debug("building cluster for contact points={}, port={} ok", contactPoints, port);
	}

	public void destroy() throws Exception {
		this.cluster.close();
        log.debug("cluster {} shut down", contactPoints);
	}

	public void setContactPoints(String contactPoints) {
		this.contactPoints = contactPoints;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setMaxSchemaAgreementWaitSeconds(Integer maxSchemaAgreementWaitSeconds) {
		this.maxSchemaAgreementWaitSeconds = maxSchemaAgreementWaitSeconds;
	}

	public void setAllowBetaProtocolVersion(boolean allowBetaProtocolVersion) {
		this.allowBetaProtocolVersion = allowBetaProtocolVersion;
	}

	public void setProtocolVersion(ProtocolVersion protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

	public void setLoadBalancingPolicy(LoadBalancingPolicy loadBalancingPolicy) {
		this.loadBalancingPolicy = loadBalancingPolicy;
	}

	public void setReconnectionPolicy(ReconnectionPolicy reconnectionPolicy) {
		this.reconnectionPolicy = reconnectionPolicy;
	}

	public void setRetryPolicy(RetryPolicy retryPolicy) {
		this.retryPolicy = retryPolicy;
	}

	public void setAuthProvider(AuthProvider authProvider) {
		this.authProvider = authProvider;
	}

	public void setCompressionType(Compression compressionType) {
		this.compressionType = compressionType;
	}

	public void setMetricsEnabled(boolean metricsEnabled) {
		this.metricsEnabled = metricsEnabled;
	}

	public void setPoolingOptions(PoolingOptions poolingOptions) {
		this.poolingOptions = poolingOptions;
	}

	public void setSocketOptions(SocketOptions socketOptions) {
		this.socketOptions = socketOptions;
	}

	public void setQueryOptions(QueryOptions queryOptions) {
		this.queryOptions = queryOptions;
	}

	public void setThreadingOptions(ThreadingOptions threadingOptions) {
		this.threadingOptions = threadingOptions;
	}

	public void setNettyOptions(NettyOptions nettyOptions) {
		this.nettyOptions = nettyOptions;
	}
}
