package uk.co.simon.app.sqllite;

public class SQLReport {
	private long id;
	private long projectId = 0;
	private String reportDate = " ";
	private boolean reportType = true;
	private String supervisor = " ";
	private String reportRef = " ";
	private String weather = " ";
	private String temp = " ";
	private long tempType = 0;
	private String PDF = " ";
	private boolean hasPDF = false;
	private long cloudID = 0;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getTempType() {
		return tempType;
	}

	//1=Farenheit 0=Celcius
	public void setTempType(long tempType) {
		this.tempType = tempType;
	}

	public String getTemp() {
		return temp;
	}

	public void setTemp(String temp) {
		this.temp = temp;
	}

	public String getWeather() {
		return weather;
	}

	public void setWeather(String weather) {
		this.weather = weather;
	}

	public String getReportRef() {
		return reportRef;
	}

	public void setReportRef(String reportRef) {
		this.reportRef = reportRef;
	}

	public String getSupervisor() {
		return supervisor;
	}

	public void setSupervisor(String supervisor) {
		this.supervisor = supervisor;
	}

	public boolean getReportType() {
		return reportType;
	}

	public void setReportType(boolean reportType) {
		this.reportType = reportType;
	}

	public String getReportDate() {
		return reportDate;
	}

	public void setReportDate(String reportDate) {
		this.reportDate = reportDate;
	}

	public long getProjectId() {
		return projectId;
	}

	public void setProjectId(long projectId) {
		this.projectId = projectId;
	}

	public String getPDF() {
		return PDF;
	}

	public void setPDF(String Path) {
		PDF = Path;
		if (Path != null) {
			hasPDF = true;
		}
	}

	public boolean hasPDF() {
		return this.hasPDF;
	}

	public long getCloudID() {
		return cloudID;
	}

	public void setCloudID(long cloudID) {
		this.cloudID = cloudID;
	}
}
