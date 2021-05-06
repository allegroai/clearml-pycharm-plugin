import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class HookConfigurable implements Configurable {

    private static final String PATH_API = "clearml.host";
    private static final String PATH_WEB = "clearml.web";
    private static final String PATH_FILES = "clearml.files";
    private static final String PATH_KEY = "clearml.key";
    private static final String PATH_SECRET = "clearml.secret";

    private JTextField userAPI;
    private JTextField userWEB;
    private JTextField userFILES;
    private JTextField userKey;
    private JTextField userSecret;
    private static String storedAPI = null;
    private static String storedWEB = null;
    private static String storedFILES = null;
    private static String storedKey = null;
    private static String storedSecret = null;

    private final Project project;

    public HookConfigurable(Project project) {
        this.project = project;
        if (storedFILES == null || storedWEB == null || storedAPI == null || storedSecret == null || storedKey==null) {
            loadFromProperties(project);
        }
    }

    private static void loadFromProperties(Project project){
        PropertiesComponent properties = PropertiesComponent.getInstance(project);
        storedKey = properties.getValue(PATH_KEY);
        storedSecret = properties.getValue(PATH_SECRET);
        storedAPI = properties.getValue(PATH_API);
        storedWEB = properties.getValue(PATH_WEB);
        storedFILES = properties.getValue(PATH_FILES);
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "ClearML";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        userAPI = new JTextField();
        userWEB = new JTextField();
        userFILES = new JTextField();
        userKey = new JTextField();
        userSecret = new JTextField();

        JPanel container = new JPanel(new GridLayoutManager(6, 2,
                new Insets(0, 0, 0, 0), 12, 12));

        GridConstraints pathLabelConstraint0a = new GridConstraints();
        pathLabelConstraint0a.setRow(0);
        pathLabelConstraint0a.setColumn(0);
        pathLabelConstraint0a.setFill(GridConstraints.FILL_HORIZONTAL);
        pathLabelConstraint0a.setVSizePolicy(GridConstraints.SIZEPOLICY_CAN_SHRINK);
        container.add(new JLabel("ClearML Web server: "), pathLabelConstraint0a);

        GridConstraints pathFieldConstraint0a = new GridConstraints();
        pathFieldConstraint0a.setHSizePolicy(GridConstraints.SIZEPOLICY_WANT_GROW);
        pathFieldConstraint0a.setFill(GridConstraints.FILL_HORIZONTAL);
        pathFieldConstraint0a.setAnchor(GridConstraints.ANCHOR_WEST);
        pathFieldConstraint0a.setRow(0);
        pathFieldConstraint0a.setColumn(1);
        pathFieldConstraint0a.setVSizePolicy(GridConstraints.SIZEPOLICY_CAN_SHRINK);
        container.add(userWEB, pathFieldConstraint0a);


        GridConstraints pathLabelConstraint0b = new GridConstraints();
        pathLabelConstraint0b.setRow(1);
        pathLabelConstraint0b.setColumn(0);
        pathLabelConstraint0b.setFill(GridConstraints.FILL_HORIZONTAL);
        pathLabelConstraint0b.setVSizePolicy(GridConstraints.SIZEPOLICY_CAN_SHRINK);
        container.add(new JLabel("ClearML API server: "), pathLabelConstraint0b);

        GridConstraints pathFieldConstraint0b = new GridConstraints();
        pathFieldConstraint0b.setHSizePolicy(GridConstraints.SIZEPOLICY_WANT_GROW);
        pathFieldConstraint0b.setFill(GridConstraints.FILL_HORIZONTAL);
        pathFieldConstraint0b.setAnchor(GridConstraints.ANCHOR_WEST);
        pathFieldConstraint0b.setRow(1);
        pathFieldConstraint0b.setColumn(1);
        pathFieldConstraint0b.setVSizePolicy(GridConstraints.SIZEPOLICY_CAN_SHRINK);
        container.add(userAPI, pathFieldConstraint0b);


        GridConstraints pathLabelConstraint0c = new GridConstraints();
        pathLabelConstraint0c.setRow(2);
        pathLabelConstraint0c.setColumn(0);
        pathLabelConstraint0c.setFill(GridConstraints.FILL_HORIZONTAL);
        pathLabelConstraint0c.setVSizePolicy(GridConstraints.SIZEPOLICY_CAN_SHRINK);
        container.add(new JLabel("ClearML File server: "), pathLabelConstraint0c);

        GridConstraints pathFieldConstraint0c = new GridConstraints();
        pathFieldConstraint0c.setHSizePolicy(GridConstraints.SIZEPOLICY_WANT_GROW);
        pathFieldConstraint0c.setFill(GridConstraints.FILL_HORIZONTAL);
        pathFieldConstraint0c.setAnchor(GridConstraints.ANCHOR_WEST);
        pathFieldConstraint0c.setRow(2);
        pathFieldConstraint0c.setColumn(1);
        pathFieldConstraint0c.setVSizePolicy(GridConstraints.SIZEPOLICY_CAN_SHRINK);
        container.add(userFILES, pathFieldConstraint0c);


        GridConstraints pathLabelConstraint = new GridConstraints();
        pathLabelConstraint.setRow(3);
        pathLabelConstraint.setColumn(0);
        pathLabelConstraint.setFill(GridConstraints.FILL_HORIZONTAL);
        pathLabelConstraint.setVSizePolicy(GridConstraints.SIZEPOLICY_CAN_SHRINK);
        container.add(new JLabel("User credentials: Key"), pathLabelConstraint);

        GridConstraints pathFieldConstraint = new GridConstraints();
        pathFieldConstraint.setHSizePolicy(GridConstraints.SIZEPOLICY_WANT_GROW);
        pathFieldConstraint.setFill(GridConstraints.FILL_HORIZONTAL);
        pathFieldConstraint.setAnchor(GridConstraints.ANCHOR_WEST);
        pathFieldConstraint.setRow(3);
        pathFieldConstraint.setColumn(1);
        pathFieldConstraint.setVSizePolicy(GridConstraints.SIZEPOLICY_CAN_SHRINK);
        container.add(userKey, pathFieldConstraint);


        GridConstraints pathLabelConstraint2 = new GridConstraints();
        pathLabelConstraint2.setRow(4);
        pathLabelConstraint2.setColumn(0);
        pathLabelConstraint2.setFill(GridConstraints.FILL_HORIZONTAL);
        pathLabelConstraint2.setVSizePolicy(GridConstraints.SIZEPOLICY_CAN_SHRINK);
        container.add(new JLabel("User credentials: Secret   "), pathLabelConstraint2);

        GridConstraints pathFieldConstraint2 = new GridConstraints();
        pathFieldConstraint2.setHSizePolicy(GridConstraints.SIZEPOLICY_WANT_GROW);
        pathFieldConstraint2.setFill(GridConstraints.FILL_HORIZONTAL);
        pathFieldConstraint2.setAnchor(GridConstraints.ANCHOR_WEST);
        pathFieldConstraint2.setRow(4);
        pathFieldConstraint2.setColumn(1);
        pathFieldConstraint2.setVSizePolicy(GridConstraints.SIZEPOLICY_CAN_SHRINK);
        container.add(userSecret, pathFieldConstraint2);


        JPanel spacer = new JPanel();
        GridConstraints spacerConstraints = new GridConstraints();
        spacerConstraints.setRow(5);
        spacerConstraints.setFill(GridConstraints.FILL_BOTH);
        container.add(spacer, spacerConstraints);

        return container;
    }

    @Override
    public boolean isModified() {
        if (storedKey == null && userKey != null) {
            return true;
        }

        if (storedSecret == null && userSecret != null) {
            return true;
        }

        if (storedAPI == null && userAPI != null) {
            return true;
        }

        if (storedWEB == null && userWEB != null) {
            return true;
        }

        if (storedFILES == null && userFILES != null) {
            return true;
        }

        return !storedKey.equals(userKey.getText()) || !storedSecret.equals(userSecret.getText())
                || !storedAPI.equals(userAPI.getText())
                || !storedWEB.equals(userWEB.getText())
                || !storedFILES.equals(userFILES.getText());
    }

    @Override
    public void apply() throws ConfigurationException {
        storedKey = userKey.getText().trim();
        storedSecret = userSecret.getText().trim();
        storedAPI = fixHost(userAPI.getText().trim());
        storedWEB = fixHost(userWEB.getText().trim());
        storedFILES = fixHost(userFILES.getText().trim());

        PropertiesComponent properties = PropertiesComponent.getInstance(project);
        properties.setValue(PATH_API, storedAPI);
        properties.setValue(PATH_WEB, storedWEB);
        properties.setValue(PATH_FILES, storedFILES);
        properties.setValue(PATH_KEY, storedKey);
        properties.setValue(PATH_SECRET, storedSecret);
    }

    @Override
    public void reset() {
        if (userAPI != null) {
            userAPI.setText(storedAPI);
        }
        if (userWEB != null) {
            userWEB.setText(storedWEB);
        }
        if (userFILES != null) {
            userFILES.setText(storedFILES);
        }
        if (userKey != null) {
            userKey.setText(storedKey);
        }
        if (userSecret != null) {
            userSecret.setText(storedSecret);
        }
    }

    @Override
    public void disposeUIResources() {
        userKey = null;
        userSecret = null;
        userAPI = null;
        userWEB = null;
        userFILES = null;
    }

    static String fixHost(String host) {
        if (!host.isEmpty() && !(host.startsWith("http://") || host.startsWith("https://")))
            return "http://"+host;
        return host;
    }

    static String getStoredKey(Project project) {
        if (storedKey==null && project!=null){
            loadFromProperties(project);
        }
        return storedKey;
    }

    static String getStoredSecret(Project project) {
        if (storedSecret==null && project!=null){
            loadFromProperties(project);
        }
        return storedSecret;
    }

    static String getStoredAPI(Project project) {
        if (storedAPI==null && project!=null){
            loadFromProperties(project);
        }
        return storedAPI;
    }

    static String getStoredWEB(Project project) {
        if (storedWEB==null && project!=null){
            loadFromProperties(project);
        }
        return storedWEB;
    }

    static String getStoredFILES(Project project) {
        if (storedFILES==null && project!=null){
            loadFromProperties(project);
        }
        return storedFILES;
    }
}