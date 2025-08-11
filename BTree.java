import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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

        if(node.keys[i] == studentId){
            return search(node.children[i+1], studentId);
        }else{
            return search(node.children[i], studentId);
        }
         // this code is assuming that the leaf node holding the value will be at the left
        // of the parent node. E.g. if we find the actual key as a navigational key, we'll take the child on the left side to find it.
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
            
            insertNonFull(root, key, recordId);

            if (root.n == 2 * t) { // MLB remove -1 so we use all key spaces available in Root
                BTreeNode newRoot = new BTreeNode(t, false);
                newRoot.children[0] = root;
                root.parent = newRoot; // MLB Keep track of the parent for deletion.
                splitChild(newRoot, 0);
                root = newRoot;
            } 
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
        int i = node.n - 1; // index of the last entry in the node's key table

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
            i++; // move i from the last entry in the key table to the index of the largest value smaller than the one we're inserting

            if (node.children[i].n == 2 * t) { // mlb remove -1 from this so we use all of the key spaces in a node.
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
                newChild.keys[j] = fullChild.keys[j + t];
                fullChild.keys[j+t] = 0L;
                newChild.values[j] = fullChild.values[j + t];
                fullChild.values[j+t] = 0L;
            }

            newChild.n = t;
            fullChild.n = t; 

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
            // MLB - something in this operation is losing keys.

            for (int j = 0; j < t ; j++) {
                newChild.keys[j] = fullChild.keys[j + t];
                fullChild.keys[j+t] = 0L;
            }
            for (int j = 0; j <= t; j++) { // make this less than or equal to. We need to copy t+1 children to the new child node(s)
                newChild.children[j] = fullChild.children[j + t];
                fullChild.children[j+t] = null; // MLB null copied children.
            }
            newChild.n = t;
            fullChild.n = t;

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


    /** Split a full child node into two nodes.
     * The middle key is pushed up to the parent node.
     * If the child is a leaf, the new child will also be a leaf.
     * If the child is not a leaf, the new child will have its own children.
     *
     * @param parent The parent node containing the full child.
     * @param index The index of the full child in the parent's children array.
     */
    boolean delete(long studentId) {
 
        BTreeNode foundNode = this.findLeaf(null,studentId);
        
        if(foundNode.equals(null)){
            return false;
        } 

        int deletionIndex = foundNode.findIndexOfId(studentId);

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
        if(node == null){
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

     //move the remaining keys, values to the merged node.
    private static void appendAllKeysToNode(BTreeNode outgoing,BTreeNode incoming){
        for(int i=0;i<outgoing.n-1;i++){
            incoming.keys[incoming.n+i] = outgoing.keys[i];
            incoming.values[incoming.n+i] = outgoing.values[i];
        }
    }

    //move the remaining keys, values to the merged node.
    private static void prependAllKeysToNode(BTreeNode outgoing,BTreeNode incoming){
        //move all keys and values of outgoing onto the front of incoming
        int stageSize=outgoing.n + incoming.n;
        long[] keyStage = new long[stageSize];
        long[] valueStage = new long[stageSize];

        int i;
        for(i = 0; i<outgoing.n ; i++){
            keyStage[i] = outgoing.keys[i];
            valueStage[i] = outgoing.values[i];
        }
        for(int j=0;j<incoming.n;i++){
            keyStage[j+i] = incoming.keys[j];
            valueStage[j+i] = outgoing.values[j];
        }

        for(int j=0;j<outgoing.n + incoming.n;j++){
            incoming.keys[j] = keyStage[j];
            incoming.values[j] = valueStage[j];
        }
    }

    //move last key,Value in leftNode to beginning found node.
    private static void prependLastKeyToNode(BTreeNode outgoing,BTreeNode incoming){
        for(int i=incoming.n;i>-1;i--){
            incoming.keys[i] = incoming.keys[i-1];
            incoming.values[i] = incoming.values[i-1];
        }
        incoming.keys[0] = outgoing.keys[outgoing.n-1];
        incoming.values[0] = outgoing.values[outgoing.n-1];
    }

    //move first key,Value pair in right Node to found node.
    private static void appendFirstKeyToNode(BTreeNode outgoing,BTreeNode incoming){
        incoming.keys[incoming.n] = outgoing.keys[0];
        incoming.values[incoming.n] = outgoing.values[0];
    }

    //remove the student from the CSV
    private static void deleteStudentFromCSV(long studentID){

        try(Scanner fileScanner = new Scanner(new File("Student.csv"))){
            while(fileScanner.hasNextLine()){

                String line = fileScanner.nextLine();
                String[] tokens = line.split(",");

                long readStudentID = 0;

                if(tokens.length == 6){

                    readStudentID = Long.parseLong(tokens[0]);
                }
                if(readStudentID == studentID){
                    try(FileWriter fw = new FileWriter("Student.csv",false)){
                       fw.write("");
                  }catch(Exception e){
                    e.printStackTrace();
                     }
                }
            }
        }catch (Exception e) {
            System.out.println("Error reading Student.csv: " + e.getMessage());
            return;
        }
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
