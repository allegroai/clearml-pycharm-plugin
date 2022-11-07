import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
// import com.intellij.execution.configurations.ParamsGroup;
import com.intellij.execution.configurations.RunnerSettings;
// import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.*;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import git4idea.config.GitVcsApplicationSettings;

// import com.jetbrains.python.debugger.PyDebugRunner;
import com.jetbrains.python.run.AbstractPythonRunConfiguration;
// import com.jetbrains.python.run.PythonCommandLineState;
import com.jetbrains.python.run.PythonRunConfigurationExtension;


import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

import java.util.Map;

public class ClearMLRunExtension extends PythonRunConfigurationExtension {
    private Project project = null;

    @Override
    protected void readExternal(@NotNull AbstractPythonRunConfiguration runConfiguration, @NotNull Element element) throws InvalidDataException {
    }

    @Override
    protected void writeExternal(@NotNull AbstractPythonRunConfiguration runConfiguration, @NotNull Element element) throws WriteExternalException {
    }

    @Nullable
    @Override
    protected SettingsEditor<AbstractPythonRunConfiguration> createEditor(@NotNull AbstractPythonRunConfiguration configuration) {
        return null;
    }

    @Nullable
    @Override
    protected String getEditorTitle() {
        return "ClearML (configuration)";
    }

    @Override
    public boolean isApplicableFor(@NotNull AbstractPythonRunConfiguration configuration) {
        return true;

    }

    @Override
    public boolean isEnabledFor(@NotNull AbstractPythonRunConfiguration applicableConfiguration, @Nullable RunnerSettings runnerSettings) {
        return true;
    }

    @Override
    protected void patchCommandLine(AbstractPythonRunConfiguration configuration, @Nullable RunnerSettings runnerSettings,
                                    GeneralCommandLine cmdLine, String runnerId) throws ExecutionException {
        // Reference: org.python.pydev.debug.profile.PyProfilePreferences.addProfileArgs(List<String>, boolean, boolean)
        project = configuration.getProject();
        if (HookConfigurable.getStoredKey(project) != null && !HookConfigurable.getStoredKey(project).isEmpty())
            cmdLine.withEnvironment("CLEARML_API_ACCESS_KEY", HookConfigurable.getStoredKey(project));
        if (HookConfigurable.getStoredSecret(project) != null && !HookConfigurable.getStoredSecret(project).isEmpty())
            cmdLine.withEnvironment("CLEARML_API_SECRET_KEY", HookConfigurable.getStoredSecret(project));
        if (HookConfigurable.getStoredAPI(project) != null && !HookConfigurable.getStoredAPI(project).isEmpty())
            cmdLine.withEnvironment("CLEARML_API_HOST", HookConfigurable.getStoredAPI(project));
        if (HookConfigurable.getStoredWEB(project) != null && !HookConfigurable.getStoredWEB(project).isEmpty())
            cmdLine.withEnvironment("CLEARML_WEB_HOST", HookConfigurable.getStoredWEB(project));
        if (HookConfigurable.getStoredFILES(project) != null && !HookConfigurable.getStoredFILES(project).isEmpty())
            cmdLine.withEnvironment("CLEARML_FILES_HOST", HookConfigurable.getStoredFILES(project));
        if (HookConfigurable.getDisableVerify(project))
            cmdLine.withEnvironment("CLEARML_API_HOST_VERIFY_CERT", "0");

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
        // if we failed try the old API
        if (git == null)
        {
            try {
                git = GitVcsApplicationSettings.getInstance().getPathToGit();
            } catch (Throwable t) {
            }
        }
        if (git == null) {
            project = null;
            return;
        }

        if (OsUtil.isWindows) {
            git = String.format("\"%s\"", git);
        }
        // String path = project.getBasePath();
        String path = configuration.getWorkingDirectory();

        // first make sure we have a git repository
        String gitStatus = runCommand(git + " status -s", path, false);
        if (gitStatus==null){
            project = null;
            return;
        }


        String gitRepo = runCommand(git + " ls-remote --get-url origin", path, false);
        String gitBranch = runCommand(git + " rev-parse --abbrev-ref --symbolic-full-name @{u}", path, false);
        String gitCommit = runCommand(git + " rev-parse HEAD", path, false);
        String gitRoot = runCommand(git + " rev-parse --show-toplevel", path, false);
        String gitDiff = runCommand(git + " diff --no-color", path, true);
        if (gitRepo!=null)
            cmdLine.withEnvironment("CLEARML_VCS_REPO_URL", gitRepo);
        if (gitBranch!=null)
            cmdLine.withEnvironment("CLEARML_VCS_BRANCH", gitBranch);
        if (gitCommit!=null)
            cmdLine.withEnvironment("CLEARML_VCS_COMMIT_ID", gitCommit);
        if (gitRoot!=null) {
            String relRool = ".";
            try {
                relRool = Paths.get(configuration.getWorkingDirectory()).relativize(Paths.get(gitRoot)).toString();
            } catch (Throwable t) {
                // We cannot resolve it, assume same folder.
            }
            cmdLine.withEnvironment("CLEARML_VCS_ROOT", relRool);

            try {
                String workDir = Paths.get(gitRoot).relativize(Paths.get(configuration.getWorkingDirectory())).toString();
                cmdLine.withEnvironment("CLEARML_VCS_WORK_DIR", workDir);
            } catch (Throwable t) {
                // We cannot resolve it, assume same folder.
            }
        }
        if (gitStatus!=null)
            cmdLine.withEnvironment("CLEARML_VCS_STATUS",  Base64.getEncoder().encodeToString(gitStatus.getBytes()));
        if (gitDiff!=null)
            cmdLine.withEnvironment("CLEARML_VCS_DIFF", Base64.getEncoder().encodeToString(gitDiff.getBytes()));

        Map<String, String>  commands = cmdLine.getEffectiveEnvironment();
        String effectiveCmdString = "" + commands;
        // System.out.println("CMD: " + effectiveCmdString);
        // System.out.println("CMD length: " + effectiveCmdString.length());
        if (effectiveCmdString.length() >= 128000) {
            openWarning("warning", String.format("Dropping GIT DIFF! Git diff is too large (%d bytes)",
                    effectiveCmdString.length()), 5000);
            cmdLine.withEnvironment("CLEARML_VCS_DIFF", "");

            // Map<String, String>  reduced_commands = cmdLine.getEffectiveEnvironment();
            // String reduced_commands_str = "" + reduced_commands;
            // System.out.println("New CMD length: " + reduced_commands_str.length());
        }
        project = null;
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
        try {
            JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(
                            "ClearML " + title + ": " + message, MessageType.WARNING, null
                    ).setFadeoutTime(timeout)
                    .createBalloon().show(
                            RelativePoint.getNorthWestOf(WindowManager.getInstance().getStatusBar(project).getComponent()),
                            Balloon.Position.atRight
                    );
        }
        catch (Throwable t){
            JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(
                            "ClearML " + title + ": " + message, MessageType.WARNING, null
                    ).setFadeoutTime(timeout)
                    .createBalloon().show(
                            RelativePoint.fromScreen(new Point(0,0)),
                            Balloon.Position.below);
        }
    }

    private void openError(final String title, final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(new JFrame(), message, title, JOptionPane.ERROR_MESSAGE);
            }
        });
    }

}
