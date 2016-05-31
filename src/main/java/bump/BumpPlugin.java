package bump;

import org.apache.commons.lang.StringUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class BumpPlugin implements Plugin<Project> {
    @Override
    public void apply(final Project project) {
        String versao = StringUtils.EMPTY;
        if (project.hasProperty("version")) {
            versao = System.getProperty("version");
        }
        Bump.setConfigurations(new GitVersion(project.getRootProject().getProjectDir().getAbsolutePath()), versao);
        project.getTasks().create("bump", Bump.class);
    }
}