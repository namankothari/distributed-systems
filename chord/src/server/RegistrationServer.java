/*
 * RegistrationServer.java
 *
 * Author: Naman Kothari    nsk2400
 *
 * This class provides functionality of the Registration Server.
 */

package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class RegistrationServer extends Thread{

    private ServerSocket serverSocket;
    private final int SERVER_PORT = 8000;
    private boolean active = false;
    private Map<Integer, String> activeNodes;

    /**
     * Constructor intializes a new hash map to store active nodes and their IP in the form (node_key, node_IP)
     */
    public RegistrationServer(){

        activeNodes = new HashMap<>();

    }

    /**
     * This method sets up the server and creates a new server socket
     */
    public void run(){

        try {
            serverSocket = new ServerSocket(SERVER_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        active = true;
        System.out.println("server started...");

        handleRequests();

        System.out.println("server shut down...");
    }

    /**
     * This method handles the requests by nodes and spawns off new thread to fulfil the request
     */
    public void handleRequests(){
        while (active){

            try {
                Socket clientSocket = serverSocket.accept();
                new RequestHandler(clientSocket, this).start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method adds the nodeid and its IP to the hash map of active nodes
     * @param nodeid id of the node
     * @param IP IP of the node
     */
    public void addNode(int nodeid, String IP){

        activeNodes.put(nodeid, IP);
    }

    /**
     * This method gets the data of the next active node with respect to the current node
     * @param currentNode given node
     * @return data of the next active node with respect to the current node
     */
    public String getActiveNodeData(int currentNode){

        int counter = 0;
        int nextNode = currentNode;

        while (counter < 16){
            if (activeNodes.containsKey(nextNode))
                return "" + nextNode + ":" + activeNodes.get(nextNode);

            counter += 1;
            nextNode = (nextNode + 1) % 16;
        }

        // should never happen
        return null;
    }

    /**
     * This method removes the given node from the map of active nodes
     * @param nodeid id of node to be removed
     */
    public void removeNode(int nodeid){
        activeNodes.remove(nodeid);
    }

    /**
     * This method gets the id of the next active node with respect to the current node
     * @param currentNode given node
     * @return id of the next active node with respect to the current node
     */
    public int getNextActiveNode(int currentNode){

        int counter = 0;
        int nextNode = (currentNode + 1) % 16;

        while (counter < 16){
            if (activeNodes.containsKey(nextNode))
                return nextNode;

            counter += 1;
            nextNode = (nextNode + 1) % 16;
        }

        return nextNode;
    }

    /**
     * This method gets the id of the previous active node with respect to the current node
     * @param currentNode given node
     * @return id of the previous active node with respect to the current node
     */
    public int getPreviousActiveNode(int currentNode){

        int counter = 0;
        int prevNode;

        if(currentNode == 0)
            prevNode = 15;
        else
            prevNode = currentNode - 1;

        while (counter < 16){
            if (activeNodes.containsKey(prevNode))
                return prevNode;

            counter += 1;
            prevNode = (prevNode - 1);

            if(prevNode == -1)
                prevNode = 15;
        }

        return prevNode;
    }

    /**
     * This method returns the IP of the given node
     * @param nodeid id of the node
     * @return IP of the node
     */
    public String getIP(int nodeid){

        return (activeNodes.containsKey(nodeid)) ? activeNodes.get(nodeid): "-1";
    }

    /**
     * This is the main method that starts the server thread
     * @param args
     */
    public static void main(String[] args){

        RegistrationServer server = new RegistrationServer();
        server.start();

    }

}
