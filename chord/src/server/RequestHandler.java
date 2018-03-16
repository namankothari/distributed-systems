/*
 * RequestHadler.java
 *
 * Author: Naman Kothari    nsk2400
 *
 * This class handles the requests received by the Registration Server..
 */


package server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class RequestHandler extends Thread {

    private Socket clientSocket;
    private RegistrationServer server;
    private final int COMMON_PORT = 8000;

    /**
     * Constructor initializes the client socket and the instance of the Registration Server
     * @param clientSocket socket of the client
     * @param server instance of Registration Server
     */
    public RequestHandler(Socket clientSocket, RegistrationServer server) {

        this.server = server;
        this.clientSocket = clientSocket;
    }

    /**
     * This method reads the request received from the node and makes a call to parse the message.
     */
    public void run() {

        try (
                BufferedReader br = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
               BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(this.clientSocket.getOutputStream()));
        ) {
            String message = br.readLine().trim();
            parseMessage(message, bw);

        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    /**
     * This method parses the message and adds or removes a node from hash map
     * of active nodes based on the request. It also notifies the change to
     * the other active nodes
     * @param message message received in the client request
     * @param bw BufferedWriter to write back to the node
     * @throws IOException
     */
    public void parseMessage(String message, BufferedWriter bw) throws IOException{

        // message = JOIN key(0) IP(192.168.0.1) 1:2:4:8
        // message = LEAVE key(0) - -

        String[] messageData = message.split("\\s+");

        String command = messageData[0];
        String nodeid = messageData[1];
        String IP = messageData[2];
        String[] fingerTableNodes = messageData[3].split(":");

        if (command.equals("JOIN")) {

            // added the node and responded with data to build finger table
            System.out.println("Request to join: Node " + nodeid);

            // response = K1:IP1 K2:IP2 K4:IP4 K8:IP8 IP_PREV_NODE (for node 0)
            String response = addNode(Integer.parseInt(nodeid), IP, fingerTableNodes);
            response += "\n";

            bw.write(response);
            bw.flush();

            // notify required nodes of the new added node
            notifyChange(Integer.parseInt(nodeid), false);

        } else {

            System.out.println("Request to leave: Node " + nodeid);
            notifyChange(Integer.parseInt(nodeid), true);

            String reply_IP = server.getIP(Integer.parseInt(nodeid));

            try(
                    Socket socket = new Socket(reply_IP, COMMON_PORT);
                    BufferedWriter bw2 = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            ) {
                // reply = BYE
                String bye = "BYE\n";

                bw2.write(bye);
                bw2.flush();
            }
            server.removeNode(Integer.parseInt(nodeid));
        }
    }

    /**
     * This method adds the new node and its IP to map of active nodes and
     * returns the key:IP of the actives nodes needed to construct IP table
     * @param nodeid id of node to be added
     * @param IP IP of the node to be added
     * @param fingerTableNodes id of the nodes in the finger table of the given node
     * @return
     */
    public String addNode(int nodeid, String IP, String[] fingerTableNodes) {
        // return = K1:IP1 K2:IP2 K4:IP4 K8:IP8 IP_PREV_NODE (for node 0)

        server.addNode(nodeid, IP);

        String response = "";

        for(String nodeId: fingerTableNodes){

            response += server.getActiveNodeData(Integer.parseInt(nodeId)) + " ";
        }

        int prevActiveNode = server.getPreviousActiveNode(nodeid);

        return response.substring(0, response.length() - 1) + " " + prevActiveNode;
    }

    /**
     * This method notifies the addition or removal of node to other
     * relevant live nodes
     * @param nodeid id of the node
     * @param remove true if node is removed, false if node is added
     * @throws IOException
     */
    public void notifyChange(int nodeid, boolean remove) throws IOException {

        int nextActiveNode = server.getNextActiveNode(nodeid);
        int prevActiveNode = server.getPreviousActiveNode(nodeid);

        if (nextActiveNode != nodeid) {

            ArrayList<Integer> affectedNodes = new ArrayList<>();
            int id = (prevActiveNode + 1) % 16;

            while (id % 16 != nodeid) {

                affectedNodes.add(id);
                id += 1;
            }

            affectedNodes.add(nodeid);

            updateFingerTables(nodeid, nextActiveNode, affectedNodes, remove);
        }
    }

    /**
     * This method updates the relevant entries of the finger table of all the affected nodes
     * @param nodeid id of node
     * @param nextActiveNode id of the next active node with respect to the given node
     * @param affectedNodes list of affected nodes by addition or removal of node
     * @param remove true if node is removed, false if node is added
     */
    public void updateFingerTables(int nodeid, int nextActiveNode, ArrayList<Integer> affectedNodes, boolean remove) {

        int[] steps = new int[]{1, 2, 4, 8};

        for (int id : affectedNodes) {
            for (int index = 0; index < steps.length; index += 1) {

                int updateNodeId = id - steps[index];
                if (updateNodeId < 0)
                    updateNodeId = 16 + updateNodeId;

                String updateIP = server.getIP(updateNodeId);
                if (updateIP != "-1" && updateNodeId != nodeid) {
                    if(!remove)
                        updateFingerTable(nodeid, updateNodeId, index);
                    else
                        updateFingerTable(nextActiveNode, updateNodeId, index);
                }
            }
        }
    }

    /**
     * This method notifies the given node to update the index of the finger table
     * @param nodeid node to be notified for update
     * @param updateNodeId id with which finger table is to be updated
     * @param index index of finger table to be updated
     */
    public void updateFingerTable(int nodeid, int updateNodeId, int index) {

        // send "UPDATE INDEX:KEY:IP"

        String updateIP = server.getIP(nodeid);
        String message = "UPDATE " + index + ":" + nodeid + ":" + updateIP + "\n";

        String IP = server.getIP(updateNodeId);

        try (
                Socket socket = new Socket(IP, COMMON_PORT);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        )
        {
            bw.write(message);
            bw.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
