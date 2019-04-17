/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.kafka.connect.runtime.rest.util;


import CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import DistributedConfig.CONFIG_TOPIC_CONFIG;
import DistributedConfig.GROUP_ID_CONFIG;
import DistributedConfig.OFFSET_STORAGE_TOPIC_CONFIG;
import DistributedConfig.STATUS_STORAGE_TOPIC_CONFIG;
import SslConfigs.DEFAULT_SSL_ENABLED_PROTOCOLS;
import SslConfigs.DEFAULT_SSL_KEYMANGER_ALGORITHM;
import SslConfigs.DEFAULT_SSL_KEYSTORE_TYPE;
import SslConfigs.DEFAULT_SSL_PROTOCOL;
import SslConfigs.DEFAULT_SSL_TRUSTMANAGER_ALGORITHM;
import SslConfigs.DEFAULT_SSL_TRUSTSTORE_TYPE;
import StandaloneConfig.OFFSET_STORAGE_FILE_FILENAME_CONFIG;
import WorkerConfig.INTERNAL_KEY_CONVERTER_CLASS_CONFIG;
import WorkerConfig.INTERNAL_VALUE_CONVERTER_CLASS_CONFIG;
import WorkerConfig.KEY_CONVERTER_CLASS_CONFIG;
import WorkerConfig.VALUE_CONVERTER_CLASS_CONFIG;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.connect.runtime.distributed.DistributedConfig;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.Assert;
import org.junit.Test;


@SuppressWarnings("deprecation")
public class SSLUtilsTest {
    private static final Map<String, String> DEFAULT_CONFIG = new HashMap<>();

