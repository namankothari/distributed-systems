/*
 * PubSubAgent.java
 *
 * Author: Naman Kothari    nsk2400
 * Author: Atit Gupta       ag3654
 * Author: Akshay Karki     avk1063
 *
 * This class has the main methods for the PubSubAgent and calls the threads associated for the same.
 */

package edu.rit.CSCI652.impl;

import edu.rit.CSCI652.demo.Event;
import edu.rit.CSCI652.demo.Publisher;
import edu.rit.CSCI652.demo.Subscriber;
import edu.rit.CSCI652.demo.Topic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;

public class PubSubAgent implements Publisher, Subscriber {

    private final static int SERVER_PORT = 8080;
    private static String LOCAL_ADDRESS = "localhost";
    private Socket socket;
    protected boolean isActive;


    @Override
    public void subscribe(Topic topic) {
        // TODO Auto-generated method stub

    }

    @Override
    public void subscribe(String keyword) {
        // TODO Auto-generated method stub

    }

    @Override
    public void unsubscribe(Topic topic) {
        // TODO Auto-generated method stub

    }

    @Override
    public void unsubscribe() {
        // TODO Auto-generated method stub

    }

    @Override
    public void listSubscribedTopics() {
        // TODO Auto-generated method stub

    }

    @Override
    public void publish(Event event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void advertise(Topic newTopic) {
        // TODO Auto-generated method stub

    }

    /**
     * Connects to server on the common port.
     *
     * @param assignedPort
     */

    public void connectToServer(int assignedPort) {

        //System.out.println("Trying to connect on " + assignedPort);
        try {

            this.socket = new Socket(LOCAL_ADDRESS, assignedPort);
            //System.out.println("Client thread connected at " + socket.getPort());
            this.isActive = true;

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Gets the port to connect to the server
     *
     * @return
     */
    public int getPortFromServer() {

        String assignedPort = null;

        try {

            this.socket = new Socket(LOCAL_ADDRESS, SERVER_PORT);
            //System.out.println("Client thread connected at port " + socket.getPort());


            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            assignedPort = br.readLine();

            //System.out.println("Port received from server is: " + assignedPort);
            br.close();
            this.socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return Integer.parseInt(assignedPort);

    }

    /**
     * The main method.
     *
     * @param args
     */

    public static void main(String[] args) {

        if (args.length > 0) {
            LOCAL_ADDRESS = args[0];
        }

        PubSubAgent pubSubAgent = new PubSubAgent();

        int assignedPort = pubSubAgent.getPortFromServer();
        pubSubAgent.connectToServer(assignedPort);

        Scanner sc = new Scanner(System.in);

        PubSubRecThread rec = new PubSubRecThread(pubSubAgent, pubSubAgent.socket);
        PubSubSendThread send = new PubSubSendThread(pubSubAgent, pubSubAgent.socket, sc);

        rec.start();
        send.start();
    }
}
