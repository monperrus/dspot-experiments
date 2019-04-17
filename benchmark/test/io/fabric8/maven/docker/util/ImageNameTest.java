package io.fabric8.maven.docker.util;


import org.junit.Assert;
import org.junit.Test;


public class ImageNameTest {
    @Test
    public void simple() {
        Object[] data = new Object[]{ "jolokia/jolokia_demo", ImageNameTest.r().repository("jolokia/jolokia_demo").fullName("jolokia/jolokia_demo").fullNameWithTag("jolokia/jolokia_demo:latest").simpleName("jolokia_demo").tag("latest"), "jolokia/jolokia_demo:0.9.6", ImageNameTest.r().repository("jolokia/jolokia_demo").tag("0.9.6").fullName("jolokia/jolokia_demo").fullNameWithTag("jolokia/jolokia_demo:0.9.6").simpleName("jolokia_demo"), "test.org/jolokia/jolokia_demo:0.9.6", ImageNameTest.r().registry("test.org").repository("jolokia/jolokia_demo").tag("0.9.6").fullName("test.org/jolokia/jolokia_demo").fullNameWithTag("test.org/jolokia/jolokia_demo:0.9.6").simpleName("jolokia_demo"), "test.org/jolokia/jolokia_demo", ImageNameTest.r().registry("test.org").repository("jolokia/jolokia_demo").fullName("test.org/jolokia/jolokia_demo").fullNameWithTag("test.org/jolokia/jolokia_demo:latest").simpleName("jolokia_demo").tag("latest"), "test.org:8000/jolokia/jolokia_demo:8.0", ImageNameTest.r().registry("test.org:8000").repository("jolokia/jolokia_demo").tag("8.0").fullName("test.org:8000/jolokia/jolokia_demo").fullNameWithTag("test.org:8000/jolokia/jolokia_demo:8.0").simpleName("jolokia_demo"), "jolokia_demo", ImageNameTest.r().repository("jolokia_demo").fullName("jolokia_demo").fullNameWithTag("jolokia_demo:latest").simpleName("jolokia_demo").tag("latest"), "jolokia_demo:0.9.6", ImageNameTest.r().repository("jolokia_demo").tag("0.9.6").fullName("jolokia_demo").fullNameWithTag("jolokia_demo:0.9.6").simpleName("jolokia_demo"), "consol/tomcat-8.0:8.0.9", ImageNameTest.r().repository("consol/tomcat-8.0").tag("8.0.9").fullName("consol/tomcat-8.0").fullNameWithTag("consol/tomcat-8.0:8.0.9").simpleName("tomcat-8.0"), "test.org/user/subproject/image:latest", ImageNameTest.r().registry("test.org").repository("user/subproject/image").tag("latest").fullName("test.org/user/subproject/image").fullNameWithTag("test.org/user/subproject/image:latest").simpleName("subproject/image") };
        verifyData(data);
    }

    @Test
    public void testMultipleSubComponents() {
        Object[] data = new Object[]{ "org/jolokia/jolokia_demo", ImageNameTest.r().repository("org/jolokia/jolokia_demo").fullName("org/jolokia/jolokia_demo").fullNameWithTag("org/jolokia/jolokia_demo:latest").simpleName("jolokia/jolokia_demo").tag("latest"), "org/jolokia/jolokia_demo:0.9.6", ImageNameTest.r().repository("org/jolokia/jolokia_demo").tag("0.9.6").fullName("org/jolokia/jolokia_demo").fullNameWithTag("org/jolokia/jolokia_demo:0.9.6").simpleName("jolokia/jolokia_demo"), "repo.example.com/org/jolokia/jolokia_demo:0.9.6", ImageNameTest.r().registry("repo.example.com").repository("org/jolokia/jolokia_demo").tag("0.9.6").fullName("repo.example.com/org/jolokia/jolokia_demo").fullNameWithTag("repo.example.com/org/jolokia/jolokia_demo:0.9.6").simpleName("jolokia/jolokia_demo"), "repo.example.com/org/jolokia/jolokia_demo", ImageNameTest.r().registry("repo.example.com").repository("org/jolokia/jolokia_demo").fullName("repo.example.com/org/jolokia/jolokia_demo").fullNameWithTag("repo.example.com/org/jolokia/jolokia_demo:latest").simpleName("jolokia/jolokia_demo").tag("latest"), "repo.example.com:8000/org/jolokia/jolokia_demo:8.0", ImageNameTest.r().registry("repo.example.com:8000").repository("org/jolokia/jolokia_demo").tag("8.0").fullName("repo.example.com:8000/org/jolokia/jolokia_demo").fullNameWithTag("repo.example.com:8000/org/jolokia/jolokia_demo:8.0").simpleName("jolokia/jolokia_demo"), "org/jolokia_demo", ImageNameTest.r().repository("org/jolokia_demo").fullName("org/jolokia_demo").fullNameWithTag("org/jolokia_demo:latest").simpleName("jolokia_demo").tag("latest"), "org/jolokia_demo:0.9.6", ImageNameTest.r().repository("org/jolokia_demo").tag("0.9.6").fullName("org/jolokia_demo").fullNameWithTag("org/jolokia_demo:0.9.6").simpleName("jolokia_demo") };
        verifyData(data);
    }

