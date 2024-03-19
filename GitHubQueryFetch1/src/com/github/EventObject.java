package com.github;
/* Event information*/
public class EventObject {
	private String id, type;
	private String createdBy;
	private Repository repository;
	
	public EventObject getInstance(String id, String type, String createdBy, Repository repository) {
		this.id = id;
		this.type = type;
		this.createdBy = createdBy;
		this.repository = repository;
		return this;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Repository getRepository() {
		return repository;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}
	public String[] toStringArray() {
        return new String[]{id, type, createdBy, String.valueOf(repository.getId()), repository.getName(), repository.getUrl()};
    }
}
