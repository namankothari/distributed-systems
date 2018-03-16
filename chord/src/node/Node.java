/*
 * Node.java
 *
 * Author: Naman Kothari    nsk2400
 *
 * This class provides functionality of the Node.
 */

package node;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Node {

    private HashMap<Integer, ArrayList<String>> records;
    private ArrayList<FingerTableEntry> table;
    private int key;
    private String IP;
    private boolean isActive;
    private Router router;

    private static String SERVER_HOST;

    public static final int COMMON_PORT = 8000;

    /**
     * Constructor initializes the data, router, finger table, etc.
     * @param key key of the node
     */
    public Node(int key) {

        this.key = key;
        this.records = new HashMap<>();
        this.table = new ArrayList<>();
        this.router = new Router(this);

        try {
            this.IP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method returns the IP of the node
     * @return IP of the node
     */
    public String getIP(){
        return IP;
    }

    /**
     * This method returns the Router of this node
     * @return Router of the node
     */
    public Router getRouter() {
        return router;
    }

    /**
     * This method returns a boolean to denote if the node is alive
     * @return boolean to denote if the node is alive
     */
    public boolean isActive(){
        return isActive;
    }

    /**
     * This method returns the key of the node
     * @return key of the node
     */
    public int getKey(){
        return key;
    }

    /**
     * This method returns the finger table of the node
     * @return finger table of the node.
     */
    public ArrayList<FingerTableEntry> getFingerTable() {
        return table;
    }

    /**
     * This method returns data stored locally at the node
     * @return data stored locally at the node
     */
    public HashMap<Integer, ArrayList<String>> getData() {
        return records;
    }

    /**
     * This method sets up the socket and spawns a new thread to handle request
     * and constructs the finger table and gets the data from the next active node.
     */
    public void initialize() {

        Object[] nodeData = createFingerTable();
        isActive = true;

        try {

            ServerSocket serverSocket = new ServerSocket(COMMON_PORT);
            NodeThread nodeThread = new NodeThread(serverSocket, this);
            nodeThread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }

        if(key != (int)nodeData[0])
            getDataFromNextNode((String)nodeData[1], (int)nodeData[2]);


        printFingerTable();
        startService();
    }

    /**
     * This method takes the choice from the user to add data, retrieve data, see local data
     * or exit and responds based on the choice made
     */
    public void startService() {

        displayMessage();

        Scanner sc = new Scanner(System.in);
        String choice = sc.nextLine().trim();

        while (!choice.equals("4")) {

            if (choice.equals("1")) {

                System.out.println("Enter key of the entry:");
                int data_key = Integer.parseInt(sc.nextLine());

                System.out.println("Enter value of the entry:");
                String data_value = sc.nextLine();

                if (data_key == key){

                    ArrayList<String> values = (records.containsKey(data_key)? records.get(data_key): new ArrayList<>());

                    values.add(data_value);
                    records.put(data_key, values);

                }else{
                    router.addEntry(data_key, data_value);
                }

            } else if(choice.equals("2")){

                System.out.println("Enter key of the entry:");
                int data_key = Integer.parseInt(sc.nextLine());

                if(data_key == key){
                    System.out.println("RETRIEVED DATA:");

                    ArrayList<String> values = (records.containsKey(data_key)? records.get(data_key): new ArrayList<>());

                    for (String value : values) {
                        System.out.println(value);
                    }
                }
                else {
                    router.getEntry(data_key, IP);
                }

            } else {

                printData();
            }

            displayMessage();
            choice = sc.nextLine().trim();
        }

        endService();
    }

    /**
     * This method transfers all the local data to next active node and notifies the server
     * that of its exit
     */
    public void endService(){

        transferData();

        try (
                Socket socket = new Socket(SERVER_HOST, COMMON_PORT);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        ) {
            // message = LEAVE 0 - -
            String message = "LEAVE " + key + " - -" + "\n";

            bw.write(message);
            bw.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

        isActive = false;
        System.out.println("Node Shutdown!");
    }

    /**
     * This method transfers the local data to next active node available from the finger table
     */
    public void transferData() {

        System.out.println("Transferring data...");

        // "DATA K1:V1 K1:V2 K2:V3..."
        FingerTableEntry entry = table.get(0);
        String IP = entry.getIP();
        if( !IP.equals(this.IP)){
            try (
                    Socket socket = new Socket(IP, COMMON_PORT);
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            ) {

                    String message = "DATA ";

                    for(Map.Entry<Integer, ArrayList<String>> data: records.entrySet()){

                        int key = data.getKey();
                        ArrayList<String> values = data.getValue();

                        for(String value: values){
                            message += key + ":" + value + " ";
                        }
                    }

                    message = message.substring(0, message.length() - 1) + "\n";
                    bw.write(message);
                    bw.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method displays the choices available to the user
     */
    public void displayMessage() {
        System.out.println("===========================");
        System.out.println("Press:\n1 to add new entry.\n2 to lookup entry.\n3 to see local data.\n4 to exit.");
        System.out.println("===========================");
    }

    /**
     * This table notifies the server that it wants to join and creates the finger table
     * based on the response from the server
     * @return object array of nextActiveNode, nextActiveNodeIP, prevActiveNode
     */
    public Object[] createFingerTable() {

        String response = null;
        try (   Socket socket = new Socket(SERVER_HOST, COMMON_PORT);
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))
            )
             {
                String message = "JOIN " + key + " " + IP + " ";
                message += getFingerTableNodeId();                  // JOIN key(0) IP(192.168.1.1) 1:2:4:8
                message += "\n";

                bw.write(message);
                bw.flush();

                response = br.readLine().trim();
                // K0:P0 K1:P1 K2:P2 K3:P3

        } catch (IOException e) {
            e.printStackTrace();
        }

        return parseResponseOnJoin(response);
    }

    /**
     * This message parses the response received from the server and creates the finger table
     * @param response response from the server
     * @return object array of nextActiveNode, nextActiveNodeIP, prevActiveNode
     */
    public Object[] parseResponseOnJoin(String response) {

        String[] responseData = response.split(" ");

        int nextActiveNode = -1;
        String nextActiveNodeIP = "-1";

        int prevActiveNode = Integer.parseInt(responseData[4]);

        for (int index = 0; index < 4; index++) {

            String[] fingerTableEntryData = responseData[index].split(":");

            if(index == 0){
                nextActiveNode = Integer.parseInt(fingerTableEntryData[0]);
                nextActiveNodeIP = fingerTableEntryData[1];
            }

            FingerTableEntry entry = new FingerTableEntry(index, key + (int) Math.pow(2, index),
                    Integer.parseInt(fingerTableEntryData[0]), (fingerTableEntryData[1]));

            table.add(entry);
        }

       return new Object[]{nextActiveNode, nextActiveNodeIP, prevActiveNode};
    }

    /**
     * This methods notifies the next active node to send data of nodes in the range
     * (prevActiveNode, curNode]
     * @param nextActiveNodeIP
     * @param prevActiveNode
     */
    public void getDataFromNextNode(String nextActiveNodeIP, int prevActiveNode){

        // SEND K0:K1:K2.. IP

        String message = "SEND ";

        int counter = 0;
        int currentNode = (prevActiveNode + 1) % 16;

        while (currentNode != key && counter < 16){

            message += currentNode + ":";
            currentNode = (currentNode + 1) % 16;

            counter += 1;
        }

        message += key + " " + IP + "\n";

        try(
            Socket socket = new Socket(nextActiveNodeIP, COMMON_PORT);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        ){
            bw.write(message);
            bw.flush();
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    /**
     * This method computes the node ids in the finger table of the given node
     * @return the node ids in the finger table of the given node
     */
    public String getFingerTableNodeId() {
        // returns 1:2:4:8 (for node 0)
        String ids = "";

        for (int index = 0; index < 4; index++) {

            ids += (key + (int) Math.pow(2, index)) % 16 + ":";
        }

        return ids.substring(0, ids.length() - 1);
    }

    /**
     * This method prints the finger table
     */
    public void printFingerTable(){

        System.out.println("FINGER TABLE:");
        System.out.println("===========================");
        for(FingerTableEntry f: table)
            System.out.println(f);

        System.out.println("===========================");
    }

    /**
     * This method prints the data stored locally
     */
    public void printData(){
        System.out.println("DATA:");
        System.out.println("===========================");
        for(Map.Entry<Integer, ArrayList<String>> data: records.entrySet()){

            int key = data.getKey();
            ArrayList<String> values = data.getValue();

            for(String value: values){
                System.out.println("" + key + ":" + value);
            }
        }

        System.out.println("===========================");
    }

    /**
     * This is the main method that sets up the node
     * @param args SERVER IP
     */
    public static void main(String[] args) {

        SERVER_HOST = args[0];

        Scanner sc = new Scanner(System.in);

        System.out.println("Enter node key between 0-15:");
        int key = Integer.parseInt(sc.nextLine());

        Node node = new Node(key);

        node.initialize();
    }
}
