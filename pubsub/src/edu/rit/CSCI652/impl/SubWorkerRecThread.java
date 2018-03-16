/*
 * SubWorkerRecThread.java
 *
 * Author: Naman Kothari    nsk2400
 * Author: Atit Gupta       ag3654
 * Author: Akshay Karki     avk1063
 *
 * This thread is the receiving thread for the EventManager.
 *
 */

package edu.rit.CSCI652.impl;

import edu.rit.CSCI652.demo.Event;
import edu.rit.CSCI652.demo.Topic;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class SubWorkerRecThread extends Thread {
    private EventManager eventManager;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private int port;
    private SubWorkerSendThread sendThread;
    private boolean isActive;
    private String userId;

    public SubWorkerRecThread(EventManager eventManager, ServerSocket serverSocket, Socket clientSocket, int port) {
        this.eventManager = eventManager;
        this.serverSocket = serverSocket;
        this.clientSocket = clientSocket;
        this.port = port;
        this.isActive = true;
    }

    /**
     * Creates a connection between the sending and receiving threads.
     *
     * @param sendThread
     */
    public void setSendThread(SubWorkerSendThread sendThread) {
        this.sendThread = sendThread;
    }


    @Override
    public void run() {
        try {
            // string type
            // this.action|USER_ID|id|topic|actual_topic|content|actual_content|title|actual_title
            BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            while (isActive) {

                String message = br.readLine().trim();
                String[] details = message.split("\\|");
                switch (details[0]) {

                    case "LOGIN":
                        this.userId = details[1];
                        String password = details[2];

                        String verification = eventManager.getUserPassword(userId);

                        if (password.equals(verification)) {

                            sendThread.setMessage("Logged in!");
                            sendThread.setToSend(true);

                            eventManager.addActiveUsers(userId, sendThread);

                            eventManager.notifySubscribersOnLogin(userId);


                        } else {

                            sendThread.setMessage("Authentication Failed!");
                            sendThread.setToSend(true);

                        }

                        break;

                    case "NEW":

                        this.userId = details[1];
                        password = details[2];

                        eventManager.createUser(userId, password);

                        sendThread.setMessage("Account created!");
                        sendThread.setToSend(true);

                        eventManager.addActiveUsers(userId, sendThread);

                        break;

                    case "LOGOUT":

                        sendThread.setMessage("LOGOUT");
                        sendThread.setToSend(true);

                        eventManager.removeInActiveUsers(userId);

                        this.isActive = false;

                        break;


                    case "SUBSCRIBE":
                        this.userId = details[2];
                        String topic = details[4];
                        eventManager.addSubscriber(new Topic(topic), this.userId);


                        break;

                    case "UNSUBSCRIBE":

                        this.userId = details[2];
                        topic = details[4];
                        if (topic.equals("-")) {
                            eventManager.removeSubscriber(userId);
                        } else {
                            eventManager.removeSubscriber(new Topic(topic), userId);
                        }

                        sendThread.setMessage("Unsubscribed!");
                        sendThread.setToSend(true);


                        break;

                    case "ADVERTISE":

                        this.userId = details[2];
                        topic = details[4];

                        //handle already advertised

                        boolean flag = eventManager.addTopic(new Topic(topic), userId);

                        if (!flag) {
                            sendThread.setMessage(topic + " is already advertised!");
                            sendThread.setToSend(true);
                        }

                        break;

                    case "SUBLIST":


                        this.userId = details[2];

                        String topics = eventManager.getSubscribedTopics(userId);
                        sendThread.setMessage(topics);
                        sendThread.setToSend(true);

                        break;

                    case "PUBLISH":

                        topic = details[4];
                        String content = details[6];
                        String title = details[8];

                        if(eventManager.checkTopic(topic)){
                            Event event = new Event(new Topic(topic), content, title);
                            eventManager.notifySubscribers(event, userId);
                        }
                        else{
                            sendThread.setMessage("Topic is not advertised");
                            sendThread.setToSend(true);
                        }

                        break;

                    case "ALLTOPICS":
                        String alltopics = eventManager.getAllTopics();
                        sendThread.setMessage(alltopics);
                        sendThread.setToSend(true);
                        break;
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
