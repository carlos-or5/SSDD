package es.um.sisdist.videofaces.models;

import es.um.sisdist.videofaces.backend.dao.models.Video.PROCESS_STATUS;

public class VideoDTO {

	private String id;
	private String userid;
	private PROCESS_STATUS pstatus;
	private String date;
	private String filename;

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the userId
	 */
	public String getUserid() {
		return userid;
	}

	/**
	 * @return the process status
	 */
	public PROCESS_STATUS getPstatus() {
		return pstatus;
	}

	/**
	 * @return the date
	 */
	public String getDate() {
		return date;
	}

	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @param userid the user id to set
	 */
	public void setuserId(String userid) {
		this.userid = userid;
	}

	/**
	 * @param status set the process status
	 */
	public void setPstatus(PROCESS_STATUS pstatus) {
		this.pstatus = pstatus;
	}

	/**
	 * @param date set the date
	 */
	public void setDate(String date) {
		this.date = date;
	}

	/**
	 * @param filename set the filename
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

	public VideoDTO(String id, String userid, PROCESS_STATUS pstatus, String date, String filename) {
		super();
		this.id = id;
		this.userid = userid;
		this.pstatus = pstatus;
		this.date = date;
		this.filename = filename;
	}

	public VideoDTO() {
	}

}
