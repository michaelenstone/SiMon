package uk.co.simon.app.wordpress;

import net.bican.wordpress.StringHeader;
import net.bican.wordpress.XmlRpcMapped;

public class SiMonUser extends XmlRpcMapped  implements StringHeader {

	public String photoStorage;
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

	
	public int getPhotoStorage() {
		int output = 0;
		try {
			output = Integer.parseInt(photoStorage);
		} catch (NumberFormatException nfe) {
			output = 0;
		}
		return output;
	}

	public void setPhotoStorage(String photoStorage) {
		this.photoStorage = photoStorage;
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
