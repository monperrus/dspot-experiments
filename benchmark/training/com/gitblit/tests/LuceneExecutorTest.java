/**
 * Copyright 2012 gitblit.com.
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
package com.gitblit.tests;


import com.gitblit.models.RepositoryModel;
import com.gitblit.models.SearchResult;
import com.gitblit.service.LuceneService;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jgit.lib.Repository;
import org.junit.Assert;
import org.junit.Test;


/**
 * Tests Lucene indexing and querying.
 *
 * @author James Moger
 */
public class LuceneExecutorTest extends GitblitUnitTest {
    LuceneService lucene;

    @Test
    public void testIndex() {
        // reindex helloworld
        Repository repository = GitBlitSuite.getHelloworldRepository();
        RepositoryModel model = newRepositoryModel(repository);
        lucene.reindex(model, repository);
        repository.close();
        SearchResult result = lucene.search("type:blob AND path:bit.bit", 1, 1, model.name).get(0);
        Assert.assertEquals("Mike Donaghy", result.author);
        result = lucene.search("type:blob AND path:clipper.prg", 1, 1, model.name).get(0);
        Assert.assertEquals("tinogomes", result.author);
        // reindex JGit
        repository = GitBlitSuite.getJGitRepository();
        model = newRepositoryModel(repository);
        lucene.reindex(model, repository);
        repository.close();
    }

    @Test
    public void testQuery() throws Exception {
        // 2 occurrences on the master branch
        Repository repository = GitBlitSuite.getHelloworldRepository();
        RepositoryModel model = newRepositoryModel(repository);
        repository.close();
        List<SearchResult> results = lucene.search("ada", 1, 10, model.name);
        Assert.assertEquals(2, results.size());
        for (SearchResult res : results) {
            Assert.assertEquals("refs/heads/master", res.branch);
        }
        // author test
        results = lucene.search("author: tinogomes AND type:commit", 1, 10, model.name);
        Assert.assertEquals(2, results.size());
        // blob test
        results = lucene.search("type: blob AND \"import std.stdio\"", 1, 10, model.name);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals("d.D", results.get(0).path);
        // commit test
        repository = GitBlitSuite.getJGitRepository();
        model = newRepositoryModel(repository);
        repository.close();
        results = lucene.search("\"initial jgit contribution to eclipse.org\"", 1, 10, model.name);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals("Git Development Community", results.get(0).author);
        Assert.assertEquals("1a6964c8274c50f0253db75f010d78ef0e739343", results.get(0).commitId);
        Assert.assertEquals("refs/heads/master", results.get(0).branch);
        // hash id tests
        results = lucene.search("type:commit AND commit:1a6964c8274c50f0253db75f010d78ef0e739343", 1, 10, model.name);
        Assert.assertEquals(1, results.size());
        results = lucene.search("type:commit AND commit:1a6964c8274*", 1, 10, model.name);
        Assert.assertEquals("Shawn O. Pearce", results.get(0).committer);
        Assert.assertEquals(1, results.size());
        // annotated tag test
        results = lucene.search("I663208919f297836a9c16bf458e4a43ffaca4c12", 1, 10, model.name);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals("[v1.3.0.201202151440-r]", results.get(0).tags.toString());
    }

    @Test
    public void testMultiSearch() throws Exception {
        List<String> list = new ArrayList<String>();
        Repository repository = GitBlitSuite.getHelloworldRepository();
        list.add(newRepositoryModel(repository).name);
        repository.close();
        repository = GitBlitSuite.getJGitRepository();
        list.add(newRepositoryModel(repository).name);
        repository.close();
        List<SearchResult> results = lucene.search("test", 1, 10, list);
        Assert.assertEquals(10, results.size());
    }

    @Test
    public void testDeleteBlobFromIndex() throws Exception {
        // start with a fresh reindex of entire repository
        Repository repository = GitBlitSuite.getHelloworldRepository();
        RepositoryModel model = newRepositoryModel(repository);
        lucene.reindex(model, repository);
        // now delete a blob
        Assert.assertTrue(lucene.deleteBlob(model.name, "refs/heads/master", "java.java"));
        Assert.assertFalse(lucene.deleteBlob(model.name, "refs/heads/master", "java.java"));
    }
}
