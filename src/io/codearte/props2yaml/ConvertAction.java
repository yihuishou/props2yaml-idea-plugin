package io.codearte.props2yaml;

import java.io.IOException;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

/**
 * @author Jakub Kubrynski
 */
public class ConvertAction extends AnAction {

    private static final String GROUP_DISPLAY_ID = "io.codearte.props2yaml";

    @Override
    public void update(AnActionEvent anActionEvent) {
        PsiFile selectedFile = getSelectedPropertiesFile(anActionEvent, false);
        anActionEvent.getPresentation().setEnabledAndVisible(selectedFile != null);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        PsiFile selectedFile = getSelectedPropertiesFile(anActionEvent, true);
        if (selectedFile == null) return;

        VirtualFile propertiesFile = selectedFile.getVirtualFile();
        ApplicationManager.getApplication().runWriteAction(() -> {
            try {
                String yamlContent = new Props2YAML(new String(propertiesFile.contentsToByteArray())).convert();
                propertiesFile.rename(this, propertiesFile.getNameWithoutExtension() + ".yml");
                propertiesFile.setBinaryContent(yamlContent.getBytes());
            } catch (IOException e) {
                Notifications.Bus.notify(new Notification(GROUP_DISPLAY_ID, "无法重命名文件", e.getMessage(), NotificationType.ERROR));
            }
        });

        Notifications.Bus.notify(new Notification(GROUP_DISPLAY_ID, "文件转换", "文件转换成功", NotificationType.INFORMATION));
    }

    @Nullable
    private PsiFile getSelectedPropertiesFile(AnActionEvent anActionEvent, boolean showNotifications) {
        PsiFile selectedFile = anActionEvent.getData(LangDataKeys.PSI_FILE);
        if (selectedFile == null) {
            if (showNotifications) {
                Notifications.Bus.notify(new Notification(GROUP_DISPLAY_ID, "没有选中的文件", "请选中Properties文件", NotificationType.ERROR));
            }
            return null;
        }
        if (!StdFileTypes.PROPERTIES.equals(selectedFile.getFileType())) {
            if (showNotifications) {
                Notifications.Bus.notify(new Notification(GROUP_DISPLAY_ID, "无效的文件", "请选中Properties文件", NotificationType.ERROR));
            }
            return null;
        }
        return selectedFile;
    }
}
