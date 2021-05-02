package jspv.fsmodel;
//(C) 2004 - Geotechnical Software Services

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import javax.annotation.PostConstruct;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeModelEvent;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * A TreeModel implementation for a disk directory structure. FileSystemModel
 * model = new FileSystemModel (new File ("/")); JTree tree = new JTree (model);
 */
@Component
@Scope("singleton")
@Lazy(false)
public class FileSystemModel implements TreeModel {

    private Collection listeners_;
    private FileTreeNode root = new FileTreeNode(new File("/"));
    private HashMap sortedChildren_; // File -> List<File>
    private HashMap lastModified_;
    private File[] allroots;
    private List<String> allrootsString = new ArrayList<>();

    /**
     * Create a tree model using the specified file as root.
     *
     * @param root Root file (directory typically).
     */
    public FileSystemModel() {
    }

    @PostConstruct
    public void afterBirn() {
        //this.root = new FileTreeNode(root);
        listeners_ = new ArrayList();
        sortedChildren_ = new HashMap();
        lastModified_ = new HashMap();
        allroots = File.listRoots();
        for (File file : allroots) {
            allrootsString.add(file.toString());
        }
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public Object getChild(Object parent, int index) {
        List children = (List) sortedChildren_.get(parent);
        return children == null ? null : children.get(index);
    }

    @Override
    public int getChildCount(Object parent) {
        File file = (File) parent;
        if (!file.isDirectory()) {
            return 0;
        }

        File[] children = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String name = pathname.getName().toLowerCase();
                //return name.endsWith(".bmp")||
                return name.endsWith(".jpeg")||name.endsWith(".gif")||name.endsWith(".png")||name.endsWith(".jpg")||pathname.isDirectory();
            }
        });
        int nChildren = children == null ? 0 : children.length;

        long lastModified = file.lastModified();

        boolean isFirstTime = lastModified_.get(file) == null;
        boolean isChanged = false;

        if (!isFirstTime) {
            Long modified = (Long) lastModified_.get(file);
            long diff = Math.abs(modified.longValue() - lastModified);
            isChanged = diff > 4000; // MS/Win or Samba HACK. Check this!
        }

        // Sort and register children info
        if (isFirstTime || isChanged) {
            lastModified_.put(file, new Long(lastModified));

            TreeSet sorted = new TreeSet();
            for (int i = 0; i < nChildren; i++) {
                sorted.add(new FileTreeNode(children[i]));
            }

            sortedChildren_.put(file, new ArrayList(sorted));
        }

        // Notify listeners (visual tree typically) if changes
        if (isChanged) {
            TreeModelEvent event = new TreeModelEvent(this, getTreePath(file));
            fireTreeStructureChanged(event);
        }

        return nChildren;
    }

    private Object[] getTreePath(File file) {
        List path = new ArrayList();
        while (!file.equals(root)) {
            path.add(file);
            file = file.getParentFile();
        }
        path.add(root);

        int nElements = path.size();

        Object[] treePath = new Object[nElements];
        for (int i = 0; i < nElements; i++) {
            treePath[i] = path.get(nElements - i - 1);
        }

        return treePath;
    }

    @Override
    public boolean isLeaf(Object node) {
        return ((File) node).isFile();
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        List children = (List) sortedChildren_.get(parent);
        return children.indexOf(child);
    }

    @Override
    public void addTreeModelListener(TreeModelListener listener) {
        if (listener != null && !listeners_.contains(listener)) {
            listeners_.add(listener);
        }
    }

    @Override
    public void removeTreeModelListener(TreeModelListener listener) {
        if (listener != null) {
            listeners_.remove(listener);
        }
    }

    public void fireTreeNodesChanged(TreeModelEvent event) {
        for (Iterator i = listeners_.iterator(); i.hasNext();) {
            TreeModelListener listener = (TreeModelListener) i.next();
            listener.treeNodesChanged(event);
        }
    }

    public void fireTreeNodesInserted(TreeModelEvent event) {
        for (Iterator i = listeners_.iterator(); i.hasNext();) {
            TreeModelListener listener = (TreeModelListener) i.next();
            listener.treeNodesInserted(event);
        }
    }

    public void fireTreeNodesRemoved(TreeModelEvent event) {
        for (Iterator i = listeners_.iterator(); i.hasNext();) {
            TreeModelListener listener = (TreeModelListener) i.next();
            listener.treeNodesRemoved(event);
        }
    }

    public void fireTreeStructureChanged(TreeModelEvent event) {
        for (Iterator i = listeners_.iterator(); i.hasNext();) {
            TreeModelListener listener = (TreeModelListener) i.next();
            listener.treeStructureChanged(event);
        }
    }

    /*public static void main(String args[]) {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // The entire Linux root partition
        FileSystemModel model = new FileSystemModel(new File("/"));
        JTree tree = new JTree(model);
        f.getContentPane().add(new JScrollPane(tree));

        f.pack();
        f.setVisible(true);
    }*/
    /////////////////////////////////////////////////////////
    public String[] getAllrootsString() {
        return (String[]) allrootsString.toString().split(",");
    }

    public void setRoot(String root) {
        this.root = new FileTreeNode(new File(root));
    }

    public File[] getAllroots() {
        return allroots;
    }

    public void setAllroots(File[] allroots) {
        this.allroots = allroots;
    }

}