    @Test
    public void testRegistryNaming() throws Exception {
        Assert.assertEquals("docker.jolokia.org/jolokia/jolokia_demo:0.18", new ImageName("jolokia/jolokia_demo:0.18").getFullName("docker.jolokia.org"));
        Assert.assertEquals("docker.jolokia.org/jolokia/jolokia_demo:latest", new ImageName("jolokia/jolokia_demo").getFullName("docker.jolokia.org"));
        Assert.assertEquals("jolokia/jolokia_demo:latest", new ImageName("jolokia/jolokia_demo").getFullName(null));
        Assert.assertEquals("docker.jolokia.org/jolokia/jolokia_demo:latest", new ImageName("docker.jolokia.org/jolokia/jolokia_demo").getFullName("another.registry.org"));
        Assert.assertEquals("docker.jolokia.org/jolokia/jolokia_demo:latest", new ImageName("docker.jolokia.org/jolokia/jolokia_demo").getFullName(null));
    }

    @Test
    public void testRegistryNamingExtended() throws Exception {
        Assert.assertEquals("docker.jolokia.org/org/jolokia/jolokia_demo:0.18", new ImageName("org/jolokia/jolokia_demo:0.18").getFullName("docker.jolokia.org"));
        Assert.assertEquals("docker.jolokia.org/org/jolokia/jolokia_demo:latest", new ImageName("org/jolokia/jolokia_demo").getFullName("docker.jolokia.org"));
        Assert.assertEquals("org/jolokia/jolokia_demo:latest", new ImageName("org/jolokia/jolokia_demo").getFullName(null));
        Assert.assertEquals("docker.jolokia.org/org/jolokia/jolokia_demo:latest", new ImageName("docker.jolokia.org/org/jolokia/jolokia_demo").getFullName("another.registry.org"));
        Assert.assertEquals("docker.jolokia.org/org/jolokia/jolokia_demo:latest", new ImageName("docker.jolokia.org/org/jolokia/jolokia_demo").getFullName(null));
        Assert.assertEquals("docker.jolokia.org/org/jolokia/jolokia_demo@sha256:2781907cc3ae9bb732076f14392128d4b84ff3ebb66379d268e563b10fbfb9da", new ImageName("docker.jolokia.org/org/jolokia/jolokia_demo@sha256:2781907cc3ae9bb732076f14392128d4b84ff3ebb66379d268e563b10fbfb9da").getFullName(null));
        Assert.assertEquals("docker.jolokia.org", new ImageName("docker.jolokia.org/org/jolokia/jolokia_demo@sha256:2781907cc3ae9bb732076f14392128d4b84ff3ebb66379d268e563b10fbfb9da").getRegistry());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalFormat() throws Exception {
        new ImageName("");
    }

    @Test
    public void namesUsedByDockerTests() {
        StringBuffer longTag = new StringBuffer();
        for (int i = 0; i < 130; i++) {
            longTag.append("a");
        }
        String[] illegal = new String[]{ "fo$z$", "Foo@3cc", "Foo$3", "Foo*3", "Fo^3", "Foo!3", "F)xcz(", "fo%asd", "FOO/bar", "repo:fo$z$", "repo:Foo@3cc", "repo:Foo$3", "repo:Foo*3", "repo:Fo^3", "repo:Foo!3", "repo:%goodbye", "repo:#hashtagit", "repo:F)xcz(", "repo:-foo", "repo:..", "repo:" + (longTag.toString()), "-busybox:test", "-test/busybox:test", "-index:5000/busybox:test" };
        for (String i : illegal) {
            try {
                new ImageName(i);
                Assert.fail(String.format("Name '%s' should fail", i));
            } catch (IllegalArgumentException exp) {
                /* expected */
            }
        }
        String[] legal = new String[]{ "fooo/bar", "fooaa/test", "foooo:t", "HOSTNAME.DOMAIN.COM:443/foo/bar" };
        for (String l : legal) {
            new ImageName(l);
        }
    }

    private static class Res {
        private String registry;

        private String repository;

        private String tag;

        private String fullName;

        private String fullNameWithTag;

        private String simpleName;

        boolean hasRegistry = false;

        ImageNameTest.Res registry(String registry) {
            this.registry = registry;
            this.hasRegistry = registry != null;
            return this;
        }

        ImageNameTest.Res repository(String repository) {
            this.repository = repository;
            return this;
        }

        ImageNameTest.Res tag(String tag) {
            this.tag = tag;
            return this;
        }

        ImageNameTest.Res fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        ImageNameTest.Res fullNameWithTag(String fullNameWithTag) {
            this.fullNameWithTag = fullNameWithTag;
            return this;
        }

        ImageNameTest.Res simpleName(String simpleName) {
            this.simpleName = simpleName;
            return this;
        }
    }
}
