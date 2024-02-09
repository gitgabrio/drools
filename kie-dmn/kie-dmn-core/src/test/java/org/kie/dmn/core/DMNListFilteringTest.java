/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.dmn.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNMessage;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.dmn.api.core.DMNType;
import org.kie.dmn.api.core.FEELPropertyAccessible;
import org.kie.dmn.api.core.ast.ItemDefNode;
import org.kie.dmn.core.api.DMNFactory;
import org.kie.dmn.core.compiler.DMNTypeRegistry;
import org.kie.dmn.core.impl.CompositeTypeImpl;
import org.kie.dmn.core.impl.DMNContextFPAImpl;
import org.kie.dmn.core.impl.DMNModelImpl;
import org.kie.dmn.core.impl.SimpleTypeImpl;
import org.kie.dmn.core.util.DMNRuntimeUtil;
import org.kie.dmn.feel.lang.EvaluationContext;
import org.kie.dmn.feel.lang.impl.EvaluationContextImpl;
import org.kie.dmn.feel.lang.types.AliasFEELType;
import org.kie.dmn.feel.lang.types.BuiltInType;
import org.kie.dmn.feel.util.ClassLoaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.kie.dmn.core.util.DynamicTypeUtils.entry;
import static org.kie.dmn.core.util.DynamicTypeUtils.mapOf;

public class DMNListFilteringTest extends BaseVariantTest {

    public static final Logger LOG = LoggerFactory.getLogger(DMNListFilteringTest.class);

    public DMNListFilteringTest(VariantTestConf testConfig) {
        super(testConfig);
    }

    @BeforeClass
    public static void setup() {

    }

    @Test
    public void testEvaluateAll() {
        final DMNRuntime runtime = createRuntime("list_filtering.dmn", this.getClass());
        final DMNModel dmnModel = runtime.getModel("https://kiegroup.org/dmn/_BDB6D3FE-834D-483B-A8DD-AB6DBA9BAD75",
                                                   "list_filtering");
        assertThat(dmnModel).isNotNull();

        final DMNContext context = runtime.newContext();

        List<Map<String, Object>> reports = IntStream.rangeClosed(1, 10)
                .mapToObj(i -> {
                    Map<String, Object> report = new HashMap<>();
                    report.put("confirmed", i % 2 == 0);
                    report.put("value", Integer.toString(i));
                    return report;
                })
                .toList();

        context.set("reports", reports);

        final DMNResult evaluateAll = evaluateModel(runtime, dmnModel, context);
        LOG.debug("{}", evaluateAll);
        assertThat(evaluateAll.hasErrors()).isFalse();
    }

}
