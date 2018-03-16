/*
 * EventManager.java
 *
 * Author: Naman Kothari    nsk2400
 * Author: Atit Gupta       ag3654
 * Author: Akshay Karki     avk1063
 *
 * This class has the main methods for EventManager and calls the threads associated for the same.
 */
package edu.rit.CSCI652.impl;

import edu.rit.CSCI652.demo.Event;
import edu.rit.CSCI652.demo.Topic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EventManager {

    private ConcurrentHashMap<String, ArrayList<String>> topicSubscribersMap;
    private HashSet<String> topicsSet;
    private ConcurrentHashMap<String, String> subscriberidPwdMap;
    private ConcurrentHashMap<String, SubWorkerSendThread> activeUserMap;
    private ConcurrentHashMap<String, ArrayList<String>> userNotificationMap;

    public EventManager() {
        topicSubscribersMap = new ConcurrentHashMap<>();
        topicsSet = new HashSet<>();
        subscriberidPwdMap = new ConcurrentHashMap<>();
        activeUserMap = new ConcurrentHashMap<>();
        userNotificationMap = new ConcurrentHashMap<>();
    }

    public ConcurrentHashMap<String, ArrayList<String>> getTopicSubscribersMap() {
        return this.topicSubscribersMap;
    }

    /*
     * Start the repo service
     */
    private void startService() {
        Thread listSubscribers = new Thread(new ListSubscribers(this));
        listSubscribers.start();

        Thread serverThread = new Thread(new ServerThread(this));
        serverThread.start();
    }

    /*
     * get user password
     */
    public String getUserPassword(String userId) {
        return subscriberidPwdMap.getOrDefault(userId, null);
    }

    /*
     * create new user
     */
    public void createUser(String userId, String password) {
        subscriberidPwdMap.put(userId, password);
    }

    /*
     * keep track of active users
     */
    public void addActiveUsers(String userId, SubWorkerSendThread sendThread) {

        activeUserMap.put(userId, sendThread);
    }

    /*
     *  remove inactive users
     */
    public void removeInActiveUsers(String userId) {

        activeUserMap.remove(userId);
    }

    /*
     * notify all subscribers subscribed to event topic if active, else...
     */
    public void notifySubscribers(Event event, String userID) {


        String topicName = event.getTopic().getName();
        ArrayList<String> subscribers = topicSubscribersMap.getOrDefault(topicName, new ArrayList<>());


        if (!subscribers.contains(userID)) {
            SubWorkerSendThread sendThread = activeUserMap.get(userID);
            sendThread.setMessage("Article Published");
            sendThread.setToSend(true);

        }

        for (String subscriber : subscribers) {


            if (activeUserMap.containsKey(subscriber)) {
                SubWorkerSendThread sendThread = activeUserMap.get(subscriber);
                String message = "New Article: Title: " + event.getTitle() + "\t" + "Content: " + event.getContent();
                sendThread.setMessage(message);
                sendThread.setToSend(true);
                // create a message based on Event..


            } else {

                ArrayList<String> notifications = userNotificationMap.getOrDefault(subscriber, new ArrayList<>());
                String message = "New Article: Title: " + event.getTitle() + "\t" + "Content: " + event.getContent();
                notifications.add(message); //add the message
                userNotificationMap.put(subscriber, notifications);
            }
        }
    }

    /*
     * notify all subscribers
     */


    public void notifyAllSubscribers(String topic, String userID) {
        Set<String> subscribers = subscriberidPwdMap.keySet();
        for (String subscriber : subscribers) {
            if (activeUserMap.containsKey(subscriber)) {


                SubWorkerSendThread sendThread = activeUserMap.get(subscriber);

                // create a message based on Event..
                String message = "New Topic: " + topic + " has been advertised!";
                sendThread.setMessage(message);
                sendThread.setToSend(true);

            } else {
                ArrayList<String> notifications = userNotificationMap.getOrDefault(subscriber, new ArrayList<>());
                String message = "New Topic: " + topic + " has been advertised!";
                notifications.add(message); //add the message
                userNotificationMap.put(subscriber, notifications);
            }
        }


    }
    /*
     * notify when user logs in
     */

    public void notifySubscribersOnLogin(String userId) {
        if (userNotificationMap.containsKey(userId)) {
            ArrayList<String> notifications = userNotificationMap.get(userId);
            String message = "Notifications since your last login... \t";

            for (String notification : notifications) {
                message += notification + "\t\t";
            }

            SubWorkerSendThread sendThread = activeUserMap.get(userId);
            sendThread.setMessage(message);
            sendThread.setToSend(true);

            userNotificationMap.remove(userId);
        }
    }

    /*
     * add new topic when received advertisement of new topic
     */
    public boolean addTopic(Topic topic, String userID) {
        if (!topicsSet.contains(topic.getName())) {
            topicsSet.add(topic.getName());

            notifyAllSubscribers(topic.getName(), userID);
            return true;
        }
        return false;
    }

    /*
     * add subscriber to the specified Topic
     */
    public void addSubscriber(Topic topic, String subscriberId) {

        String topicName = topic.getName();
        SubWorkerSendThread sendThread = activeUserMap.get(subscriberId);
        if (topicsSet.contains(topicName)) {
            ArrayList<String> subscribers = topicSubscribersMap.getOrDefault(topicName, new ArrayList<>());
            if (subscribers.contains(subscriberId)) {
                sendThread.setMessage("You are already Subscribed!");
                sendThread.setToSend(true);
            } else {
                subscribers.add(subscriberId);
                topicSubscribersMap.put(topicName, subscribers);
                sendThread.setMessage("Subscribed!");
                sendThread.setToSend(true);
            }
        } else {
            sendThread.setMessage("Topic not advertised!");
            sendThread.setToSend(true);
        }
    }


    /*
     * remove subscriber from all Topics
     */
    public void removeSubscriber(String subscriberId) {

        for (Map.Entry<String, ArrayList<String>> entry : topicSubscribersMap.entrySet()) {

            ArrayList<String> subscribers = entry.getValue();

            if (subscribers.contains(subscriberId)) {
                subscribers.remove(subscriberId);
                topicSubscribersMap.put(entry.getKey(), subscribers);
            }
        }
    }


    /*
     * remove subscriber from specified Topic
     */
    public void removeSubscriber(Topic topic, String subscriberId) {
        String topicName = topic.getName();
        ArrayList<String> subscribers = topicSubscribersMap.getOrDefault(topicName, new ArrayList<>());

        if (subscribers.contains(subscriberId)) {
            subscribers.remove(subscriberId);
            topicSubscribersMap.put(topicName, subscribers);
        }
    }

    /*
     * show the list of subscriber for a specified topic
     */
    public ArrayList<String> showSubscribers(Topic topic) {
        String topicName = topic.getName();
        ArrayList<String> subscribers = topicSubscribersMap.getOrDefault(topicName, new ArrayList<>());
        return subscribers;
    }

    /*
     *   get user subscribed topics
     */
    public String getSubscribedTopics(String subscriberId) {

        String topics = "Subscribed topics... \t";
        for (Map.Entry<String, ArrayList<String>> pair : topicSubscribersMap.entrySet()) {

            if (pair.getValue().contains(subscriberId))
                topics += pair.getKey() + "\t";
        }
        return topics;
    }

    /*
     * get a list of all topics advertised.
     */
    public String getAllTopics() {
        String allTopics = "Topic list: \t";
        for (String topic : topicsSet)
            allTopics += topic + "\t";
        return allTopics;
    }

    public boolean checkTopic(String topic) {
        return topicsSet.contains(topic);
    }

    /*
     * the main method.
     */
    public static void main(String[] args) {
        new EventManager().startService();
    }
}