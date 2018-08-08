package jspv.fsmodel;

import java.io.File;
import java.text.Collator;

public class FileTreeNode extends File implements Comparable<File> {

    public FileTreeNode(File file) {
        super(file, "");
    }

    /**
     * Compare two FileTreeNode objects so that directories are sorted first.
     * @param object
     */
    @Override
    public int compareTo(File object) {
        File file1 = this;
        File file2 = (File) object;

        Collator collator = Collator.getInstance();

        if (file1.isDirectory() && file2.isFile()) {
            return -1;
        } else if (file1.isFile() && file2.isDirectory()) {
            return +1;
        } else {
            return collator.compare(file1.getName(), file2.getName());
        }
    }

    /**
     * Return a string representation of this node. The inherited toString()
     * method returns the entire path. For use in a tree structure, the name is
     * more appropriate.
     */
   /* @Override
    public String toString() {
        return getName();
    }*/
}
