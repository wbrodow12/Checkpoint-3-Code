class BTreeNode {

    /**
     * Array of the keys stored in the node.
     */
    long[] keys;
    /**
     * Array of the values[recordID] stored in the node. This will only be filled when the node is a leaf node.
     */
    long[] values;
    /**
     * Minimum degree (defines the range for number of keys)
     **/
    int t;
    /**
     * Pointers to the children, if this node is not a leaf.  If
     * this node is a leaf, then null.
     */
    BTreeNode[] children;
    /**
     * number of key-value pairs in the B-tree
     * MLB - based ont the code, this is the # of pairs in the node, not the whole tree.
     */
    int n;
    /**
     * true when node is leaf. Otherwise false
     */
    boolean leaf;

    /**
     * point to next node when it is a leaf node. null if not a leaf node or last leaf node.
     */
    BTreeNode next;

    /**
     * point to previous node when it is a leaf node. null if not a leaf node or first node.
     */
    BTreeNode previous;

    /**
     * point to the parent node of this node.
     */
    BTreeNode parent;


    /*
     * Given a BTreeNode and student record ID that exists in the node,
     * return the index of that value. Otherwise, return .-1
     */
    int findIndexOfId(long studentID){
        for(int i=0; i<this.keys.length;i++){
            if(this.keys[i] == studentID){
                return i;
            }
        }
        return -1;
    }

    void leftAlignKeyValuePairs(){
        //Left Align all of the Key-Value Pairs in this leaf.
        
        long[] keyStage = new long[this.keys.length];
        long[] valueStage = new long[this.values.length];

        int j=0;
        for(int i=0;i<this.keys.length;i++){
            if(!(this.keys[i]==0L)){
                keyStage[j] = this.keys[i];
                valueStage[j] = this.values[i];
                j++;
            }
        }

        this.keys = new long[keyStage.length];
        this.values = new long[valueStage.length];

        for(int k=0;k<j;k++){
            this.keys[k] = keyStage[k];
            this.values[k] = valueStage[k];
        }
    }

    // Constructor
    BTreeNode(int t, boolean leaf) {
        this.t = t;
        this.leaf = leaf;
        this.keys = new long[2 * t];
        this.children = new BTreeNode[2 * t + 1];
        this.n = 0;
        this.next = null;
        this.values = new long[2 * t];
    }

}
