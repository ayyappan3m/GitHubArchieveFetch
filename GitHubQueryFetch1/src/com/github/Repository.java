package com.github;

/*Repository information*/
public class Repository {
	private int id;
	private String name;
	private String Url;
	public Repository getInstance(int id, String name, String url) {
		setId(id);
		setName(name);
		setUrl(url);
		return this;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUrl() {
		return Url;
	}
	public void setUrl(String url) {
		Url = url;
	}
	
	
}
