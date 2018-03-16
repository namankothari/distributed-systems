/*
 * FingerTableEntry.java
 *
 * Author: Naman Kothari    nsk2400
 *
 * This class represents an entry in the finger table of the node.
 */

package node;

public class FingerTableEntry {

    private int index;
    private int nodeId;
    private int successorId;
    private String IP;

    /**
     * Constructor sets up the entry
     * @param index
     * @param nodeId
     * @param successorId
     * @param IP
     */
    public FingerTableEntry(int index, int nodeId, int successorId, String IP){

        this.index = index;
        this.nodeId = nodeId;
        this.successorId = successorId;
        this.IP = IP;
    }

    /**
     * This method sets the key in the entry
     * @param key key in the entry
     */
    public void setKey(int key) {
        this.successorId = key;
    }

    /**
     * This method sets the IP in the entry
     * @param IP IP in the entry
     */
    public void setIP(String IP){
        this.IP = IP;
    }

    /**
     * This method returns the IP of the successor in the entry
     * @return IP of the successor in the entry
     */
    public String getIP(){
        return IP;
    }

    /**
     * This method returns the id of the successor in the entry
     * @return id of the successor in the entry
     */
    public int getSuccessorId(){
        return successorId;
    }

    /**
     * This method returns the string representation the entry
     * @return string representation the entry
     */
    public String toString(){

        return "" + index + "\t" +  nodeId + "\t" + successorId + "\t" + IP;
    }
}
