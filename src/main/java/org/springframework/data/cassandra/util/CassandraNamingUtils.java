/*
 * Copyright 2011 the original author or authors.
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
package org.springframework.data.cassandra.util;

import com.google.common.base.CaseFormat;

/**
 * Helper class featuring helper methods for working with Cassandra tables.
 * Mainly intended for internal use within the framework.
 * 
 * @author Alex Shvid
 */
final public class CassandraNamingUtils {

	/**
	 * Private constructor to prevent instantiation.
	 */
	private CassandraNamingUtils() {}
	
	/**
	 * Obtains the table name to use for the provided class.
     * Converts upper-camel org.springframework.data.cassandra.entity name to lower-underscore format.
	 * 
	 * @param entityClass The class to determine the preferred table name for
	 * @return The preferred collection name
	 */
	public static String guessTableName(Class<?> entityClass) {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, entityClass.getSimpleName());
	}

    /**
     * Converts upper-camel field name to lower-underscore format.
     *
     * @param fieldName
     * @return
     */
    public static String guessColumnName(String fieldName) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, fieldName);
    }

}
