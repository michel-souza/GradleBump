package bump;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import com.github.zafarkhaja.semver.Version;

public class GitVersion {
    private final String gitDir;
    
    public GitVersion(final String gitDir) {
        this.gitDir = gitDir;
    }
    
    public String getGitDir() {
        return gitDir;
    }
    
    String getLatestReleaseTag(final String releaseTagPattern) throws IOException, GitAPIException {
        final Pattern tagPattern = Pattern.compile(releaseTagPattern);
        final FileRepositoryBuilder builder = new FileRepositoryBuilder();
        final Repository repository = builder.setWorkTree(new File(gitDir)).findGitDir().build();
        final RevWalk walk = new RevWalk(repository);
        walk.markStart(walk.parseCommit(repository.resolve("HEAD")));
        final String tag = new Git(repository).describe().call();
        walk.reset();
        final Matcher releaseTagMatcher = tagPattern.matcher(tag);
        repository.close();
        return releaseTagMatcher.matches() ? tag : null;
    }
    
    public void commitFile() throws IOException {
        final FileRepositoryBuilder builder = new FileRepositoryBuilder();
        final Repository repository = builder.setWorkTree(new File(gitDir)).findGitDir().build();
        try {
            new Git(repository).commit().setAll(true).setMessage("Bump the version of builld.gradle").call();
        } catch (final GitAPIException e) {
            e.printStackTrace();
        }
    }
    
    public String nextPreReleaseVersion(final String releaseTag, final String releaseTagPattern) {
        if (releaseTag.matches(releaseTagPattern)) {
            final Pattern pattern = Pattern.compile(releaseTagPattern);
            final Matcher m = pattern.matcher(releaseTag);
            m.matches();
            Version v = Version.valueOf(m.group(0));
            if (!m.group(2).contains("-")) {
                v = v.incrementPatchVersion().setPreReleaseVersion("rc").incrementPreReleaseVersion();
            } else {
                v = v.incrementPreReleaseVersion();
            }
            return v.toString();
        }
        return StringUtils.EMPTY;
    }
    
    public String nextReleaseVersion(final String tag, final String tagPattern) {
        Version v;
        if (tag.matches(tagPattern)) {
            final Pattern pattern = Pattern.compile(tagPattern);
            final Matcher m = pattern.matcher(tag);
            m.matches();
            if (m.group(2).contains("-")) {
                v = Version.valueOf(m.group(1));
            } else {
                v = Version.valueOf(m.group(1)).incrementPatchVersion();
            }
            return v.toString();
        }
        return StringUtils.EMPTY;
    }
    
    public String nextSnapshotVersion(final String tag, final String tagPattern) {
        Version v;
        if (tag.matches(tagPattern)) {
            final Pattern pattern = Pattern.compile(tagPattern);
            final Matcher m = pattern.matcher(tag);
            m.matches();
            v = Version.valueOf(m.group(1)).incrementMinorVersion();
            return v.toString();
        }
        return StringUtils.EMPTY;
    }
    
    public String versionIncrement(final String versao, final String releaseTag, final String releaseTagPattern) {
        switch (versao) {
        case "prerelease":
            return nextPreReleaseVersion(releaseTag, releaseTagPattern);
        case "release":
            return nextReleaseVersion(releaseTag, releaseTagPattern);
        case "snapshot":
            return nextSnapshotVersion(releaseTag, releaseTagPattern);
        default:
            return "0.0.0";
        }
    }
}
