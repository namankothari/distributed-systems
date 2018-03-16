package edu.rit.CSCI652.demo;

import java.util.List;

public class Topic {
	private int id;
	private List<String> keywords;
	private String name;


	public String getName(){
		return name;
	}

	public Topic(String name){
		this.name = name;
	}

}
