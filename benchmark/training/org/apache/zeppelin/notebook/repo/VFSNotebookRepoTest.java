/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.zeppelin.notebook.repo;


import AuthenticationInfo.ANONYMOUS;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.zeppelin.conf.ZeppelinConfiguration;
import org.apache.zeppelin.notebook.Note;
import org.apache.zeppelin.notebook.NoteInfo;
import org.apache.zeppelin.notebook.Paragraph;
import org.junit.Assert;
import org.junit.Test;


public class VFSNotebookRepoTest {
    private ZeppelinConfiguration zConf;

    private VFSNotebookRepo notebookRepo;

    private File notebookDir = Files.createTempDir();

    @Test
    public void testBasics() throws IOException {
        Assert.assertEquals(0, notebookRepo.list(ANONYMOUS).size());
        // create note1
        Note note1 = new Note();
        note1.setPath("/my_project/my_note1");
        Paragraph p1 = note1.insertNewParagraph(0, ANONYMOUS);
        p1.setText("%md hello world");
        p1.setTitle("my title");
        notebookRepo.save(note1, ANONYMOUS);
        Map<String, NoteInfo> noteInfos = notebookRepo.list(ANONYMOUS);
        Assert.assertEquals(1, noteInfos.size());
        Assert.assertEquals(note1.getId(), noteInfos.get(note1.getId()).getId());
        Assert.assertEquals(note1.getName(), noteInfos.get(note1.getId()).getNoteName());
        // create note2
        Note note2 = new Note();
        note2.setPath("/my_note2");
        Paragraph p2 = note2.insertNewParagraph(0, ANONYMOUS);
        p2.setText("%md hello world2");
        p2.setTitle("my title2");
        notebookRepo.save(note2, ANONYMOUS);
        noteInfos = notebookRepo.list(ANONYMOUS);
        Assert.assertEquals(2, noteInfos.size());
        // move note2
        String newPath = "/my_project2/my_note2";
        notebookRepo.move(note2.getId(), note2.getPath(), "/my_project2/my_note2", ANONYMOUS);
        Note note3 = notebookRepo.get(note2.getId(), newPath, ANONYMOUS);
        Assert.assertEquals(note2, note3);
        // move folder
        notebookRepo.move("/my_project2", "/my_project3/my_project2", ANONYMOUS);
        noteInfos = notebookRepo.list(ANONYMOUS);
        Assert.assertEquals(2, noteInfos.size());
        Note note4 = notebookRepo.get(note3.getId(), "/my_project3/my_project2/my_note2", ANONYMOUS);
        Assert.assertEquals(note3, note4);
        // remote note1
        notebookRepo.remove(note1.getId(), note1.getPath(), ANONYMOUS);
        Assert.assertEquals(1, notebookRepo.list(ANONYMOUS).size());
    }

    @Test
    public void testUpdateSettings() throws IOException {
        List<NotebookRepoSettingsInfo> repoSettings = notebookRepo.getSettings(ANONYMOUS);
        Assert.assertEquals(1, repoSettings.size());
        NotebookRepoSettingsInfo settingInfo = repoSettings.get(0);
        Assert.assertEquals("Notebook Path", settingInfo.name);
        Assert.assertEquals(notebookDir.getAbsolutePath(), settingInfo.selected);
        createNewNote("{}", "id2", "my_project/name2");
        Assert.assertEquals(1, notebookRepo.list(ANONYMOUS).size());
        String newNotebookDir = "/tmp/zeppelin/vfs_notebookrepo2";
        FileUtils.forceMkdir(new File(newNotebookDir));
        Map<String, String> newSettings = ImmutableMap.of("Notebook Path", newNotebookDir);
        notebookRepo.updateSettings(newSettings, ANONYMOUS);
        Assert.assertEquals(0, notebookRepo.list(ANONYMOUS).size());
    }
}
