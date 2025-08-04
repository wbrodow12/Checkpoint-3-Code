import java.io.FileWriter;
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


    /**     * Search for a studentId in the B+Tree.
     * If found, return the recordID associated with that studentId.
     * If not found, print a message and return -1.
     *
     * @param studentId The ID of the student to search for.
     * @return The recordID if found, otherwise -1.
     */ 
    long search(long studentId) {
        if (root == null){
            System.out.println("The B+Tree is empty. No records to search.");
            return -1;
        }
        return search(root, studentId);
    }

    /**Recursive function to search for a studentId in the B+Tree.
     * It traverses the tree until it finds the leaf node containing the studentId.
     *
     * @param node The current node being searched.
     * @param studentId The ID of the student to search for.
     * @return The recordID if found, otherwise -1.
     */
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


    /**Insert a student into the B+Tree.
     * If the tree is empty, create a new root node.
     * If the root is full, split it and create a new root.
     * Insert the student in the appropriate leaf node.
     *
     * @param student The student to be inserted.
     * @return The updated BTree instance.
     */
    BTree insert(Student student) {
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

        try(FileWriter fw = new FileWriter("Student.csv",true)){
            String CSVline = String.format("%d,%s,%s,%s,%d,%d\n", 
                student.studentId, student.studentName, student.major, student.level, student.age, recordId);
            fw.write(CSVline);
        }catch(Exception e){
            e.printStackTrace();
        }

        return this;
    }

    /** Insert a key-value pair into a non-full node.
     * If the node is a leaf, insert the key and recordId directly.
     * If the node is not a leaf, find the appropriate child to insert into.
     * If that child is full, split it before inserting.
     *
     * @param node The node to insert into.
     * @param key The key to be inserted (studentId).
     * @param recordId The record ID associated with the key.
     */
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

    /** Split a full child node into two nodes.
     * The middle key is pushed up to the parent node.
     * If the child is a leaf, the new child will also be a leaf.
     * If the child is not a leaf, the new child will have its own children.
     *
     * @param parent The parent node containing the full child.
     * @param index The index of the full child in the parent's children array.
     */
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


    //comment what this does?
    boolean delete(long studentId) {
        /**
         * TODO:
         * Implement this function to delete in the B+Tree.
         * Also, delete in student.csv after deleting in B+Tree, if it exists.
         * Return true if the student is deleted successfully otherwise, return false.
         */
        
        BTreeNode foundNode = this.findLeaf(studentId);

        if(foundNode.equals(null)){
            return false;
        } if(foundNode.keys.length>t){

            //we can just remove the key and be okay!
            int deletionIndex = foundNode.findIndexOfId(studentId);
            foundNode.keys[deletionIndex] = (Long) null; // wtf?
            foundNode.values[deletionIndex] = (Long) null;

            // need to reconcile the list, and make sure we've "left-aligned"
            // all of the key-value paris.
            foundNode.leftAlignKeyValuePairs();
        } else{
            // need to merge or something else to delete this key.

            // try borrow Left and Right

            // if fail, try merge with left/right.

            }

        return true;
    }

    // find the node that a studentId belongs to, if it exists. Otherwise, return null.
    BTreeNode findLeaf(long studentId) {

        return null;
    }


    /**
     * This method traverses the B+Tree and collects all record IDs
     * from the leaf nodes, returning them in a list.
     *
     * @return List of record IDs in the B+Tree.
     */
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
