package uk.co.simon.app.sqllite;

public class SQLProject {
	private long id;
	private String Project;
	private String ProjectNumber;
	private long cloudID = 0;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getProject() {
		return Project;
	}

	public String getProjectNumber() {
		return ProjectNumber;
	}

	public void setProject(String Project) {
		this.Project = Project;
	}

	// Will be used by the ArrayAdapter in the ListView
	@Override
	public String toString() {
		return Project;
	}

	public void setProjectNumber(String ProjectNumber) {
		this.ProjectNumber = ProjectNumber;
	}
	
	public long getCloudID() {
		return cloudID;
	}
	
	public void setCloudID(long cloudID) {
		this.cloudID = cloudID;
	}

}
