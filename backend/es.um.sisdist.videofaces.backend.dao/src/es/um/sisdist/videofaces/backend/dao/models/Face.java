/**
 *
 */
package es.um.sisdist.videofaces.backend.dao.models;

import java.util.Arrays;

public class Face {
	private String id;
	private String videoid;
	private byte[] imagedata;

	public byte[] getImagedata() {
		return imagedata;
	}

	public void setImagedata(byte[] imagedata) {
		this.imagedata = Arrays.copyOf(imagedata, imagedata.length);
	}

	public Face() {
	}

	public Face(String id, String videoid, byte[] imagedata) {
		this.id = id;
		this.videoid = videoid;
		this.imagedata = Arrays.copyOf(imagedata, imagedata.length);
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