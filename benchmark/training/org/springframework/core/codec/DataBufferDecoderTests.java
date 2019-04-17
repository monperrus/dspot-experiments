/**
 * Copyright 2002-2018 the original author or authors.
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
package org.springframework.core.codec;


import MimeTypeUtils.APPLICATION_JSON;
import MimeTypeUtils.TEXT_PLAIN;
import java.nio.charset.StandardCharsets;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;


/**
 *
 *
 * @author Sebastien Deleuze
 */
public class DataBufferDecoderTests extends AbstractDecoderTestCase<DataBufferDecoder> {
    private final byte[] fooBytes = "foo".getBytes(StandardCharsets.UTF_8);

    private final byte[] barBytes = "bar".getBytes(StandardCharsets.UTF_8);

    public DataBufferDecoderTests() {
        super(new DataBufferDecoder());
    }

    @Override
    @Test
    public void canDecode() {
        Assert.assertTrue(this.decoder.canDecode(ResolvableType.forClass(DataBuffer.class), TEXT_PLAIN));
        Assert.assertFalse(this.decoder.canDecode(ResolvableType.forClass(Integer.class), TEXT_PLAIN));
        Assert.assertTrue(this.decoder.canDecode(ResolvableType.forClass(DataBuffer.class), APPLICATION_JSON));
    }
}
