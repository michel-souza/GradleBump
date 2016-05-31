package br.com.sabium;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import br.com.sabium.Bump;
import br.com.sabium.GitVersion;

public class BumpTest {
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
