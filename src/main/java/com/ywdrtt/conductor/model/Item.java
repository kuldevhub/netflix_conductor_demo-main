package com.ywdrtt.conductor.model;

public class Item {

	private  String name;
	private int count;
	private String status;
	private int cost;
	
	  // Default constructor (required for Jackson to deserialize)
    public Item() {
    }
    
	public Item(String name, int count, String status, int cost) {
		// TODO Auto-generated constructor stub
		this.name = name;
		this.count = count;
		this.status = status;
		this.cost = cost;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public int getCost() {
		return cost;
	}
	public void setCost(int cost) {
		this.cost = cost;
	}

}
