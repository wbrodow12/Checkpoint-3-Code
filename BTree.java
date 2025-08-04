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


    //comment what this does
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

    //comment what this does
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


    //comment what this does
    BTree insert(Student student) {
            /**
             * DONE:
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
                root.parent = newRoot; // MLB Keep track of the parent for deletion.
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

    //Comment what this does?
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

    // Comment what this does?
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
            newChild.previous = fullChild;

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
        
        BTreeNode foundNode = this.findLeaf(null,studentId);
        
        if(foundNode.equals(null)){
            return false;
        } 

        int deletionIndex = foundNode.findIndexOfId(studentId);
        boolean isDeletedFromTree=false;

        if(foundNode.keys.length>t){
            //we can just remove the key and be okay!
            foundNode.keys[deletionIndex] = 0L;
            foundNode.values[deletionIndex] = 0L;

            // need to reconcile the list, and make sure we've "left-aligned"
            // all of the key-value paris.
            foundNode.leftAlignKeyValuePairs();
            deleteStudentFromCSV(studentId);

        } else{
            // try borrow Left and Right
            BTreeNode leftNode = foundNode.previous;
            
            //borrow if possible!
            if(leftNode.n>t && (leftNode.parent.equals(foundNode.parent))){
                //move last key,Value in leftNode to beginning found node.
                prependLastKeyToNode(leftNode,foundNode);
                
                //remove key from left node + other vars
                leftNode.keys[leftNode.n-1] = 0L;
                leftNode.values[leftNode.n-1] = 0L;
                leftNode.n--;
                
                //change the pointer in the parent node to be the key from the new value
                for(int i=0;i<foundNode.parent.n;i++){
                    
                    //fix key in parent node so it still works.
                    if(foundNode.parent.children[i]==foundNode){
                        
                        foundNode.parent.keys[i-1]=foundNode.keys[0];

                        deleteStudentFromCSV(studentId);
                        return true;
                    }
                }
            }

            BTreeNode rightNode = foundNode.next;

            if(rightNode.n>t && (rightNode.parent.equals(foundNode.parent))){
                //move first key,Value pair in right Node to found node.
                appendFirstKeyToNode(rightNode,foundNode);
                
                //remove key from right node + other vars
                rightNode.keys[0] = 0L;
                rightNode.values[0] = 0L;
                rightNode.n--;
                
                //make sure there's no zero values in the first entry.
                rightNode.leftAlignKeyValuePairs();

                
                
                //change the pointer in the parent node to be the key from the new value
                for(int i=0;i<foundNode.parent.n;i++){
                    
                    //fix key in parent node so it still works.
                    if(foundNode.parent.children[i]==foundNode){
                        
                        foundNode.parent.keys[i]=rightNode.keys[0];

                        deleteStudentFromCSV(studentId);
                        return true;
                    }
                }
            }


            // Merge if we can't borrow

            if(!(leftNode.n>(t+1)) && (leftNode.parent.equals(foundNode.parent))){
                
                foundNode.keys[deletionIndex] = 0L;
                foundNode.values[deletionIndex] = 0L;

                //align all nodes in the found node so the prepend method works
                foundNode.leftAlignKeyValuePairs();

                //move the remaining keys, values to the merged node.
                appendAllKeysToNode(foundNode, leftNode);
                
                //get rid of the old node, preserve referrential integrity.
                leftNode.next = rightNode;

                //remove one navigational node, now that we're merging a leaf
                for(int i=0;i<foundNode.parent.n;i++){
                    
                    //fix key in parent node so it still works.
                    if(foundNode.parent.children[i]==foundNode){
                        
                        //update the key to be the new min value in this node
                        foundNode.parent.keys[i-1] = rightNode.keys[0];

                        for(int j=i;j<foundNode.parent.n-1;j++){
                            foundNode.parent.keys[j]=foundNode.parent.keys[j+1];
                            j++;
                        }

                        //update children in parent to be aligned with deleted node

                        for(int j=i;j<foundNode.parent.n;j++) {
                            foundNode.parent.children[j] = foundNode.parent.children[j+1];
                        }

                        foundNode.parent.children[foundNode.parent.n] = null; 
                        foundNode.parent.keys[foundNode.parent.n] = 0L;
                        foundNode.parent.n--; 
                        
                        //"delete" the node that's now merged into another node.
                        foundNode=null;

                        deleteStudentFromCSV(studentId);
                        return true;
                    }
                }
            }

            if(!(rightNode.n>(t+1)) && (rightNode.parent.equals(foundNode.parent))){

                foundNode.keys[deletionIndex] = 0L;
                foundNode.values[deletionIndex] = 0L;

                //align all nodes in the found node so the prepend method works
                foundNode.leftAlignKeyValuePairs();

                //move the remaining keys, values to the merged node.
                prependAllKeysToNode(foundNode, rightNode);
                
                //get rid of the old node, preserve referrential integrity.
                leftNode.next = rightNode;

                //remove one navigational node, now that we're merging a leaf
                for(int i=0;i<foundNode.parent.n;i++){
                    
                    //fix key in parent node so it still works.
                    if(foundNode.parent.children[i]==foundNode){
                        
                        //update the key to be the new min value in this node
                        foundNode.parent.keys[i-1] = rightNode.keys[0];

                        for(int j=i;j<foundNode.parent.n-1;j++){
                            foundNode.parent.keys[j]=foundNode.parent.keys[j+1];
                            j++;
                        }

                        //update children in parent to be aligned with deleted node

                        for(int j=i;j<foundNode.parent.n;j++) {
                            foundNode.parent.children[j] = foundNode.parent.children[j+1];
                        }

                        foundNode.parent.children[foundNode.parent.n] = null; 
                        foundNode.parent.keys[foundNode.parent.n] = 0L;
                        foundNode.parent.n--; 
                        
                        //"delete" the node that's now merged into another node.
                        foundNode=null;

                        deleteStudentFromCSV(studentId);
                        return true;
                        }
                 }
             }

                //check if Navigational Node Keys need to be changed
                return true;
            }
            //check navigational nodes
            return true;
        }

    // find the node that a studentId belongs to, if it exists. Otherwise, return null.
    BTreeNode findLeaf(BTreeNode node, long studentId) {
        int i=0;
       if(node.equals(null)){
         node = root;
       }
        while(i < node.n && studentId > node.keys[i]){
            i++;
        }
        if(node.leaf){
            if(i < node.n && node.keys[i] == studentId){
                return node;
            }
            System.out.println("Provided student ID to delete: " + studentId + " was not found in the table.");
            return null;
        }
        return findLeaf(node.children[i], studentId);
    }

    //TODO
    private static void appendAllKeysToNode(BTreeNode outgoing,BTreeNode incoming){
        for(int i=0;i<outgoing.n-1;i++){
            incoming.keys[incoming.n+i] = outgoing.keys[i];
            incoming.values[incoming.n+i] = outgoing.values[i];
        }
    }

    //TODO
    private static void prependAllKeysToNode(BTreeNode outgoing,BTreeNode incoming){

    }

    //TODO
    private static void prependLastKeyToNode(BTreeNode outgoing,BTreeNode incoming){

    }

    //TODO
    private static void appendFirstKeyToNode(BTreeNode outgoing,BTreeNode incoming){

    }

    //TODO
    private static void deleteStudentFromCSV(long studentID){

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
