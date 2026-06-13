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

package com.alibaba.cloud.sentinel.custom;

import com.alibaba.cloud.sentinel.SentinelProperties;
import com.alibaba.cloud.sentinel.datasource.RuleType;
import com.alibaba.cloud.sentinel.datasource.config.AbstractDataSourceProperties;
import com.alibaba.cloud.sentinel.datasource.config.FileDataSourceProperties;
import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Test cases for {@link SentinelDataSourceHandler}.
 *
 * @author <a href="mailto:i@5icodes.com">hnyyghk</a>
 */
public class SentinelDataSourceHandlerTests {

	private SentinelDataSourceHandler sentinelDataSourceHandler;

	private DefaultListableBeanFactory beanFactory;

	private SentinelProperties sentinelProperties;

	private Environment env;

	@Before
	public void setUp() {
		beanFactory = mock(DefaultListableBeanFactory.class);
		sentinelProperties = mock(SentinelProperties.class);
		env = mock(Environment.class);
		sentinelDataSourceHandler = new SentinelDataSourceHandler(beanFactory, sentinelProperties, env);
	}

	/**
	 * Test cases for {@link SentinelDataSourceHandler#parseBeanDefinition(AbstractDataSourceProperties, String)}.
	 *
	 * @see com.alibaba.cloud.sentinel.datasource.config.FileDataSourceProperties
	 * @see com.alibaba.cloud.sentinel.datasource.factorybean.FileRefreshableDataSourceFactoryBean
	 */
	@Test
	public void testParseBeanDefinition() {
		FileDataSourceProperties dataSourceProperties = new FileDataSourceProperties();
		dataSourceProperties.setFile("classpath:flowrule.json");
		dataSourceProperties.setDataType("json");
		dataSourceProperties.setRuleType(RuleType.FLOW);
		String dataSourceName = "ds1" + "-sentinel-" + "file" + "-datasource";

		BeanDefinitionBuilder builder = sentinelDataSourceHandler.parseBeanDefinition(dataSourceProperties, dataSourceName);
		MutablePropertyValues propertyValues = builder.getBeanDefinition().getPropertyValues();

		assertThat(propertyValues.size()).isEqualTo(5);
		assertThat(propertyValues).noneMatch(propertyValue -> "$jacocoData".equals(propertyValue.getName()));
		assertThat(propertyValues).anyMatch(propertyValue -> "file".equals(propertyValue.getName())
				&& dataSourceProperties.getFile().equals(propertyValue.getValue()));
		assertThat(propertyValues).anyMatch(propertyValue -> "converter".equals(propertyValue.getName())
				&& propertyValue.getValue() instanceof RuntimeBeanReference value
				&& value.getBeanName().equals("sentinel-" + dataSourceProperties.getDataType() + "-" + dataSourceProperties.getRuleType().getName() + "-converter"));
	}

}
