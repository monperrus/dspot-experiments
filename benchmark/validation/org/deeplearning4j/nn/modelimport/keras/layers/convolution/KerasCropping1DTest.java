/**
 * *****************************************************************************
 * Copyright (c) 2015-2018 Skymind, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ****************************************************************************
 */
package org.deeplearning4j.nn.modelimport.keras.layers.convolution;


import org.deeplearning4j.nn.modelimport.keras.config.Keras1LayerConfiguration;
import org.deeplearning4j.nn.modelimport.keras.config.Keras2LayerConfiguration;
import org.junit.Test;


/**
 *
 *
 * @author Max Pumperla
 */
public class KerasCropping1DTest {
    private final String LAYER_NAME = "cropping_1D_layer";

    private final int CROPPING = 2;

    private Keras1LayerConfiguration conf1 = new Keras1LayerConfiguration();

    private Keras2LayerConfiguration conf2 = new Keras2LayerConfiguration();

    @Test
    public void testCropping1DLayer() throws Exception {
        Integer keras1 = 1;
        Integer keras2 = 2;
        buildCroppingSingleDim1DLayer(conf1, keras1);
        buildCroppingSingleDim1DLayer(conf2, keras2);
    }
}
