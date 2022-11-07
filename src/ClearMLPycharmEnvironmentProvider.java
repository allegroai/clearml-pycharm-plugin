import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import com.jetbrains.python.run.PythonExecution;
import com.jetbrains.python.run.PythonRunParams;
import com.jetbrains.python.run.target.HelpersAwareTargetEnvironmentRequest;
import com.jetbrains.python.run.target.PythonCommandLineTargetEnvironmentProvider;
import git4idea.config.GitVcsApplicationSettings;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.*;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

public class ClearMLPycharmEnvironmentProvider implements PythonCommandLineTargetEnvironmentProvider {

    @Override
    public void extendTargetEnvironment(@NotNull Project project, @NotNull HelpersAwareTargetEnvironmentRequest helpersAwareTargetEnvironmentRequest, @NotNull PythonExecution pythonExecution, @NotNull PythonRunParams pythonRunParams) {
        // Reference: org.python.pydev.debug.profile.PyProfilePreferences.addProfileArgs(List<String>, boolean, boolean)
        if (HookConfigurable.getStoredKey(project) != null && !HookConfigurable.getStoredKey(project).isEmpty())
            pythonExecution.addEnvironmentVariable("CLEARML_API_ACCESS_KEY", HookConfigurable.getStoredKey(project));
        if (HookConfigurable.getStoredSecret(project) != null && !HookConfigurable.getStoredSecret(project).isEmpty())
            pythonExecution.addEnvironmentVariable("CLEARML_API_SECRET_KEY", HookConfigurable.getStoredSecret(project));
        if (HookConfigurable.getStoredAPI(project) != null && !HookConfigurable.getStoredAPI(project).isEmpty())
            pythonExecution.addEnvironmentVariable("CLEARML_API_HOST", HookConfigurable.getStoredAPI(project));
        if (HookConfigurable.getStoredWEB(project) != null && !HookConfigurable.getStoredWEB(project).isEmpty())
            pythonExecution.addEnvironmentVariable("CLEARML_WEB_HOST", HookConfigurable.getStoredWEB(project));
        if (HookConfigurable.getStoredFILES(project) != null && !HookConfigurable.getStoredFILES(project).isEmpty())
            pythonExecution.addEnvironmentVariable("CLEARML_FILES_HOST", HookConfigurable.getStoredFILES(project));
        if (HookConfigurable.getDisableVerify(project))
            pythonExecution.addEnvironmentVariable("CLEARML_API_HOST_VERIFY_CERT", "0");

        // if we do not need to check the git status, we can leave now
        if (HookConfigurable.getDisableGit(project))
            return;

        String git = null;
        // first try new API
        try {
            git = GitVcsApplicationSettings.getInstance().getSavedPathToGit();
        }
        catch (Throwable t){
        }

        if (git == null) {
            // no git
            return;
        }

        if (OsUtil.isWindows) {
            git = String.format("\"%s\"", git);
        }
        // String path = project.getBasePath();
        String path = pythonRunParams.getWorkingDirectory();

        // first make sure we have a git repository
        String gitStatus = runCommand(git + " status -s", path, false);
        if (gitStatus==null){
            return;
        }

        String gitRepo = runCommand(git + " ls-remote --get-url origin", path, false);
        String gitBranch = runCommand(git + " rev-parse --abbrev-ref --symbolic-full-name @{u}", path, false);
        String gitCommit = runCommand(git + " rev-parse HEAD", path, false);
        String gitRoot = runCommand(git + " rev-parse --show-toplevel", path, false);
        String gitDiff = runCommand(git + " diff --no-color", path, true);
        if (gitRepo!=null)
            pythonExecution.addEnvironmentVariable("CLEARML_VCS_REPO_URL", gitRepo);
        if (gitBranch!=null)
            pythonExecution.addEnvironmentVariable("CLEARML_VCS_BRANCH", gitBranch);
        if (gitCommit!=null)
            pythonExecution.addEnvironmentVariable("CLEARML_VCS_COMMIT_ID", gitCommit);
        if (gitRoot!=null) {
            String relRool = ".";
            try {
                relRool = Paths.get(pythonRunParams.getWorkingDirectory()).relativize(Paths.get(gitRoot)).toString();
            } catch (Throwable t) {
                // We cannot resolve it, assume same folder.
            }
            pythonExecution.addEnvironmentVariable("CLEARML_VCS_ROOT", relRool);

            try {
                String workDir = Paths.get(gitRoot).relativize(Paths.get(pythonRunParams.getWorkingDirectory())).toString();
                pythonExecution.addEnvironmentVariable("CLEARML_VCS_WORK_DIR", workDir);
            } catch (Throwable t) {
                // We cannot resolve it, assume same folder.
            }
        }
        if (gitStatus!=null)
            pythonExecution.addEnvironmentVariable("CLEARML_VCS_STATUS",  Base64.getEncoder().encodeToString(gitStatus.getBytes()));
        if (gitDiff!=null)
            pythonExecution.addEnvironmentVariable("CLEARML_VCS_DIFF", Base64.getEncoder().encodeToString(gitDiff.getBytes()));

        String effectiveCmdString = "" + pythonExecution.getEnvs();
        if (effectiveCmdString.length() >= 128000) {
            openWarning("warning", String.format("Dropping GIT DIFF! Git diff is too large (%d bytes)",
                    effectiveCmdString.length()), 5000);
            pythonExecution.addEnvironmentVariable("CLEARML_VCS_DIFF", "");
        }
    }

