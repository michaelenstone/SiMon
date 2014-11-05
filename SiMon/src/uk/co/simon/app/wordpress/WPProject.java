package uk.co.simon.app.wordpress;

import net.bican.wordpress.StringHeader;
import net.bican.wordpress.XmlRpcMapped;
import uk.co.simon.app.sqllite.SQLProject;

public class WPProject extends XmlRpcMapped  implements StringHeader {

	public String Project;
	public String ProjectNumber;
	public String cloudID;

	public String getProject() {
		return Project;
	}

	public String getProjectNumber() {
		return ProjectNumber;
	}

	public void setProject(String Project) {
		this.Project = Project;
	}

	public void setProjectNumber(String ProjectNumber) {
		this.ProjectNumber = ProjectNumber;
	}

	public long getCloudID() {
		return Long.parseLong(cloudID);
	}

	public void setCloudID(long cloudID) {
		this.cloudID = String.valueOf(cloudID);
	}

	public SQLProject toSQLProject(){
		SQLProject project = new SQLProject();
		project.setProject(Project);
		project.setProjectNumber(ProjectNumber);
		project.setCloudID(Long.parseLong(cloudID));
		return project;
	}
	
	@Override
	public String getStringHeader() {
		final String TAB = ":";
		return "Project Name" + TAB + "Project Number" + TAB + "Cloud ID";
	}

}
