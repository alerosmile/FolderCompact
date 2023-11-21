package dev.xframe.fc;

import com.intellij.history.core.Paths;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.NotNull;

public class LeafFolderNode extends PsiDirectoryNode {
    public LeafFolderNode(PsiDirectoryNode origin) {
        super(origin.getProject(), origin.getValue(), origin.getSettings(), origin.getFilter());
    }

    @Override
    public boolean isAlwaysLeaf() {
        return true;
    }
}