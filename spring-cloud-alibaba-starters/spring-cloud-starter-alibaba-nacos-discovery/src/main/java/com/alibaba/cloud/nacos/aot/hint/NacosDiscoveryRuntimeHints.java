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

package com.alibaba.cloud.nacos.aot.hint;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.cloud.nacos.discovery.NacosDiscoveryAutoConfiguration;
import com.alibaba.cloud.nacos.discovery.NacosServiceDiscovery;
import com.alibaba.cloud.nacos.registry.NacosRegistration;
import com.alibaba.cloud.nacos.registry.NacosServiceRegistry;
import org.jspecify.annotations.Nullable;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

/**
 * Native hints for Nacos Discovery registration and service lookup.
 *
 * @author heyoulin
 */
public class NacosDiscoveryRuntimeHints implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
		registerBeanStyleType(hints, NacosDiscoveryProperties.class);
		registerBeanStyleType(hints, NacosServiceManager.class);
		registerBeanStyleType(hints, NacosDiscoveryAutoConfiguration.class);
		registerBeanStyleType(hints, NacosServiceDiscovery.class);
		registerBeanStyleType(hints, NacosRegistration.class);
		registerBeanStyleType(hints, NacosServiceRegistry.class);
	}

	private void registerBeanStyleType(RuntimeHints hints, Class<?> type) {
		hints.reflection().registerType(type, MemberCategory.DECLARED_FIELDS,
				MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
				MemberCategory.INVOKE_DECLARED_METHODS,
				MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
				MemberCategory.INVOKE_PUBLIC_METHODS);
	}

}
