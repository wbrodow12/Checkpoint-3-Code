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
     * point to other next node when it is a leaf node. Otherwise null
     */
    BTreeNode next;

    /*
     * Given a BTreeNode and student record ID that exists in the node,
     * return the index of that value. Otherwise, return .-1
     */
    int findIndexOfId(long studentID){
        for(int i=0; i<this.values.length;i++){
            if(this.values[i] == studentID){
                return i;
            }
        }
        return -1;
    }

    void leftAlignKeyValuePairs(){
        //Left Align all of the Key-Value Pairs in this leaf.
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
