/*
 * ServerThread.java
 *
 * Author: Naman Kothari    nsk2400
 * Author: Atit Gupta       ag3654
 * Author: Akshay Karki     avk1063
 *
 * Thread of this class is spawned by the EventManager thread.
 */

package edu.rit.CSCI652.impl;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ServerThread implements Runnable {

    private EventManager eventManager;
    private final int SERVER_PORT = 8080;
    private ServerSocket serverSocket = null;
    private boolean isRunning = true;
    private int contactPort;

    private static int portIndex = 0;
    private static int noOfPorts = 10;
    private static int startPort = 5000;

    private static ArrayList<Integer> availablePorts = new ArrayList<Integer>() {
        {
            for (int i = 0; i < noOfPorts; i++) {
                add(startPort);
                startPort += 1000;
            }
        }
    };

    private static HashMap<Integer, ServerSocket> activeSockets = new HashMap<Integer, ServerSocket>() {
        {
            for (Integer i : availablePorts) {
                try {
                    put(i, new ServerSocket(i));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };


    public ServerThread(EventManager eventManager) {

        this.eventManager = eventManager;

    }

    /**
     * Initialises the common port.
     */
    private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.SERVER_PORT);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port 8080", e);
        }
    }

    @Override
    public void run() {
        openServerSocket();

        while (this.isRunning) {
            Socket clientSocket = null;
            try {
                clientSocket = this.serverSocket.accept();

                this.contactPort = availablePorts.get(portIndex++);
                if (portIndex == noOfPorts)
                    portIndex = 0;


                OutputStream os = clientSocket.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(os);
                BufferedWriter bw = new BufferedWriter(osw);
                bw.write("" + this.contactPort);
                //System.out.println("This client will connect on port: " + this.contactPort);
                bw.flush();
                clientSocket.close();

                new Thread(new WorkerThread(activeSockets.get(this.contactPort), this.eventManager, this.contactPort)).start(); //send port which is allocated

            } catch (IOException e) {
                if (!this.isRunning) {
                    System.out.println("Server Stopped.");
                    return;
                }
                throw new RuntimeException(
                        "Error accepting client connection", e);
            }
        }
    }
}
