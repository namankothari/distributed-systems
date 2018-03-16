/*
 * PubSubSendThread.java
 *
 * Author: Naman Kothari    nsk2400
 * Author: Atit Gupta       ag3654
 * Author: Akshay Karki     avk1063
 *
 *
 * The sender thread for PubSubAgent class.
 */
package edu.rit.CSCI652.impl;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class PubSubSendThread extends Thread {

    private PubSubAgent pubSubAgent;
    private Socket socket;
    private String message;
    private Scanner sc;

    private String userId;
    private String action;
    private String topic;
    private String content;
    private String title;

    public PubSubSendThread(PubSubAgent pubSubAgent, Socket socket, Scanner sc) {
        this.pubSubAgent = pubSubAgent;
        this.socket = socket;
        this.sc = sc;
        this.message = null;
    }

    /*
     * returns the message
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * set the message
     *
     * @param message
     */

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Builds the message.
     *
     * @param userInput
     */
    private void buildMessage(String userInput) {
        setAction(Integer.parseInt(userInput));

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(this.action);

        stringBuilder.append("|USERID|");
        stringBuilder.append(this.userId);

        stringBuilder.append("|TOPIC|");
        stringBuilder.append(this.topic);

        stringBuilder.append("|CONTENT|");
        stringBuilder.append(this.content);

        stringBuilder.append("|TITLE|");
        stringBuilder.append(this.title);

        stringBuilder.append("\n");

        this.message = stringBuilder.toString();
    }

    /**
     * Sets action based on choice.
     *
     * @param choice
     */
    public void setAction(int choice) {


        switch (choice) {
            case 1:
                this.action = "SUBSCRIBE";
                System.out.println("To which topic do you want to subscribe to? ");
                System.out.println("One topic at a time please without spaces...");
                this.topic = sc.nextLine();
                break;

            case 2:
                this.action = "UNSUBSCRIBE";
                System.out.println("From which topic do you want to unsubscribe from? ");
                System.out.println("To unsubscribe from all topics, press '-' as topic name.");
                this.topic = sc.nextLine();
                break;


            case 3:
                this.action = "ADVERTISE";
                System.out.println("Which topic do you want to advertise? ");
                this.topic = sc.nextLine();
                break;

            case 4:
                this.action = "PUBLISH";
                System.out.println("Under which topic do you want to publish? ");
                this.topic = sc.nextLine();
                System.out.println("What is the title of the topic? ");
                this.title = sc.nextLine();
                System.out.println("What is the content of the topic? ");
                this.content = sc.nextLine();
                break;

            case 5:
                this.action = "SUBLIST";
                break;

            case 6:
                this.action = "ALLTOPICS";
                break;
            case 7:
                this.action = "LOGOUT";
                break;
            default:
                System.out.println("Error");
                System.exit(1);
        }
    }

    /**
     * Used for logging in the user.
     *
     * @param bw
     */
    public void userLogin(BufferedWriter bw) {

        String prompt = ">>> Enter 1 for login, 2 for sign up";
        System.out.println(prompt);

        int choice = Integer.parseInt(sc.nextLine());

        switch (choice) {

            case 1:

                System.out.println("Enter userId:");
                this.userId = sc.nextLine();

                System.out.println("Enter Password:");
                String password = sc.nextLine();

                String message = "LOGIN|" + userId + "|" + password + "\n";

                try {
                    bw.write(message);
                    bw.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;

            case 2:

                System.out.println("Enter new userId:");
                this.userId = sc.nextLine();

                System.out.println("Enter new Password:");
                password = sc.nextLine();

                message = "NEW|" + userId + "|" + password + "\n";

                try {
                    bw.write(message);
                    bw.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                System.out.println("Error");
                System.exit(1);
        }
    }

    @Override
    public void run() {

        try {

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            //login or sign up
            userLogin(bw);

            while (this.pubSubAgent.isActive) {

                System.out.println("Menu:");
                System.out.println("1. Subscribe to a topic from the list");
                System.out.println("2. Unsubscribe from a topic or all topics");
                System.out.println("3. Advertise a topic");
                System.out.println("4. Publish a new article");
                System.out.println("5. Get a list of all subscribed topics");
                System.out.println("6. List of all topics");
                System.out.println("7. Logout");
                System.out.println(">>>Choose a number between 1-7. Only... ");

                String userInput = sc.nextLine();

                buildMessage(userInput);

                if (getMessage() != null) {
                    bw.write(getMessage());
                    bw.flush();
                    setMessage(null);
                }


                Thread.sleep(1000);
            }

            bw.close();
            socket.close();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}


