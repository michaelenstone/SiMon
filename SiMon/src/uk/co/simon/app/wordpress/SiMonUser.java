package uk.co.simon.app.wordpress;

import net.bican.wordpress.StringHeader;
import net.bican.wordpress.XmlRpcMapped;

public class SiMonUser extends XmlRpcMapped  implements StringHeader {

	public String projectLimit;
	public String manyUsers;
	public String primaryAccountId;
	public String photoStorage;
	public String accessReports;
	public String nickname;
	public Integer userid;
	public String url;
	public String lastname;
	public String firstname;

	public String getStringHeader() {
		final String TAB = ":";
		return "First name" + TAB + "Last name" + TAB + "Nick name" + TAB + "Url"
		+ TAB + "User ID" + TAB + "Maximum Projects" + TAB +"Many Users" + TAB + "Primary Account ID"
		+ TAB + "Photo Storage" + TAB + "Can Access Reports";
	}

	public int getMaxProjects() {
		return Integer.parseInt(projectLimit);
	}

	public void setMaxProjects(String maxProjects) {
		this.projectLimit = maxProjects;
	}

	public boolean isManyUsers() {
		return Boolean.getBoolean(manyUsers);
	}

	public void setManyUsers(String manyUsers) {
		this.manyUsers = manyUsers;
	}

	public int getPrimaryAccountId() {
		return Integer.parseInt(primaryAccountId);
	}

	public void setPrimaryAccountId(String primaryAccountId) {
		this.primaryAccountId = primaryAccountId;
	}

	public int getPhotoStorage() {
		return Integer.parseInt(photoStorage);
	}

	public void setPhotoStorage(String photoStorage) {
		this.photoStorage = photoStorage;
	}

	public boolean isAccessReports() {
		return Boolean.getBoolean(accessReports);
	}

	public void setAccessReports(String accessReports) {
		this.accessReports = accessReports;
	}

	/**
	 *  @return the nickname
	 */
	public String getNickname() {
		return this.nickname;
	}

	/**
	 * @param nickname the nickname to set
	 */
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	/**
	 * @return the userid
	 */
	public Integer getUserid() {
		return this.userid;
	}

	/**
	 * @param userid the userid to set
	 */
	public void setUserid(Integer userid) {
		this.userid = userid;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return this.url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the lastname
	 */
	public String getLastname() {
		return this.lastname;
	}

	/**
	 * @param lastname the lastname to set
	 */
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	/**
	 * @return the firstname
	 */
	public String getFirstname() {
		return this.firstname;
	}

	/**
	 * @param firstname the firstname to set
	 */
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

}
