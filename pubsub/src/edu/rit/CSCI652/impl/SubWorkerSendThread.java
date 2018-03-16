/*
 * SubWorkerRecThread.java
 *
 * Author: Naman Kothari    nsk2400
 * Author: Atit Gupta       ag3654
 * Author: Akshay Karki     avk1063
 *
 * This thread is the Sending thread for the EventManager.
 *
 */


package edu.rit.CSCI652.impl;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class SubWorkerSendThread extends Thread {
    private EventManager eventManager;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private int port;
    private SubWorkerRecThread recThread;
    private boolean isActive;
    private boolean toSend; // set this to true if you need to send something..
    private String message; // set message here

    public SubWorkerSendThread(EventManager eventManager, ServerSocket serverSocket, Socket clientSocket, int port) {
        this.eventManager = eventManager;
        this.serverSocket = serverSocket;
        this.clientSocket = clientSocket;
        this.port = port;
        this.isActive = true;
    }

    /**
     * Sets the connection between the sending and receiving threads.
     *
     * @param recThread
     */
    public void setRecThread(SubWorkerRecThread recThread) {
        this.recThread = recThread;
    }

    /**
     * This is used to check if there's a message to send.
     *
     * @return
     */
    public boolean getToSend() {
        return this.toSend;
    }

    /**
     * Set the boolean variable.
     *
     * @param toSend
     */
    public void setToSend(boolean toSend) {
        this.toSend = toSend;
    }

    /**
     * Sets the message to send.
     *
     * @param message
     */
    public void setMessage(String message) {
        this.message = message + "\n";
    }

    /**
     * Gets the message to send.
     *
     * @return
     */

    public String getMessage() {
        return this.message;
    }

    @Override
    public void run() {
        try {

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            while (isActive) {

                if (getToSend()) {

                    bw.write(getMessage());

                    bw.flush();

                    if (getMessage().equals("LOGOUT")) {

                        isActive = false;
                    } else {
                        setMessage(null);
                        setToSend(false);
                    }
                }

                Thread.sleep(500);
            }

            bw.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

}