    static {
        // The WorkerConfig base class has some required settings without defaults
        SSLUtilsTest.DEFAULT_CONFIG.put(STATUS_STORAGE_TOPIC_CONFIG, "status-topic");
        SSLUtilsTest.DEFAULT_CONFIG.put(CONFIG_TOPIC_CONFIG, "config-topic");
        SSLUtilsTest.DEFAULT_CONFIG.put(BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        SSLUtilsTest.DEFAULT_CONFIG.put(GROUP_ID_CONFIG, "connect-test-group");
        SSLUtilsTest.DEFAULT_CONFIG.put(KEY_CONVERTER_CLASS_CONFIG, "org.apache.kafka.connect.json.JsonConverter");
        SSLUtilsTest.DEFAULT_CONFIG.put(VALUE_CONVERTER_CLASS_CONFIG, "org.apache.kafka.connect.json.JsonConverter");
        SSLUtilsTest.DEFAULT_CONFIG.put(INTERNAL_KEY_CONVERTER_CLASS_CONFIG, "org.apache.kafka.connect.json.JsonConverter");
        SSLUtilsTest.DEFAULT_CONFIG.put(INTERNAL_VALUE_CONVERTER_CLASS_CONFIG, "org.apache.kafka.connect.json.JsonConverter");
        SSLUtilsTest.DEFAULT_CONFIG.put(OFFSET_STORAGE_TOPIC_CONFIG, "connect-offsets");
    }

    @Test
    public void testGetOrDefault() {
        String existingKey = "exists";
        String missingKey = "missing";
        String value = "value";
        String defaultValue = "default";
        Map<String, Object> map = new HashMap<>();
        map.put("exists", "value");
        Assert.assertEquals(SSLUtils.getOrDefault(map, existingKey, defaultValue), value);
        Assert.assertEquals(SSLUtils.getOrDefault(map, missingKey, defaultValue), defaultValue);
    }

    @Test
    public void testCreateSslContextFactory() {
        Map<String, String> configMap = new HashMap<>(SSLUtilsTest.DEFAULT_CONFIG);
        configMap.put("ssl.keystore.location", "/path/to/keystore");
        configMap.put("ssl.keystore.password", "123456");
        configMap.put("ssl.key.password", "123456");
        configMap.put("ssl.truststore.location", "/path/to/truststore");
        configMap.put("ssl.truststore.password", "123456");
        configMap.put("ssl.provider", "SunJSSE");
        configMap.put("ssl.cipher.suites", "SSL_RSA_WITH_RC4_128_SHA,SSL_RSA_WITH_RC4_128_MD5");
        configMap.put("ssl.secure.random.implementation", "SHA1PRNG");
        configMap.put("ssl.client.auth", "required");
        configMap.put("ssl.endpoint.identification.algorithm", "HTTPS");
        configMap.put("ssl.keystore.type", "JKS");
        configMap.put("ssl.protocol", "TLS");
        configMap.put("ssl.truststore.type", "JKS");
        configMap.put("ssl.enabled.protocols", "TLSv1.2,TLSv1.1,TLSv1");
        configMap.put("ssl.keymanager.algorithm", "SunX509");
        configMap.put("ssl.trustmanager.algorithm", "PKIX");
        DistributedConfig config = new DistributedConfig(configMap);
        SslContextFactory ssl = SSLUtils.createSslContextFactory(config);
        Assert.assertEquals("file:///path/to/keystore", ssl.getKeyStorePath());
        Assert.assertEquals("file:///path/to/truststore", ssl.getTrustStorePath());
        Assert.assertEquals("SunJSSE", ssl.getProvider());
        Assert.assertArrayEquals(new String[]{ "SSL_RSA_WITH_RC4_128_SHA", "SSL_RSA_WITH_RC4_128_MD5" }, ssl.getIncludeCipherSuites());
        Assert.assertEquals("SHA1PRNG", ssl.getSecureRandomAlgorithm());
        Assert.assertTrue(ssl.getNeedClientAuth());
        Assert.assertEquals("JKS", ssl.getKeyStoreType());
        Assert.assertEquals("JKS", ssl.getTrustStoreType());
        Assert.assertEquals("TLS", ssl.getProtocol());
        Assert.assertArrayEquals(new String[]{ "TLSv1.2", "TLSv1.1", "TLSv1" }, ssl.getIncludeProtocols());
        Assert.assertEquals("SunX509", ssl.getKeyManagerFactoryAlgorithm());
        Assert.assertEquals("PKIX", ssl.getTrustManagerFactoryAlgorithm());
    }

    @Test
    public void testCreateSslContextFactoryDefaultValues() {
        Map<String, String> configMap = new HashMap<>(SSLUtilsTest.DEFAULT_CONFIG);
        configMap.put(OFFSET_STORAGE_FILE_FILENAME_CONFIG, "/tmp/offset/file");
        configMap.put(KEY_CONVERTER_CLASS_CONFIG, "org.apache.kafka.connect.json.JsonConverter");
        configMap.put(VALUE_CONVERTER_CLASS_CONFIG, "org.apache.kafka.connect.json.JsonConverter");
        configMap.put(INTERNAL_KEY_CONVERTER_CLASS_CONFIG, "org.apache.kafka.connect.json.JsonConverter");
        configMap.put(INTERNAL_VALUE_CONVERTER_CLASS_CONFIG, "org.apache.kafka.connect.json.JsonConverter");
        configMap.put("ssl.keystore.location", "/path/to/keystore");
        configMap.put("ssl.keystore.password", "123456");
        configMap.put("ssl.key.password", "123456");
        configMap.put("ssl.truststore.location", "/path/to/truststore");
        configMap.put("ssl.truststore.password", "123456");
        configMap.put("ssl.provider", "SunJSSE");
        configMap.put("ssl.cipher.suites", "SSL_RSA_WITH_RC4_128_SHA,SSL_RSA_WITH_RC4_128_MD5");
        configMap.put("ssl.secure.random.implementation", "SHA1PRNG");
        DistributedConfig config = new DistributedConfig(configMap);
        SslContextFactory ssl = SSLUtils.createSslContextFactory(config);
        Assert.assertEquals(DEFAULT_SSL_KEYSTORE_TYPE, ssl.getKeyStoreType());
        Assert.assertEquals(DEFAULT_SSL_TRUSTSTORE_TYPE, ssl.getTrustStoreType());
        Assert.assertEquals(DEFAULT_SSL_PROTOCOL, ssl.getProtocol());
        Assert.assertArrayEquals(Arrays.asList(DEFAULT_SSL_ENABLED_PROTOCOLS.split("\\s*,\\s*")).toArray(), ssl.getIncludeProtocols());
        Assert.assertEquals(DEFAULT_SSL_KEYMANGER_ALGORITHM, ssl.getKeyManagerFactoryAlgorithm());
        Assert.assertEquals(DEFAULT_SSL_TRUSTMANAGER_ALGORITHM, ssl.getTrustManagerFactoryAlgorithm());
        Assert.assertFalse(ssl.getNeedClientAuth());
        Assert.assertFalse(ssl.getWantClientAuth());
    }
}
