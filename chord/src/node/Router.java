/*
 * Router.java
 *
 * Author: Naman Kothari    nsk2400
 *
 * This class handles the routing of store and retrieve requests.
 */

package node;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

public class Router {

    private Node node;
    private ArrayList<FingerTableEntry> table;

    /**
     * Constructor sets up the router
     * @param node node to which the router belongs
     */
    public Router(Node node) {
        this.node = node;
        this.table = node.getFingerTable();
    }

    /**
     * This method either stores data locally or tells the next node to store data locally or
     * tells the next node to find node to store the data
     * @param data_key key at which data is to be stored
     * @param data_value value of data to be stored
     */
    public void addEntry(int data_key, String data_value) {

        int node_key = node.getKey();
        FingerTableEntry entry = findFingerTable(data_key, node_key);

        int successorId = entry.getSuccessorId();

        int relSuccesorId = getDistance(successorId, node_key) ;
        int relDataId = getDistance(data_key, node_key);

        // DATA -> store at successorID
        // ADD -> successorID re routes to find node to store
        String command = ((relSuccesorId >= relDataId)? "DATA": "ADD");

        String IP = entry.getIP();
        String message = command + " " + data_key + ":" + data_value + "\n";
        //message = DATA K:V
        //message = ADD K:V

        if (IP.equals(node.getIP())) {
            // store locally

            ArrayList<String> values = (node.getData().containsKey(data_key)? node.getData().get(data_key): new ArrayList<>());

            values.add(data_value);
            node.getData().put(data_key, values);

        } else {

            try (
                    Socket socket = new Socket(IP, Node.COMMON_PORT);
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            ) {
                bw.write(message);
                bw.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method either retrieves data locally or tells the next node to retrieve data locally or
     * tells the next node to find node to retrieve the data
     * @param data_key key of data to be retrieved
     * @param reply_IP reply at this IP when data is found
     */
    public void getEntry(int data_key, String reply_IP) {

        int node_key = node.getKey();
        FingerTableEntry entry = findFingerTable(data_key, node_key);

        int successorId = entry.getSuccessorId();

        int relSuccesorId = getDistance(successorId, node_key) ;
        int relDataId = getDistance(data_key, node_key);

        // REPLY -> retrieve from successorID
        // GET -> successorID re routes to find node to retrieve
        String command = ((relSuccesorId >= relDataId)? "REPLY": "GET");

        String IP = entry.getIP();
        String message = command + " " + data_key + ":" + reply_IP + "\n";
        //message = REPLY K:IP
        //message = GET K:IP

        if (reply_IP.equals(IP)) {
            // reply locally
            System.out.println("RETRIEVED DATA:");


            ArrayList<String> values = (node.getData().containsKey(data_key)? node.getData().get(data_key): new ArrayList<>());

            for (String value : values) {
                System.out.println(value);
            }

        } else {

            try (
                    Socket socket = new Socket(IP, Node.COMMON_PORT);
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            ) {
                bw.write(message);
                bw.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method finds the finger table entry which is to be queried
     * @param data_key key at which data is supposed to be stored/retrieved
     * @param node_key key at which add/retrieve query is made
     * @return finger table entry which is to be queried
     */
    public FingerTableEntry findFingerTable(int data_key, int node_key) {

        int distance = getDistance(data_key, node_key);

        FingerTableEntry entry;

        if (distance >= 8) {
            entry = table.get(3);

        } else if (distance >= 4) {
            entry = table.get(2);

        } else if (distance >= 2) {
            entry = table.get(1);

        } else {
            entry = table.get(0);
        }

        return entry;
    }

    /**
     * This method gets distance between two nodes in the considering the circular structure
     * @param from distance from this node
     * @param to distance to this node
     * @return distance
     */
    public int getDistance(int from, int to){

        int distance;

        if (from > to) {
            distance = from - to;
        } else {
            distance = (16 - to) + from;
        }

        return distance;
    }

}
