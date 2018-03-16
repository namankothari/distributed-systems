/*
 * SubWorkerRecThread.java
 *
 * Author: Naman Kothari    nsk2400
 * Author: Atit Gupta       ag3654
 * Author: Akshay Karki     avk1063
 *
 * This thread initialises the Sending and Receiving threads for EventManager.
 *
 */

package edu.rit.CSCI652.impl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class WorkerThread implements Runnable {

    private Socket clientSocket;
    private EventManager eventManager;
    int contactPort;
    ServerSocket serverSocket;

    public WorkerThread(ServerSocket serverSocket, EventManager eventManager, int contactPort) {
        this.serverSocket = serverSocket;
        this.eventManager = eventManager;
        this.contactPort = contactPort;
    }

    //create server socket on the given port
    @Override
    public void run() {
        //System.out.println("\t\tWorker thread called...");

        while (true) {

            try {
                this.clientSocket = this.serverSocket.accept();

                SubWorkerSendThread send = new SubWorkerSendThread(this.eventManager, this.serverSocket, this.clientSocket, this.contactPort);
                SubWorkerRecThread rec = new SubWorkerRecThread(this.eventManager, this.serverSocket, this.clientSocket, this.contactPort);

                send.setRecThread(rec);
                rec.setSendThread(send);

                send.start();
                rec.start();


            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }
}