    private String runCommand(String cmd, String path, boolean useTempFile) {
        String output = null;
        File tempFile = null;
        try
        {
            if (useTempFile){
                tempFile = File.createTempFile(".clearml_git_diff", ".txt");
                cmd += " > " + tempFile.getAbsolutePath();
            }
            String[] fullCommand = getCommand(cmd);
            Process proc = Runtime.getRuntime().exec(fullCommand ,null, new File(path));
            BufferedReader br=null;
            if (!useTempFile) {
                InputStream stdin = proc.getInputStream();
                InputStreamReader isr = new InputStreamReader(stdin);
                br = new BufferedReader(isr);
            }
            if (!proc.waitFor(15, TimeUnit.SECONDS)) {
                proc.destroyForcibly();
                if (tempFile != null)
                    tempFile.delete();
                openWarning("warning", String.format("execution timed out: %s", cmd), 3000);
                return output;
            }
            if (br==null)
                br = new BufferedReader(new FileReader(tempFile.getAbsoluteFile()));
            // check if there was an error, update nothing
            if (proc.exitValue()!=0) {
                if (tempFile != null)
                    tempFile.delete();
                // System.out.println("<FAILED>: "+cmd);
                return output;
            }
            String line = null;
            // System.out.println("<OUTPUT>");
            while ( (line = br.readLine()) != null) {
                // System.out.println(line);
                if (output != null)
                    output += "\n"+line;
                else
                    output = line;
            }
            // System.out.println("</OUTPUT>");
            // System.out.println("Process exitValue: " + exitVal);
            if (tempFile != null) {
                tempFile.delete();
            }
        }
        catch (Throwable t)
        {
            if (tempFile != null)
                tempFile.delete();
            t.printStackTrace();
        }
        return output;
    }

    private String[] getCommand(String scriptCmd) {
        if (OsUtil.isWindows) {
            return new String[]{"C:\\Windows\\System32\\cmd.exe", "/c", scriptCmd};
        } else if (OsUtil.isMac) {
            return new String[]{"/bin/bash", "-c", scriptCmd};
        } else {
            return new String[]{"/bin/bash", "-c", scriptCmd};
        }
    }

    private void openWarning(final String title, final String message, final int timeout) {
        JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(
                        "ClearML " + title + ": " + message, MessageType.WARNING, null
                ).setFadeoutTime(timeout)
                .createBalloon().show(
                        RelativePoint.fromScreen(new Point(0,0)),
                        Balloon.Position.below);
    }
}
