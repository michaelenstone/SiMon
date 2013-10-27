package uk.co.simon.app.sqllite;

public class SQLReportItem {
	private long id = 0;
	private long reportId = 0;
	private long locationId = 0;
	private String activityOrItem = " ";
	private float progress = 0;
	private String description = " ";
	private String onTime = " ";
	private long cloudID = 0;

	public long getId() {
		return id;
	}

	public long getReportId() {
		return reportId;
	}

	public long getLocationId() {
		return locationId;
	}

	public String getReportItem() {
		return activityOrItem;
	}

	public float getProgress() {
		return progress;
	}

	public String getDescription() {
		return description;
	}

	public String getOnTIme() {
		return onTime;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setReportId(long reportId) {
		this.reportId = reportId;
	}

	public void setLocationId(long locationId) {
		this.locationId = locationId;
	}

	public void setReportItem(String activityOrItem) {
		this.activityOrItem = activityOrItem;
	}

	public void setProgress(float progress) {
		this.progress = progress;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setOnTIme(String onTime) {
		this.onTime = onTime;
	}

	// Will be used by the ArrayAdapter in the ListView
	@Override
	public String toString() {
		return activityOrItem;
	}

	
	public long getCloudID() {
		return cloudID;
	}

	
	public void setCloudID(long cloudID) {
		this.cloudID = cloudID;
	}
}
