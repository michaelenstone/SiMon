package uk.co.simon.app.wordpress;

import net.bican.wordpress.StringHeader;
import net.bican.wordpress.XmlRpcMapped;
import uk.co.simon.app.sqllite.SQLLocation;

public class WPLocation extends XmlRpcMapped  implements StringHeader {
	
	public String Location;
	public String ProjectID;
	public String cloudID;
	
	public String getLocation() {
		return Location;
	}

	public String getProjectID() {
		return ProjectID;
	}

	public void setLocation(String Location) {
		this.Location = Location;
	}

	public void setProjectID(String ProjectID) {
		this.ProjectID = ProjectID;
	}

	public long getCloudID() {
		return Long.parseLong(cloudID);
	}

	public void setCloudID(long cloudID) {
		this.cloudID = String.valueOf(cloudID);
	}

	public SQLLocation toSQLLocation(){
		SQLLocation location = new SQLLocation();
		location.setLocation(Location);
		location.setLocationProjectId(Long.parseLong(ProjectID));
		location.setCloudID(Long.parseLong(cloudID));
		return location;
	}
	
	@Override
	public String getStringHeader() {
		final String TAB = ":";
		return "Location Name" + TAB + "Location ID" + TAB + "Cloud ID";
	}

}
