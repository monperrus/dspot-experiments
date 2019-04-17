/**
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.crypto;


import java.math.BigInteger;
import java.security.SecureRandom;
import org.ethereum.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.AsymmetricCipherKeyPair;
import org.spongycastle.crypto.BufferedBlockCipher;
import org.spongycastle.crypto.KeyEncoder;
import org.spongycastle.crypto.KeyGenerationParameters;
import org.spongycastle.crypto.agreement.ECDHBasicAgreement;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.engines.AESEngine;
import org.spongycastle.crypto.engines.IESEngine;
import org.spongycastle.crypto.generators.ECKeyPairGenerator;
import org.spongycastle.crypto.generators.EphemeralKeyPairGenerator;
import org.spongycastle.crypto.modes.SICBlockCipher;
import org.spongycastle.util.encoders.Hex;

import static ECKey.CURVE;


public class CryptoTest {
    private static final Logger log = LoggerFactory.getLogger("test");

    @Test
    public void test1() {
        byte[] result = HashUtil.sha3("horse".getBytes());
        Assert.assertEquals("c87f65ff3f271bf5dc8643484f66b200109caffe4bf98c4cb393dc35740b28c0", Hex.toHexString(result));
        result = HashUtil.sha3("cow".getBytes());
        Assert.assertEquals("c85ef7d79691fe79573b1a7064c19c1a9819ebdbd1faaab1a8ec92344438aaf4", Hex.toHexString(result));
    }

    @Test
    public void test3() {
        BigInteger privKey = new BigInteger("cd244b3015703ddf545595da06ada5516628c5feadbf49dc66049c4b370cc5d8", 16);
        byte[] addr = ECKey.fromPrivate(privKey).getAddress();
        Assert.assertEquals("89b44e4d3c81ede05d0f5de8d1a68f754d73d997", Hex.toHexString(addr));
    }

    @Test
    public void test4() {
        byte[] cowBytes = HashUtil.sha3("cow".getBytes());
        byte[] addr = ECKey.fromPrivate(cowBytes).getAddress();
        Assert.assertEquals("CD2A3D9F938E13CD947EC05ABC7FE734DF8DD826", Hex.toHexString(addr).toUpperCase());
    }

    @Test
    public void test5() {
        byte[] horseBytes = HashUtil.sha3("horse".getBytes());
        byte[] addr = ECKey.fromPrivate(horseBytes).getAddress();
        Assert.assertEquals("13978AEE95F38490E9769C39B2773ED763D9CD5F", Hex.toHexString(addr).toUpperCase());
    }

    /* performance test */
    @Test
    public void test6() {
        long firstTime = System.currentTimeMillis();
        System.out.println(firstTime);
        for (int i = 0; i < 1000; ++i) {
            byte[] horseBytes = HashUtil.sha3("horse".getBytes());
            byte[] addr = ECKey.fromPrivate(horseBytes).getAddress();
            Assert.assertEquals("13978AEE95F38490E9769C39B2773ED763D9CD5F", Hex.toHexString(addr).toUpperCase());
        }
        long secondTime = System.currentTimeMillis();
        System.out.println(secondTime);
        System.out.println(((secondTime - firstTime) + " millisec"));
        // 1) result: ~52 address calculation every second
    }

    /* real tx hash calc */
    @Test
    public void test7() {
        String txRaw = "F89D80809400000000000000000000000000000000000000008609184E72A000822710B3606956330C0D630000003359366000530A0D630000003359602060005301356000533557604060005301600054630000000C5884336069571CA07F6EB94576346488C6253197BDE6A7E59DDC36F2773672C849402AA9C402C3C4A06D254E662BF7450DD8D835160CBB053463FED0B53F2CDD7F3EA8731919C8E8CC";
        byte[] txHashB = HashUtil.sha3(Hex.decode(txRaw));
        String txHash = Hex.toHexString(txHashB);
        Assert.assertEquals("4b7d9670a92bf120d5b43400543b69304a14d767cf836a7f6abff4edde092895", txHash);
    }

    /* real block hash calc */
    @Test
    public void test8() {
        String blockRaw = "F885F8818080A01DCC4DE8DEC75D7AAB85B567B6CCD41AD312451B948A7413F0A142FD40D49347940000000000000000000000000000000000000000A0BCDDD284BF396739C224DBA0411566C891C32115FEB998A3E2B4E61F3F35582AA01DCC4DE8DEC75D7AAB85B567B6CCD41AD312451B948A7413F0A142FD40D4934783800000808080C0C0";
        byte[] blockHashB = HashUtil.sha3(Hex.decode(blockRaw));
        String blockHash = Hex.toHexString(blockHashB);
        System.out.println(blockHash);
    }

    @Test
    public void test9() {
        // TODO: https://tools.ietf.org/html/rfc6979#section-2.2
        // TODO: https://github.com/bcgit/bc-java/blob/master/core/src/main/java/org/bouncycastle/crypto/signers/ECDSASigner.java
        System.out.println(new BigInteger(Hex.decode("3913517ebd3c0c65000000")));
        System.out.println(Utils.getValueShortString(new BigInteger("69000000000000000000000000")));
    }

    @Test
    public void test10() {
        BigInteger privKey = new BigInteger("74ef8a796480dda87b4bc550b94c408ad386af0f65926a392136286784d63858", 16);
        byte[] addr = ECKey.fromPrivate(privKey).getAddress();
        Assert.assertEquals("ba73facb4f8291f09f27f90fe1213537b910065e", Hex.toHexString(addr));
    }

    // basic encryption/decryption
    @Test
    public void test11() throws Throwable {
        byte[] keyBytes = HashUtil.sha3("...".getBytes());
        CryptoTest.log.info("key: {}", Hex.toHexString(keyBytes));
        byte[] ivBytes = new byte[16];
        byte[] payload = Hex.decode("22400891000000000000000000000000");
        KeyParameter key = new KeyParameter(keyBytes);
        ParametersWithIV params = new ParametersWithIV(key, new byte[16]);
        AESEngine engine = new AESEngine();
        SICBlockCipher ctrEngine = new SICBlockCipher(engine);
        ctrEngine.init(true, params);
        byte[] cipher = new byte[16];
        ctrEngine.processBlock(payload, 0, cipher, 0);
        CryptoTest.log.info("cipher: {}", Hex.toHexString(cipher));
        byte[] output = new byte[cipher.length];
        ctrEngine.init(false, params);
        ctrEngine.processBlock(cipher, 0, output, 0);
        Assert.assertEquals(Hex.toHexString(output), Hex.toHexString(payload));
        CryptoTest.log.info("original: {}", Hex.toHexString(payload));
    }

    // big packet encryption
    @Test
    public void test12() throws Throwable {
        AESEngine engine = new AESEngine();
        SICBlockCipher ctrEngine = new SICBlockCipher(engine);
        byte[] keyBytes = Hex.decode("a4627abc2a3c25315bff732cb22bc128f203912dd2a840f31e66efb27a47d2b1");
        byte[] ivBytes = new byte[16];
        byte[] payload = Hex.decode("0109efc76519b683d543db9d0991bcde99cc9a3d14b1d0ecb8e9f1f66f31558593d746eaa112891b04ef7126e1dce17c9ac92ebf39e010f0028b8ec699f56f5d0c0d00");
        byte[] cipherText = Hex.decode("f9fab4e9dd9fc3e5d0d0d16da254a2ac24df81c076e3214e2c57da80a46e6ae4752f4b547889fa692b0997d74f36bb7c047100ba71045cb72cfafcc7f9a251762cdf8f");
        KeyParameter key = new KeyParameter(keyBytes);
        ParametersWithIV params = new ParametersWithIV(key, ivBytes);
        ctrEngine.init(true, params);
        byte[] in = payload;
        byte[] out = new byte[in.length];
        int i = 0;
        while (i < (in.length)) {
            ctrEngine.processBlock(in, i, out, i);
            i += engine.getBlockSize();
            if (((in.length) - i) < (engine.getBlockSize()))
                break;

        } 
        // process left bytes
        if (((in.length) - i) > 0) {
            byte[] tmpBlock = new byte[16];
            System.arraycopy(in, i, tmpBlock, 0, ((in.length) - i));
            ctrEngine.processBlock(tmpBlock, 0, tmpBlock, 0);
            System.arraycopy(tmpBlock, 0, out, i, ((in.length) - i));
        }
        CryptoTest.log.info("cipher: {}", Hex.toHexString(out));
        Assert.assertEquals(Hex.toHexString(cipherText), Hex.toHexString(out));
    }

    // cpp keys demystified
    @Test
    public void test13() throws Throwable {
        // us.secret() a4627abc2a3c25315bff732cb22bc128f203912dd2a840f31e66efb27a47d2b1
        // us.public() caa3d5086b31529bb00207eabf244a0a6c54d807d2ac0ec1f3b1bdde0dbf8130c115b1eaf62ce0f8062bcf70c0fefbc97cec79e7faffcc844a149a17fcd7bada
        // us.address() 47d8cb63a7965d98b547b9f0333a654b60ffa190
        ECKey key = ECKey.fromPrivate(Hex.decode("a4627abc2a3c25315bff732cb22bc128f203912dd2a840f31e66efb27a47d2b1"));
        String address = Hex.toHexString(key.getAddress());
        String pubkey = Hex.toHexString(/* uncompressed form */
        key.getPubKeyPoint().getEncoded(false));
        CryptoTest.log.info(("address: " + address));
        CryptoTest.log.info(("pubkey: " + pubkey));
        Assert.assertEquals("47d8cb63a7965d98b547b9f0333a654b60ffa190", address);
        Assert.assertEquals("04caa3d5086b31529bb00207eabf244a0a6c54d807d2ac0ec1f3b1bdde0dbf8130c115b1eaf62ce0f8062bcf70c0fefbc97cec79e7faffcc844a149a17fcd7bada", pubkey);
    }

    // ECIES_AES128_SHA256 + No Ephemeral Key + IV(all zeroes)
    @Test
    public void test14() throws Throwable {
        AESEngine aesFastEngine = new AESEngine();
        IESEngine iesEngine = new IESEngine(new ECDHBasicAgreement(), new org.spongycastle.crypto.generators.KDF2BytesGenerator(new SHA256Digest()), new org.spongycastle.crypto.macs.HMac(new SHA256Digest()), new BufferedBlockCipher(new SICBlockCipher(aesFastEngine)));
        byte[] d = new byte[]{ 1, 2, 3, 4, 5, 6, 7, 8 };
        byte[] e = new byte[]{ 8, 7, 6, 5, 4, 3, 2, 1 };
        IESParameters p = new IESWithCipherParameters(d, e, 64, 128);
        ParametersWithIV parametersWithIV = new ParametersWithIV(p, new byte[16]);
        ECKeyPairGenerator eGen = new ECKeyPairGenerator();
        KeyGenerationParameters gParam = new ECKeyGenerationParameters(CURVE, new SecureRandom());
        eGen.init(gParam);
        AsymmetricCipherKeyPair p1 = eGen.generateKeyPair();
        AsymmetricCipherKeyPair p2 = eGen.generateKeyPair();
        ECKeyGenerationParameters keygenParams = new ECKeyGenerationParameters(CURVE, new SecureRandom());
        ECKeyPairGenerator generator = new ECKeyPairGenerator();
        generator.init(keygenParams);
        ECKeyPairGenerator gen = new ECKeyPairGenerator();
        gen.init(new ECKeyGenerationParameters(CURVE, new SecureRandom()));
        iesEngine.init(true, p1.getPrivate(), p2.getPublic(), parametersWithIV);
        byte[] message = Hex.decode("010101");
        CryptoTest.log.info("payload: {}", Hex.toHexString(message));
        byte[] cipher = iesEngine.processBlock(message, 0, message.length);
        CryptoTest.log.info("cipher: {}", Hex.toHexString(cipher));
        IESEngine decryptorIES_Engine = new IESEngine(new ECDHBasicAgreement(), new org.spongycastle.crypto.generators.KDF2BytesGenerator(new SHA256Digest()), new org.spongycastle.crypto.macs.HMac(new SHA256Digest()), new BufferedBlockCipher(new SICBlockCipher(aesFastEngine)));
        decryptorIES_Engine.init(false, p2.getPrivate(), p1.getPublic(), parametersWithIV);
        byte[] orig = decryptorIES_Engine.processBlock(cipher, 0, cipher.length);
        CryptoTest.log.info(("orig: " + (Hex.toHexString(orig))));
    }

    // ECIES_AES128_SHA256 + Ephemeral Key + IV(all zeroes)
    @Test
    public void test15() throws Throwable {
        byte[] privKey = Hex.decode("a4627abc2a3c25315bff732cb22bc128f203912dd2a840f31e66efb27a47d2b1");
        ECKey ecKey = ECKey.fromPrivate(privKey);
        ECPrivateKeyParameters ecPrivKey = new ECPrivateKeyParameters(ecKey.getPrivKey(), CURVE);
        ECPublicKeyParameters ecPubKey = new ECPublicKeyParameters(ecKey.getPubKeyPoint(), CURVE);
        AsymmetricCipherKeyPair myKey = new AsymmetricCipherKeyPair(ecPubKey, ecPrivKey);
        AESEngine aesFastEngine = new AESEngine();
        IESEngine iesEngine = new IESEngine(new ECDHBasicAgreement(), new org.spongycastle.crypto.generators.KDF2BytesGenerator(new SHA256Digest()), new org.spongycastle.crypto.macs.HMac(new SHA256Digest()), new BufferedBlockCipher(new SICBlockCipher(aesFastEngine)));
        byte[] d = new byte[]{ 1, 2, 3, 4, 5, 6, 7, 8 };
        byte[] e = new byte[]{ 8, 7, 6, 5, 4, 3, 2, 1 };
        IESParameters p = new IESWithCipherParameters(d, e, 64, 128);
        ParametersWithIV parametersWithIV = new ParametersWithIV(p, new byte[16]);
        ECKeyPairGenerator eGen = new ECKeyPairGenerator();
        KeyGenerationParameters gParam = new ECKeyGenerationParameters(CURVE, new SecureRandom());
        eGen.init(gParam);
        ECKeyGenerationParameters keygenParams = new ECKeyGenerationParameters(CURVE, new SecureRandom());
        ECKeyPairGenerator generator = new ECKeyPairGenerator();
        generator.init(keygenParams);
        EphemeralKeyPairGenerator kGen = new EphemeralKeyPairGenerator(generator, new KeyEncoder() {
            public byte[] getEncoded(AsymmetricKeyParameter keyParameter) {
                return getQ().getEncoded();
            }
        });
        ECKeyPairGenerator gen = new ECKeyPairGenerator();
        gen.init(new ECKeyGenerationParameters(CURVE, new SecureRandom()));
        iesEngine.init(myKey.getPublic(), parametersWithIV, kGen);
        byte[] message = Hex.decode("010101");
        CryptoTest.log.info("payload: {}", Hex.toHexString(message));
        byte[] cipher = iesEngine.processBlock(message, 0, message.length);
        CryptoTest.log.info("cipher: {}", Hex.toHexString(cipher));
        IESEngine decryptorIES_Engine = new IESEngine(new ECDHBasicAgreement(), new org.spongycastle.crypto.generators.KDF2BytesGenerator(new SHA256Digest()), new org.spongycastle.crypto.macs.HMac(new SHA256Digest()), new BufferedBlockCipher(new SICBlockCipher(aesFastEngine)));
        decryptorIES_Engine.init(myKey.getPrivate(), parametersWithIV, new org.spongycastle.crypto.parsers.ECIESPublicKeyParser(CURVE));
        byte[] orig = decryptorIES_Engine.processBlock(cipher, 0, cipher.length);
        CryptoTest.log.info(("orig: " + (Hex.toHexString(orig))));
    }

    @Test
    public void calcSaltAddrTest() {
        byte[] from = Hex.decode("0123456789012345678901234567890123456789");
        byte[] salt = Hex.decode("0000000000000000000000000000000000000000000000000000000000000314");
        // contract Demo{}
        byte[] code = Hex.decode("6080604052348015600f57600080fd5b50603580601d6000396000f3006080604052600080fd00a165627a7a72305820a63607f79a5e21cdaf424583b9686f2aa44059d70183eb9846ccfa086405716e0029");
        Assert.assertArrayEquals(Hex.decode("d26e42c8a0511c19757f783402231cf82b2bdf59"), HashUtil.calcSaltAddr(from, code, salt));
    }
}
