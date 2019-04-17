/**
 * Copyright (C) 2018 Ryszard Wi?niewski <brut.alll@gmail.com>
 *  Copyright (C) 2018 Connor Tumbleson <connor.tumbleson@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package brut.androlib.decode;


import brut.androlib.Androlib;
import brut.androlib.ApkDecoder;
import brut.androlib.BaseTest;
import brut.androlib.meta.MetaInfo;
import brut.common.BrutException;
import brut.directory.ExtFile;
import java.io.File;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;


/**
 *
 *
 * @author Connor Tumbleson <connor.tumbleson@gmail.com>
 */
public class MissingVersionManifestTest extends BaseTest {
    @Test
    public void missingVersionParsesCorrectlyTest() throws BrutException, IOException {
        String apk = "issue1264.apk";
        // decode issue1264.apk
        ApkDecoder apkDecoder = new ApkDecoder(new File((((BaseTest.sTmpDir) + (File.separator)) + apk)));
        ExtFile decodedApk = new ExtFile(((((BaseTest.sTmpDir) + (File.separator)) + apk) + ".out"));
        apkDecoder.setOutDir(new File(((((BaseTest.sTmpDir) + (File.separator)) + apk) + ".out")));
        apkDecoder.decode();
        MetaInfo metaInfo = new Androlib().readMetaFile(decodedApk);
        Assert.assertEquals(null, metaInfo.versionInfo.versionName);
    }
}
