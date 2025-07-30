import java.util.ArrayList;
import java.util.List;

/**
 * B+Tree Structure
 * Key - StudentId
 * Leaf Node should contain [ key,recordId ]
 */
class BTree {

    /**
     * Pointer to the root node.
     */
    private BTreeNode root;
    /**
     * Number of key-value pairs allowed in the tree/the minimum degree of B+Tree
     **/
    private int t;

    BTree(int t) {
        this.root = null;
        this.t = t;
    }

    long search(long studentId) {
        /**
         * DONE:
         * Implement this function to search in the B+Tree.
         * Return recordID for the given StudentID.
         * Otherwise, print out a message that the given studentId has not been found in the table and return -1.
         */
        if (root == null){
            System.out.println("The B+Tree is empty. No records to search.");
            return -1;
        }
        return search(root, studentId);
    }

    private long search(BTreeNode node, long studentId){
        int i = 0;
        while(i < node.n && studentId > node.keys[i]){
            i++;
        }
        if(node.leaf){
            if(i < node.n && node.keys[i] == studentId){
                return node.values[i];
            }
            System.out.println("Provided student ID: " + studentId + " was not found in the table.");
            return -1;
        }
        return search(node.children[i], studentId);
    }


    BTree insert(Student student) {
                /**
             * TODO:
             * Implement this function to insert in the B+Tree.
             * Also, insert in student.csv after inserting in B+Tree.
             */
        long key = student.studentId;
        long recordId = student.recordId;

        if (root == null) {
            root = new BTreeNode(t, true);
            root.keys[0] = key;
            root.values[0] = recordId;
            root.n = 1;
        } else {
            if (root.n == 2 * t - 1) {
                BTreeNode newRoot = new BTreeNode(t, false);
                newRoot.children[0] = root;
                splitChild(newRoot, 0);
                root = newRoot;
            } 
            insertNonFull(root, key, recordId);
        }

        return this;
    }

    private void insertNonFull(BTreeNode node, long key, long recordId) {
        int i = node.n - 1;

        if (node.leaf) {
            while (i >= 0 && key < node.keys[i]) {
                node.keys[i + 1] = node.keys[i];
                node.values[i + 1] = node.values[i];
                i--;
            }
            node.keys[i + 1] = key;
            node.values[i + 1] = recordId;
            node.n++;
        } else {
            while (i >= 0 && key < node.keys[i]) {
                i--;
            }
            i++;
            if (node.children[i].n == 2 * t - 1) {
                splitChild(node, i);
                if (key >= node.keys[i]) {
                    i++;
                }
            }
            insertNonFull(node.children[i], key, recordId);
        }
    }

    private void splitChild(BTreeNode parent, int index) {
        BTreeNode fullChild = parent.children[index];
        BTreeNode newChild = new BTreeNode(t, fullChild.leaf);

        if (fullChild.leaf) {
            for (int j = 0; j < t; j++) {
                newChild.keys[j] = fullChild.keys[j + t - 1];
                newChild.values[j] = fullChild.values[j + t - 1];
            }
            newChild.n = t;
            fullChild.n = t - 1;

            // Link leaf nodes
            newChild.next = fullChild.next;
            fullChild.next = newChild;

            // Push key up to parent
            for (int j = parent.n; j > index; j--) {
                parent.children[j + 1] = parent.children[j];
                parent.keys[j] = parent.keys[j - 1];
            }
            parent.children[index + 1] = newChild;
            parent.keys[index] = newChild.keys[0];
            parent.n++;

        } else {
            for (int j = 0; j < t - 1; j++) {
                newChild.keys[j] = fullChild.keys[j + t];
            }
            for (int j = 0; j < t; j++) {
                newChild.children[j] = fullChild.children[j + t];
            }
            newChild.n = t - 1;
            fullChild.n = t - 1;

            // Promote middle key
            for (int j = parent.n; j > index; j--) {
                parent.children[j + 1] = parent.children[j];
                parent.keys[j] = parent.keys[j - 1];
            }
            parent.children[index + 1] = newChild;
            parent.keys[index] = fullChild.keys[t - 1];
            parent.n++;
        }
    }


    boolean delete(long studentId) {
        /**
         * TODO:
         * Implement this function to delete in the B+Tree.
         * Also, delete in student.csv after deleting in B+Tree, if it exists.
         * Return true if the student is deleted successfully otherwise, return false.
         */
        return true;
    }

    List<Long> print() {

        List<Long> listOfRecordID = new ArrayList<>();

        BTreeNode current = root;
        if (current == null) {
            return listOfRecordID;
        }
        while (!current.leaf) {
            current = current.children[0];
        }

        while (current != null) {
            for (int i = 0; i < current.n; i++) {
                listOfRecordID.add(current.values[i]);
            }
            current = current.next;
        }

        return listOfRecordID;
    }
}
