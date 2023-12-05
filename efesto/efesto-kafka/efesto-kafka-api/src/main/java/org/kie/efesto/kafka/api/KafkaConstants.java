/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.efesto.kafka.api;

public class KafkaConstants {

    public final static String BOOTSTRAP_SERVERS =
            "localhost:9092,localhost:9093,localhost:9094";

    public final static String COMPILE_TOPIC = "compile-topic";

    public final static String COMPILED_TOPIC = "compiled-topic";

    public final static String ASK_COMPILED_TOPIC = "ask-compiled-topic";

    public final static String ALREADY_COMPILED_TOPIC = "already-compiled-topic";

    public final static String COMPILATIONSERVICE_DISCOVER_TOPIC = "compilationservice_discover_topic";

    public final static String COMPILATIONSERVICE_NOTIFICATION_TOPIC = "compilationservice_notification_topic";

    public final static String COMPILATIONSERVICE_SOURCEREQUEST_TOPIC = "compilationservice_sourcerequest_topic";

    public final static String COMPILATIONSERVICE_SOURCERESPONSE_TOPIC = "compilationservice_sourceresponse_topic";

    public final static String EVALUATE_TOPIC = "evaluate-topic";

    public final static String EVALUATED_TOPIC = "evaluated-topic";

    public final static String RUNTIMESERVICE_DISCOVER_TOPIC = "runtimeservice_discover_topic";

    public final static String RUNTIMESERVICE_NOTIFICATION_TOPIC = "runtimeservice_notification_topic";

    public final static String RUNTIMESERVICE_EVALUATEINPUTREQUEST_TOPIC = "runtimeservice_evaluateinputrequest_topic";

    public final static String RUNTIMESERVICE_EVALUATEINPUTRESPONSE_TOPIC = "runtimeservice_evaluateinputresponse_topic";


}
