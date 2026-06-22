/*
 * Copyright 2013-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.nacos;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.NamingService;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.alibaba.nacos.api.NacosFactory.createMaintainService;
import static com.alibaba.nacos.api.NacosFactory.createNamingService;

/**
 * @author yuhuangbin
 */
public class NacosServiceManager {

	private static final String AOT_PROCESSING_PROPERTY = "spring.cloud.alibaba.aot-processing";

	private static final Logger log = LoggerFactory.getLogger(NacosServiceManager.class);

	private @Nullable NacosDiscoveryProperties nacosDiscoveryProperties;

	private volatile @Nullable NamingService namingService;

	private volatile @Nullable NamingMaintainService namingMaintainService;

	public NamingService getNamingService() {
		if (Objects.isNull(this.namingService)) {
			NacosDiscoveryProperties properties = this.nacosDiscoveryProperties;
			if (properties == null) {
				throw new IllegalStateException("NacosDiscoveryProperties is not initialized");
			}
			buildNamingService(properties.getNacosProperties());
		}
		if (namingService == null) {
			throw new IllegalStateException("NamingService is not initialized");
		}
		return namingService;
	}

	@Deprecated
	public NamingService getNamingService(Properties properties) {
		if (Objects.isNull(this.namingService)) {
			buildNamingService(properties);
		}
		if (namingService == null) {
			throw new IllegalStateException("NamingService is not initialized");
		}
		return namingService;
	}

	public NamingMaintainService getNamingMaintainService(Properties properties) {
		if (Objects.isNull(namingMaintainService)) {
			buildNamingMaintainService(properties);
		}
		if (namingMaintainService == null) {
			throw new IllegalStateException("NamingMaintainService is not initialized");
		}
		return namingMaintainService;
	}

	public boolean isNacosDiscoveryInfoChanged(
			NacosDiscoveryProperties currentNacosDiscoveryPropertiesCache) {
		if (Objects.isNull(this.nacosDiscoveryProperties)
				|| this.nacosDiscoveryProperties.equals(currentNacosDiscoveryPropertiesCache)) {
			return false;
		}
		return true;
	}

	private NamingMaintainService buildNamingMaintainService(Properties properties) {
		if (Objects.isNull(namingMaintainService)) {
			synchronized (NacosServiceManager.class) {
				if (Objects.isNull(namingMaintainService)) {
					namingMaintainService = createNamingMaintainService(properties);
				}
			}
		}
		return namingMaintainService;
	}

	private NamingService buildNamingService(Properties properties) {
		if (Objects.isNull(namingService)) {
			synchronized (NacosServiceManager.class) {
				if (Objects.isNull(namingService)) {
					namingService = createNewNamingService(properties);
				}
			}
		}
		return namingService;
	}

	private NamingService createNewNamingService(Properties properties) {
		if (Boolean.getBoolean(AOT_PROCESSING_PROPERTY)) {
			log.debug("Using no-op Nacos NamingService during Spring AOT processing");
			return createAotNoopNamingService();
		}
		try {
			return createNamingService(properties);
		}
		catch (NacosException e) {
			throw new RuntimeException(e);
		}
	}

	private NamingService createAotNoopNamingService() {
		return (NamingService) Proxy.newProxyInstance(NamingService.class.getClassLoader(),
				new Class<?>[] { NamingService.class }, (proxy, method, args) -> {
					String methodName = method.getName();
					if ("toString".equals(methodName)) {
						return "AotNoopNacosNamingService";
					}
					if ("hashCode".equals(methodName)) {
						return System.identityHashCode(proxy);
					}
					if ("equals".equals(methodName)) {
						return proxy == args[0];
					}
					if ("getServerStatus".equals(methodName)) {
						return "UP";
					}
					Class<?> returnType = method.getReturnType();
					if (Void.TYPE == returnType) {
						return null;
					}
					if (Boolean.TYPE == returnType) {
						return false;
					}
					if (Integer.TYPE == returnType || Long.TYPE == returnType || Short.TYPE == returnType
							|| Byte.TYPE == returnType) {
						return 0;
					}
					if (Float.TYPE == returnType || Double.TYPE == returnType) {
						return 0.0;
					}
					if (List.class.isAssignableFrom(returnType)) {
						return Collections.emptyList();
					}
					if (Set.class.isAssignableFrom(returnType)) {
						return Collections.emptySet();
					}
					if (Map.class.isAssignableFrom(returnType)) {
						return Collections.emptyMap();
					}
					return null;
				});
	}

	private NamingMaintainService createNamingMaintainService(Properties properties) {
		try {
			return createMaintainService(properties);
		}
		catch (NacosException e) {
			throw new RuntimeException(e);
		}
	}

	public void nacosServiceShutDown() throws NacosException {
		if (Objects.nonNull(this.namingService)) {
			this.namingService.shutDown();
			this.namingService = null;
		}
		if (Objects.nonNull(this.namingMaintainService)) {
			this.namingMaintainService.shutDown();
			this.namingMaintainService = null;
		}
	}

	public void setNacosDiscoveryProperties(NacosDiscoveryProperties nacosDiscoveryProperties) {
		this.nacosDiscoveryProperties = nacosDiscoveryProperties;
	}
}
