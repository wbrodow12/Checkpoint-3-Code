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
         * TODO:
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
            if (root.n == 2 * t) {
                BTreeNode newRoot = new BTreeNode(t, false);
                newRoot.children[0] = root;
                splitChild(newRoot, 0);
                insertNonFull(newRoot, key, recordId);
                root = newRoot;
            } else {
                insertNonFull(root, key, recordId);
            }
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
            if (node.children[i].n == 2 * t) {
                splitChild(node, i);
                if (key > node.keys[i]) {
                    i++;
                }
            }
            insertNonFull(node.children[i], key, recordId);
        }
    }

    private void splitChild(BTreeNode parent, int index) {
        BTreeNode fullChild = parent.children[index];
        BTreeNode newChild = new BTreeNode(t, fullChild.leaf);
        newChild.n = t;

        // Copy last t keys from fullChild to newChild
        for (int j = 0; j < t; j++) {
            newChild.keys[j] = fullChild.keys[j + t];
            if (fullChild.leaf) {
                newChild.values[j] = fullChild.values[j + t];
            }
        }

        if (!fullChild.leaf) {
            for (int j = 0; j < t + 1; j++) {
                newChild.children[j] = fullChild.children[j + t];
            }
        }

        fullChild.n = t;

        // Insert newChild into parent
        for (int j = parent.n; j > index; j--) {
            parent.children[j + 1] = parent.children[j];
            parent.keys[j] = parent.keys[j - 1];
        }

        parent.children[index + 1] = newChild;
        parent.keys[index] = fullChild.leaf ? newChild.keys[0] : fullChild.keys[t - 1];
        parent.n++;

        // Handle leaf node next pointer
        if (fullChild.leaf) {
            newChild.next = fullChild.next;
            fullChild.next = newChild;
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

        /**
         * TODO:
         * Implement this function to print the B+Tree.
         * Return a list of recordIDs from left to right of leaf nodes.
         *
         */
        return listOfRecordID;
    }
}
