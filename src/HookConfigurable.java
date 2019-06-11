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

    private static final String PATH_HOST = "trains.host";
    private static final String PATH_KEY = "trains.key";
    private static final String PATH_SECRET = "trains.secret";

    private JTextField userHost;
    private JTextField userKey;
    private JTextField userSecret;
    private static String storedHost = null;
    private static String storedKey = null;
    private static String storedSecret = null;

    private final Project project;

    public HookConfigurable(Project project) {
        this.project = project;
        if (storedHost == null || storedSecret == null || storedKey==null) {
            loadFromProperties(project);
        }
    }

    private static void loadFromProperties(Project project){
        PropertiesComponent properties = PropertiesComponent.getInstance(project);
        storedKey = properties.getValue(PATH_KEY);
        storedSecret = properties.getValue(PATH_SECRET);
        storedHost = properties.getValue(PATH_HOST);
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "TRAINS";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        userHost = new JTextField();
        userKey = new JTextField();
        userSecret = new JTextField();

        JPanel container = new JPanel(new GridLayoutManager(4, 2,
                new Insets(0, 0, 0, 0), 12, 12));

        GridConstraints pathLabelConstraint0 = new GridConstraints();
        pathLabelConstraint0.setRow(0);
        pathLabelConstraint0.setColumn(0);
        pathLabelConstraint0.setFill(GridConstraints.FILL_HORIZONTAL);
        pathLabelConstraint0.setVSizePolicy(GridConstraints.SIZEPOLICY_CAN_SHRINK);
        container.add(new JLabel("TRAINS server: "), pathLabelConstraint0);

        GridConstraints pathFieldConstraint0 = new GridConstraints();
        pathFieldConstraint0.setHSizePolicy(GridConstraints.SIZEPOLICY_WANT_GROW);
        pathFieldConstraint0.setFill(GridConstraints.FILL_HORIZONTAL);
        pathFieldConstraint0.setAnchor(GridConstraints.ANCHOR_WEST);
        pathFieldConstraint0.setRow(0);
        pathFieldConstraint0.setColumn(1);
        pathFieldConstraint0.setVSizePolicy(GridConstraints.SIZEPOLICY_CAN_SHRINK);
        container.add(userHost, pathFieldConstraint0);


        GridConstraints pathLabelConstraint = new GridConstraints();
        pathLabelConstraint.setRow(1);
        pathLabelConstraint.setColumn(0);
        pathLabelConstraint.setFill(GridConstraints.FILL_HORIZONTAL);
        pathLabelConstraint.setVSizePolicy(GridConstraints.SIZEPOLICY_CAN_SHRINK);
        container.add(new JLabel("User credentials: Key"), pathLabelConstraint);

        GridConstraints pathFieldConstraint = new GridConstraints();
        pathFieldConstraint.setHSizePolicy(GridConstraints.SIZEPOLICY_WANT_GROW);
        pathFieldConstraint.setFill(GridConstraints.FILL_HORIZONTAL);
        pathFieldConstraint.setAnchor(GridConstraints.ANCHOR_WEST);
        pathFieldConstraint.setRow(1);
        pathFieldConstraint.setColumn(1);
        pathFieldConstraint.setVSizePolicy(GridConstraints.SIZEPOLICY_CAN_SHRINK);
        container.add(userKey, pathFieldConstraint);


        GridConstraints pathLabelConstraint2 = new GridConstraints();
        pathLabelConstraint2.setRow(2);
        pathLabelConstraint2.setColumn(0);
        pathLabelConstraint2.setFill(GridConstraints.FILL_HORIZONTAL);
        pathLabelConstraint2.setVSizePolicy(GridConstraints.SIZEPOLICY_CAN_SHRINK);
        container.add(new JLabel("User credentials: Secret   "), pathLabelConstraint2);

        GridConstraints pathFieldConstraint2 = new GridConstraints();
        pathFieldConstraint2.setHSizePolicy(GridConstraints.SIZEPOLICY_WANT_GROW);
        pathFieldConstraint2.setFill(GridConstraints.FILL_HORIZONTAL);
        pathFieldConstraint2.setAnchor(GridConstraints.ANCHOR_WEST);
        pathFieldConstraint2.setRow(2);
        pathFieldConstraint2.setColumn(1);
        pathFieldConstraint2.setVSizePolicy(GridConstraints.SIZEPOLICY_CAN_SHRINK);
        container.add(userSecret, pathFieldConstraint2);


        JPanel spacer = new JPanel();
        GridConstraints spacerConstraints = new GridConstraints();
        spacerConstraints.setRow(3);
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

        if (storedHost == null && userHost != null) {
            return true;
        }

        return !storedKey.equals(userKey.getText()) || !storedSecret.equals(userSecret.getText())
                || !storedHost.equals(userHost.getText());
    }

    @Override
    public void apply() throws ConfigurationException {
        storedKey = userKey.getText().trim();
        storedSecret = userSecret.getText().trim();
        storedHost = userHost.getText().trim();

        PropertiesComponent properties = PropertiesComponent.getInstance(project);
        properties.setValue(PATH_HOST, storedHost);
        properties.setValue(PATH_KEY, storedKey);
        properties.setValue(PATH_SECRET, storedSecret);
    }

    @Override
    public void reset() {
        if (userHost != null) {
            userHost.setText(storedHost);
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
        userHost = null;
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

    static String getStoredHost(Project project) {
        if (storedHost==null && project!=null){
            loadFromProperties(project);
        }
        return storedHost;
    }
}