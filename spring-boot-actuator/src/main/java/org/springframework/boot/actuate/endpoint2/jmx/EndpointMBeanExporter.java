/*
 * Copyright 2012-2017 the original author or authors.
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

package org.springframework.boot.actuate.endpoint2.jmx;

import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.boot.actuate.endpoint.jmx.DataConverter;
import org.springframework.boot.actuate.endpoint2.EndpointDiscoverer;
import org.springframework.boot.actuate.endpoint2.EndpointInfo;
import org.springframework.context.SmartLifecycle;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.support.ObjectNameManager;

/**
 * {@link SmartLifecycle} bean that registers all known {@link EndpointInfo endpoints}
 * with an * {@link MBeanServer} using the {@link MBeanExporter} located from
 * the application * context.
 *
 * @author Stephane Nicoll
 * @since 2.0.0
 */
public class EndpointMBeanExporter extends MBeanExporter
		implements SmartLifecycle {

	/**
	 * The default JMX domain.
	 */
	public static final String DEFAULT_DOMAIN = "org.springframework.boot2";

	private static final Log logger = LogFactory.getLog(EndpointMBeanExporter.class);


	private final EndpointMBeanInfoAssembler assembler = new EndpointMBeanInfoAssembler();

	private volatile boolean autoStartup = true;

	private volatile int phase = 0;

	private volatile boolean running = false;

	private final ReentrantLock lifecycleLock = new ReentrantLock();

	private ListableBeanFactory beanFactory;

	private String domain = DEFAULT_DOMAIN;

	private Properties objectNameStaticProperties = new Properties();

	private final EndpointDiscoverer endpointDiscoverer;

	private final DataConverter dataConverter;

	/**
	 * Create a new {@link EndpointMBeanExporter} instance.
	 * @param objectMapper the object mapper
	 */
	public EndpointMBeanExporter(EndpointDiscoverer endpointDiscoverer,
			ObjectMapper objectMapper) {
		this.endpointDiscoverer = endpointDiscoverer;
		this.dataConverter = new DataConverter((objectMapper == null
				? new ObjectMapper() : objectMapper));
		setAutodetect(false);
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		super.setBeanFactory(beanFactory);
		if (beanFactory instanceof ListableBeanFactory) {
			this.beanFactory = (ListableBeanFactory) beanFactory;
		}
		else {
			logger.warn("EndpointMBeanExporter not running in a ListableBeanFactory: "
					+ "autodetection of Endpoints not available.");
		}
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	@Override
	public void setEnsureUniqueRuntimeObjectNames(
			boolean ensureUniqueRuntimeObjectNames) {
		super.setEnsureUniqueRuntimeObjectNames(ensureUniqueRuntimeObjectNames);
	}

	public void setObjectNameStaticProperties(Properties objectNameStaticProperties) {
		this.objectNameStaticProperties = objectNameStaticProperties;
	}

	protected void doStart() {
		registerEndpoints();
	}

	protected void registerEndpoints() {
		endpointDiscoverer.discoverEndpoints().forEach(endpointInfo -> {
			EndpointMBeanInfo endpointMBeanInfo = this.assembler.getEndpointMBeanInfo(endpointInfo);
			EndpointDynamicMBean mBean = new EndpointDynamicMBean(this.beanFactory, this.dataConverter, endpointMBeanInfo);
			this.registerManagedResource(mBean, getObjectName(endpointInfo));
		});
	}

	private ObjectName getObjectName(EndpointInfo endpoint) {
		try {
			StringBuilder builder = new StringBuilder();
			builder.append(this.domain);
			builder.append(":type=Endpoint");
			builder.append(",name=" + endpoint.getId());
			builder.append(getStaticNames());
			return ObjectNameManager.getInstance(builder.toString());
		}
		catch (MalformedObjectNameException ex) {
			throw new IllegalArgumentException("Failed to register endpoint", ex);
		}
	}

	private String getStaticNames() {
		if (this.objectNameStaticProperties.isEmpty()) {
			return "";
		}
		StringBuilder builder = new StringBuilder();

		for (Entry<Object, Object> name : this.objectNameStaticProperties.entrySet()) {
			builder.append("," + name.getKey() + "=" + name.getValue());
		}
		return builder.toString();
	}

	@Override
	public final int getPhase() {
		return this.phase;
	}

	@Override
	public final boolean isAutoStartup() {
		return this.autoStartup;
	}

	@Override
	public final boolean isRunning() {
		this.lifecycleLock.lock();
		try {
			return this.running;
		}
		finally {
			this.lifecycleLock.unlock();
		}
	}

	@Override
	public final void start() {
		this.lifecycleLock.lock();
		try {
			if (!this.running) {
				this.doStart();
				this.running = true;
			}
		}
		finally {
			this.lifecycleLock.unlock();
		}
	}

	@Override
	public final void stop() {
		this.lifecycleLock.lock();
		try {
			if (this.running) {
				this.running = false;
			}
		}
		finally {
			this.lifecycleLock.unlock();
		}
	}

	@Override
	public final void stop(Runnable callback) {
		this.lifecycleLock.lock();
		try {
			this.stop();
			callback.run();
		}
		finally {
			this.lifecycleLock.unlock();
		}
	}

}
