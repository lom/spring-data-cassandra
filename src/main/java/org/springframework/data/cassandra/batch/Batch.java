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

import java.lang.annotation.*;

/**
 * Date: 18.03.14 15:59
 *
 * @author Alexandr V Solomatin
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Batch {
    /**
     * Cassandra session bean name
     *
     * @return
     */
    String value() default "";

    long timestamp() default -1L;

    ConsistencyLevel consistencyLevel() default ConsistencyLevel.ANY;

    boolean unlogged() default false;

}
