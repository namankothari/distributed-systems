/*
 * EventManager.java
 *
 * Author: Naman Kothari    nsk2400
 * Author: Atit Gupta       ag3654
 * Author: Akshay Karki     avk1063
 *
 * This Thread waits for the user input to print all subscribers.
 */

package edu.rit.CSCI652.impl;

import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class ListSubscribers implements Runnable {
    private EventManager eventManager;
    private ConcurrentHashMap<String, ArrayList<String>> topicSubscribersMap;
    private StringBuilder stringBuilder;

    public ListSubscribers(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @Override
    public void run() {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("Press any key to list all subscribers ");
            String input = sc.nextLine();

            topicSubscribersMap = eventManager.getTopicSubscribersMap();

            for (Map.Entry<String, ArrayList<String>> entry : topicSubscribersMap.entrySet()) {

                ArrayList<String> subscribers = entry.getValue();
                System.out.print("Topic: " + entry.getKey() + "\t");
                stringBuilder = new StringBuilder();

                stringBuilder.append("{ ");

                for (String s : subscribers) {
                    stringBuilder.append(s);
                    stringBuilder.append(" ");
                }
                stringBuilder.append("}");
                System.out.println("Subscribers: " + stringBuilder);
            }

        }

    }
}

