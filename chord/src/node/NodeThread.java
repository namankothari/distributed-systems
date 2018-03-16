/*
 * NodeThread.java
 *
 * Author: Naman Kothari    nsk2400
 *
 * This class handles the requests made to the node and responds accordingly.
 */

package node;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class NodeThread extends Thread {

    private ServerSocket serverSocket;
    private Node node;

    /**
     * Constructor sets up the thread
     * @param serverSocket
     * @param node
     */
    public NodeThread(ServerSocket serverSocket, Node node) {
        this.serverSocket = serverSocket;
        this.node = node;
    }

    /**
     * This method parses the request message and responds accordingly
     * @param message message received as request
     */
    public void parseMessage(String message) {

        String[] messageData = message.split("\\s+");

        String command = messageData[0];

        // update finger table entry
        if (command.equals("UPDATE")) {
            //rec "UPDATE INDEX:KEY:IP"

            System.out.println("updating finger table for " + node.getKey());
            String[] updateData = messageData[1].split(":");

            int index = Integer.parseInt(updateData[0]);
            int key = Integer.parseInt(updateData[1]);
            String IP = updateData[2];

            ArrayList<FingerTableEntry> table = node.getFingerTable();
            FingerTableEntry entry = table.get(index);
            entry.setKey(key);
            entry.setIP(IP);

            node.printFingerTable();

        } else if (command.equals("DATA")) {
            // add data locally
            // rec "DATA K1:V1 K2:V2 ..."
            for (int index = 1; index < messageData.length; index++) {

                String data = messageData[index];
                String[] record = data.split(":");

                int key = Integer.parseInt(record[0]);
                String value = record[1];

                ArrayList<String> values = (node.getData().containsKey(key)? node.getData().get(key): new ArrayList<>());
                values.add(value);
                node.getData().put(key, values);

            }
        } else if (command.equals("SEND")) {
            // send local data of given keys to the given IP
            // SEND K0:K1:K2.. REPLY_IP

            String[] nodeIds;

            if (messageData[1].contains(":"))
                nodeIds = messageData[1].split(":");
            else
                nodeIds = new String[]{messageData[1]};

            String IP = messageData[2];


            // response = "DATA K1:V1 K2:V2 ..."
            String response = "DATA ";

            ArrayList<Integer> remove = new ArrayList<>();

            for (String id : nodeIds) {

                ArrayList<String> values = (node.getData().containsKey(Integer.parseInt(id))? node.getData().get(Integer.parseInt(id)): new ArrayList<>());

                for (String value : values) {

                    response += id + ":" + value + " ";
                }
                remove.add(Integer.parseInt(id));
            }

            for (int remove_key : remove)
                node.getData().remove(remove_key);

            response = response.substring(0, response.length() - 1) + "\n";


            try (
                    Socket socket = new Socket(IP, Node.COMMON_PORT);
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            ) {

                bw.write(response);
                bw.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (command.equals("ADD")) {
            // ADD K:V
            // re route using this node's finger table to store

            Router router = node.getRouter();
            String[] recordData = messageData[1].split(":");

            router.addEntry(Integer.parseInt(recordData[0]), recordData[1]);

        } else if (command.equals("GET")) {
            // GET key:reply_IP
            // re route using this node's finger table to retrieve

            Router router = node.getRouter();
            String[] recordData = messageData[1].split(":");

            router.getEntry(Integer.parseInt(recordData[0]), (recordData[1]));

        } else if (command.equals("REPLY")) {
            // REPLY key:reply_IP
            // retrieve from this node's local data and reply to the given IP

            String[] recordData = messageData[1].split(":");
            int find_key = Integer.parseInt(recordData[0]);
            String reply_IP = recordData[1];

            String response = "PRINT ";

            ArrayList<String> values = (node.getData().containsKey(find_key)? node.getData().get(find_key): new ArrayList<>());

            for (String value : values) {
                response += value + ":";

            }

            // response = PRINT K0:V0 K1:V1
            response = response.substring(0, response.length() - 1) + "\n";

            try (
                    Socket socket = new Socket(reply_IP, Node.COMMON_PORT);
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            ) {

                bw.write(response);
                bw.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (command.equals("PRINT")) {
            // PRINT K0:V0 K1:V1
            // display the retrieved data

            System.out.println("RETRIEVED DATA:");

            if(messageData.length > 1)
                for (String value : messageData[1].split(":")) {
                    System.out.println(value);
                }
        }

    }

    /**
     * This method handles requests as long as the node is active
     * and parses the request message
     */
    public void run() {

        while (node.isActive()) {
            try (Socket socket = serverSocket.accept();
                 BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ) {

                String message = br.readLine().trim();

                if (message.equals("BYE"))
                    break;

                parseMessage(message);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
