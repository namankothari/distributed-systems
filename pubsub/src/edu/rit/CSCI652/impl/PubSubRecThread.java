/*
 * PubSubRecThread.java
 *
 * Author: Naman Kothari    nsk2400
 * Author: Atit Gupta       ag3654
 * Author: Akshay Karki     avk1063
 *
 * The receiver thread for the PubSubAgent class.
 */

package edu.rit.CSCI652.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class PubSubRecThread extends Thread {

    private PubSubAgent pubSubAgent;
    private Socket socket;
    private String message;
    private final static String LOG_OUT = "LOGOUT";


    public PubSubRecThread(PubSubAgent pubSubAgent, Socket socket) {
        this.pubSubAgent = pubSubAgent;
        this.socket = socket;
        this.message = null;
    }

    @Override
    public void run() {
        try {

            BufferedReader br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.message = br.readLine().trim();

            while (this.pubSubAgent.isActive) {
                System.out.println(">>> " + message);

                message = br.readLine().trim();

                if (message.equals(LOG_OUT)) {
                    this.pubSubAgent.isActive = false;
                    System.out.println(">>> LOGGED OUT!");
                }
            }

            br.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
