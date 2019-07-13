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
// import com.jetbrains.python.debugger.PyDebugRunner;
import com.jetbrains.python.run.AbstractPythonRunConfiguration;
// import com.jetbrains.python.run.PythonCommandLineState;
// import com.jetbrains.python.run.PythonRunConfiguration;
import com.jetbrains.python.run.PythonRunConfigurationExtension;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import git4idea.config.GitVcsApplicationSettings;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class TrainsRunExtension extends PythonRunConfigurationExtension {
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
        return "TRAINS (configuration)";
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
            cmdLine.withEnvironment("ALG_API_ACCESS_KEY", HookConfigurable.getStoredKey(project));
        if (HookConfigurable.getStoredSecret(project) != null && !HookConfigurable.getStoredSecret(project).isEmpty())
            cmdLine.withEnvironment("ALG_API_SECRET_KEY", HookConfigurable.getStoredSecret(project));
        if (HookConfigurable.getStoredHost(project) != null && !HookConfigurable.getStoredHost(project).isEmpty())
            cmdLine.withEnvironment("ALG_API_HOST", HookConfigurable.getStoredHost(project));

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
        String path = project.getBasePath();

        String gitRepo = runCommand(git + " remote get-url origin", path, false);
        String gitBranch = runCommand(git + " rev-parse --abbrev-ref --symbolic-full-name @{u}", path, false);
        String gitCommit = runCommand(git + " rev-parse HEAD", path, false);
        String gitRoot = runCommand(git + " rev-parse --show-toplevel", path, false);
        String gitStatus = runCommand(git + " status -s", path, false);
        String gitDiff = runCommand(git + " diff", path, true);
        if (gitRepo!=null)
            cmdLine.withEnvironment("ALG_VCS_REPO_URL", gitRepo);
        if (gitBranch!=null)
            cmdLine.withEnvironment("ALG_VCS_BRANCH", gitBranch);
        if (gitCommit!=null)
            cmdLine.withEnvironment("ALG_VCS_COMMIT_ID", gitCommit);
        if (gitRoot!=null) {
            String root = configuration.getWorkingDirectory().replace(gitRoot, ".");
            cmdLine.withEnvironment("ALG_VCS_ROOT", root.isEmpty() ? "." : root);
        }
        if (gitStatus!=null)
            cmdLine.withEnvironment("ALG_VCS_STATUS",  Base64.getEncoder().encodeToString(gitStatus.getBytes()));
        if (gitDiff!=null)
            cmdLine.withEnvironment("ALG_VCS_DIFF", Base64.getEncoder().encodeToString(gitDiff.getBytes()));

        Map<String, String>  commands = cmdLine.getEffectiveEnvironment();
        String effectiveCmdString = "" + commands;
        // System.out.println("CMD: " + effectiveCmdString);
        // System.out.println("CMD length: " + effectiveCmdString.length());
        if (effectiveCmdString.length() >= 128000) {
            openWarning("warning", String.format("Dropping GIT DIFF! Git diff is too large (%d bytes)",
                    effectiveCmdString.length()), 5000);
            cmdLine.withEnvironment("ALG_VCS_DIFF", "");

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
                tempFile = File.createTempFile(".trains_git_diff", ".txt");
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
            if (!proc.waitFor(5, TimeUnit.SECONDS)) {
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
            if (tempFile != null)
                tempFile.delete();
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
                "TRAINS " + title + ": " + message, MessageType.WARNING, null
        ).setFadeoutTime(timeout)
                .createBalloon().show(
                RelativePoint.getNorthWestOf(WindowManager.getInstance().getStatusBar(project).getComponent()),
                Balloon.Position.atRight
        );
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
