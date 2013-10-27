package uk.co.simon.app.sqllite;

public class SQLLocation {
	private long id;
	private long ProjectId;
	private String Location;
	private long cloudID = 0;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Long getProjectId() {
		return ProjectId;
	}

	public String getLocation() {
		return Location;
	}

	public void setLocation(String Location) {
		this.Location = Location;
	}

	public void setLocationProjectId(Long ProjectId) {
		this.ProjectId = ProjectId;
	}

	// Will be used by the ArrayAdapter in the ListView
	@Override
	public String toString() {
		return Location;
	}

	
	public long getCloudID() {
		return cloudID;
	}

	
	public void setCloudID(long cloudID) {
		this.cloudID = cloudID;
	}
}
