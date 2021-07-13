/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.pmml.clustering.tests;

import java.util.Arrays;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class SingleIrisKMeansClusteringTest extends AbstractSingleIrisKMeansClusteringTest {

    private static final String FILE_NAME = "SingleIrisKMeansClustering.pmml";

    public SingleIrisKMeansClusteringTest(
            double sepalLength, double sepalWidth, double petalLength, double petalWidth, String irisClass,
            double outNormcontinuousField, String predictedDisplayValue, int predictedEntityId, double predictedAffinity) {
        super(sepalLength, sepalWidth, petalLength, petalWidth, irisClass, outNormcontinuousField, predictedDisplayValue, predictedEntityId, predictedAffinity);
    }

    @BeforeClass
    public static void setupClass() {
        pmmlRuntime = getPMMLRuntime(FILE_NAME);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {4.4, 3.0, 1.3, 0.2, "3", 4.966666666666667, "cluster_3", 3, 0.5707919999999993},
                {5.0, 3.3, 1.4, 0.2, "3", 5.433333333333334, "cluster_3", 3, 0.019992000000000173},
                {7.0, 3.2, 4.7, 1.4, "2", 6.950000000000001, "cluster_2", 2, 0.7601784651992836},
                {5.7, 2.8, 4.1, 1.3, "4", 5.937500000000001, "cluster_4", 4, 0.09263374485596694},
                {6.3, 3.3, 6.0, 2.5, "1", 6.1625, "cluster_1", 1, 0.5745800781250017},
                {6.7, 3.0, 5.2, 2.3, "1", 6.575, "cluster_1", 1, 0.5020800781249984}
        });
    }

}
