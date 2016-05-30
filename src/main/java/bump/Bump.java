package bump;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleScriptException;
import org.gradle.api.tasks.TaskAction;

import com.github.zafarkhaja.semver.Version;

public class Bump extends DefaultTask {
    private static final String VERSION_PATTERN = "ext.baseVersion\\s\\=\\s\\\"((\\d+\\.\\d+\\.\\d+)(.*))\\\"";
    private static final String PATTERN_TAG = "(\\d+\\.\\d+\\.\\d+)(.*)";
    private static GitVersion gitVersion;
    private static String versao;
    private static final String FILE = "/build.gradle";
    
    public static void setConfigurations(final GitVersion gitVersion, final String version) {
        Bump.setGitVersion(gitVersion);
        Bump.setVersao(version);
    }
    
    @TaskAction
    public void bump() {
        try {
            alteraVersao(FILE);
        } catch (final Exception e) {
            throw new GradleScriptException("Não foi possível alterar a versão do arquivo usando o padrão "
                + PATTERN_TAG, e);
        }
    }
    
    public static void alteraVersao(final String archive) {
        final ArrayList<String> newFile = new ArrayList<String>();
        final File file = new File(archive);
        try {
            changeVersion(newFile, file);
            saveFile(newFile, file);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void saveFile(final ArrayList<String> newFile, final File file) throws IOException {
        final FileWriter fw = new FileWriter(file);
        final BufferedWriter out = new BufferedWriter(fw);
        newFile.forEach(linha -> {
            try {
                out.write(linha + "\n");
            } catch (final Exception e) {
                e.printStackTrace();
            }
        });
        out.flush();
        out.close();
        fw.close();
    }
    
    private static void changeVersion(final ArrayList<String> newFile, final File file) throws IOException {
        String line;
        final FileReader fr = new FileReader(file);
        final BufferedReader br = new BufferedReader(fr);
        while ((line = br.readLine()) != null) {
            if (line.matches(VERSION_PATTERN)) {
                final Pattern pattern = Pattern.compile(VERSION_PATTERN);
                final Matcher m = pattern.matcher(line);
                m.matches();
                final Version v =
                    Version.valueOf(getGitVersion().versionIncrement(getVersao(), m.group(1), PATTERN_TAG));
                line = line.replaceFirst("\\d+\\.\\d+\\.\\d+.*", v.toString() + "\"");
            }
            if (line.matches("ext.snapshotVersion = false")) {
                line = line.replaceFirst("false", "true");
            } else if (line.matches("ext.snapshotVersion = true")) {
                line = line.replaceFirst("true", "false");
            }
            newFile.add(line);
        }
        br.close();
        fr.close();
    }
    
    public static GitVersion getGitVersion() {
        return gitVersion;
    }
    
    public static void setGitVersion(final GitVersion gitVersion) {
        Bump.gitVersion = gitVersion;
    }
    
    public static String getVersao() {
        return versao;
    }
    
    public static void setVersao(final String versao) {
        Bump.versao = versao;
    }
}
