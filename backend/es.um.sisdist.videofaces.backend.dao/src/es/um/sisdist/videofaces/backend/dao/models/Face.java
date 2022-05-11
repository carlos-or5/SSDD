/**
 *
 */
package es.um.sisdist.videofaces.backend.dao.models;

public class Face {
	private String id;
	private String videoid;

	public Face() {
	}

	public Face(String id, String videoid) {
		super();
		this.id = id;
		this.videoid = videoid;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the id
	 */
	public String getVideoid() {
		return videoid;
	}

	/**
	 * @param id the id to set
	 */
	public void setVideoid(String videoid) {
		this.videoid = videoid;
	}
}