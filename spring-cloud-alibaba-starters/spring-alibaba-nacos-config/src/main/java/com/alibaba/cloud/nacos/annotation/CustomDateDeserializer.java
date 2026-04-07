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

package com.alibaba.cloud.nacos.annotation;

import java.text.SimpleDateFormat;
import java.util.Date;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

public class CustomDateDeserializer extends ValueDeserializer<Date> {

	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public CustomDateDeserializer() {
		super();
	}

	@Override
	public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws JacksonException {
		JsonNode node = jsonParser.objectReadContext().readTree(jsonParser);
		String date = node.stringValue();
		try {
			return dateFormat.parse(date);
		} catch (Exception e) {
			throw JacksonException.wrapWithPath(e, new JacksonException.Reference("Invalid date format", 0));
		}
	}
}
