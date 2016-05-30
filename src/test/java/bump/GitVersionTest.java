package bump;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class GitVersionTest {
    private static final String TAG_PATTERN = "(\\d+\\.\\d+\\.\\d+)(.*)$";
    static GitVersion buildVersion;
    
    @BeforeClass
    public static void setup() {
        buildVersion = new GitVersion("/home/michel/WorkspaceGit/MeuWorkspace/libs-sabium-i18n");
    }
    
    @Test
    public void testGetLatestReleaseTag() throws Exception {
        Assert.assertTrue(buildVersion.getLatestReleaseTag("^v(\\d+\\.\\d+\\.\\d+)(.*)$")
                                      .matches("^v(\\d+\\.\\d+\\.\\d+)(.*)$"));
    }
    
    @Test
    public void testNextPreReleaseVersion() throws IOException, GitAPIException {
        Assert.assertEquals("2.3.1-rc.1", buildVersion.nextPreReleaseVersion("2.3.0", TAG_PATTERN));
        Assert.assertEquals("2.3.2-rc.2", buildVersion.nextPreReleaseVersion("2.3.2-rc.1", TAG_PATTERN));
    }
    
    @Test
    public void testNextReleaseVersion() {
        Assert.assertEquals("2.3.1", buildVersion.nextReleaseVersion("2.3.1-rc.5", TAG_PATTERN));
        Assert.assertEquals("2.3.1", buildVersion.nextReleaseVersion("2.3.0", TAG_PATTERN));
    }
    
    @Test
    public void testNextSnapshotVersion() {
        Assert.assertEquals("2.3.0", buildVersion.nextSnapshotVersion("2.2.1", TAG_PATTERN));
        Assert.assertEquals("2.6.0", buildVersion.nextSnapshotVersion("2.5.9-rc.1", TAG_PATTERN));
    }
    
    @Test
    public void testAlterarArquivo() throws IOException {
        Bump.setConfigurations(new GitVersion("/home/michel/WorkspaceGit/MeuWorkspace/libs-sabium-i18n"), "prerelease");
        Bump.alteraVersao("src/test/resources/org/gradle/test.gradle");
        final File alterado = new File("src/test/resources/org/gradle/test.gradle");
        final String file = "plugins {\n" + "  id \"maven-publish\"\n" + "  id \"org.sonarqube\" version \"2.0.1\"\n"
            + "}\n" + "\n" + "compileJava.options.encoding = 'UTF-8'\n" + "compileTestJava.options.encoding = 'UTF-8'\n"
            + "\n" + "ext.baseVersion = \"2.6.6-rc.1\"\n" + "ext.snapshotVersion = false\n";
        String line;
        String arquivoLido = StringUtils.EMPTY;
        final FileReader fr = new FileReader(alterado);
        final BufferedReader br = new BufferedReader(fr);
        while ((line = br.readLine()) != null) {
            arquivoLido += line + "\n";
        }
        br.close();
        fr.close();
        Assert.assertEquals(file, arquivoLido);
    }
}
