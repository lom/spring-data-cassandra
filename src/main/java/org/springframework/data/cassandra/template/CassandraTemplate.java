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
package org.springframework.data.cassandra.template;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Statement;

/**
 * Date: 27.12.13 12:05
 *
 * @author Alexandr V Solomatin
 */
public interface CassandraTemplate {

    ResultSet execute(String query);
    ResultSet execute(Statement statement);
    ResultSetFuture executeAsync(String query);
    ResultSetFuture executeAsync(Statement statement);

    void startBatch(BatchAttributes batchAttributes);
    void cancelBatch();
    void applyBatch();
    ResultSetFuture applyBatchAsync();

}
