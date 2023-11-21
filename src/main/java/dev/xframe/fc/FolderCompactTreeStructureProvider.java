package dev.xframe.fc;

import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.ProjectRootsUtil;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class FolderCompactTreeStructureProvider implements TreeStructureProvider {

    @Override
    public @NotNull Collection<AbstractTreeNode<?>> modify(@NotNull AbstractTreeNode<?> parent, @NotNull Collection<AbstractTreeNode<?>> children, ViewSettings settings) {
        if(parent instanceof PsiDirectoryNode) {
            PsiDirectoryNode dirParent = (PsiDirectoryNode)parent;
            FolderCompactConfigState fcState = FolderCompactConfigState.getInstance(dirParent.getProject());
            if(fcState.getShowModuleLibraries() && ProjectRootsUtil.isModuleContentRoot(dirParent.getValue())) {
                children.add(new ModuleLibrariesNode((PsiDirectoryNode) dirParent, settings));
            }
            if(fcState.getCompactSourceFolder()) {
                return children.stream().map(child->compact(dirParent, child)).flatMap(List::stream).collect(Collectors.toList());
            }
        }
        return children;
    }

    private List<AbstractTreeNode<?>> compact(PsiDirectoryNode root, AbstractTreeNode<?> node) {
        if((node instanceof PsiDirectoryNode)) {
            PsiDirectoryNode dirNode = (PsiDirectoryNode) node;
            List<AbstractTreeNode<?>> out = new ArrayList<>();

            if(ProjectRootsUtil.isModuleContentRoot(dirNode.getVirtualFile(), dirNode.getProject())) {
                out.add(node);
            }
            else if(ProjectRootsUtil.isModuleContentRoot(root.getVirtualFile(), root.getProject())) {
                // the start of a compacted folder must be a content root
                findSourceRootChild(root, node, 1, out);
                out.add(node);
            }
            else if(ProjectRootsUtil.isModuleSourceRoot(dirNode.getVirtualFile(), dirNode.getProject())) {
                // do not add folders which are added as compacted folders
            } else if(areAllChildrenModuleSourceRoot(dirNode)) {
                // all children are compacted folders -> enforce leaf node
                out.add(new LeafFolderNode(dirNode));
            }
            else {
                out.add(node);
            }
            return out;
        }
        else {
            // add non-directory nodes
            return Arrays.asList(node);
        }
    }

    private List<AbstractTreeNode<?>> findSourceRootChild(PsiDirectoryNode root, AbstractTreeNode<?> node, int depth, List<AbstractTreeNode<?>> out) {
        if((node instanceof PsiDirectoryNode)) {
            PsiDirectoryNode dirNode = (PsiDirectoryNode) node;
            if(ProjectRootsUtil.isModuleContentRoot(dirNode.getVirtualFile(), dirNode.getProject())) {
                // skip paths containing content roots
                return out;
            }
            if (ProjectRootsUtil.isModuleSourceRoot(dirNode.getVirtualFile(), dirNode.getProject())) {
                if(depth > 1) {
                    // no need to compact if root is immediate parent of node
                    out.add(new CompactedFolderNode(root, dirNode));
                }
                return out;
            }
            for (AbstractTreeNode<?> child : node.getChildren()) {
                findSourceRootChild(root, child, depth+1, out);
            }
        }
        return out;
    }

    private static boolean areAllChildrenModuleSourceRoot(PsiDirectoryNode dirNode) {
        for(AbstractTreeNode<?> childNode : dirNode.getChildren()) {
            if(childNode instanceof PsiDirectoryNode)
            {
                PsiDirectoryNode childDirNode = (PsiDirectoryNode)childNode;
                if(!ProjectRootsUtil.isModuleSourceRoot(childDirNode.getVirtualFile(), childDirNode.getProject())) {
                    return false;
                }
            }
            else {
                return false;
            }
        }
        return true;
    }
}
