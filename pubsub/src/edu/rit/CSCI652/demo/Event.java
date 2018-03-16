package edu.rit.CSCI652.demo;

import java.util.List;

public class Event {
	private int id;
	private Topic topic;
	private String title;
	private String content;


	public Event(Topic topic, String content, String title){
		this.topic = topic;
		this.title = title;
		this.content = content;

	}

	public Topic getTopic() {
		return topic;
	}

	@Override
	public String toString() {
		return "Title: " + title + " Content: " + content;
	}

	public String getTitle(){
		return title;
	}

	public String getContent(){
		return content;
	}
}

