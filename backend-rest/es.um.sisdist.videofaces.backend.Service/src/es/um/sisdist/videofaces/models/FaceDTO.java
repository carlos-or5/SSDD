package es.um.sisdist.videofaces.models;

import java.util.Arrays;

public class FaceDTO {

	private String id;
	private String videoid;
	private byte[] imagedata;
	
	public FaceDTO() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getVideoid() {
		return videoid;
	}

	public void setVideoid(String videoid) {
		this.videoid = videoid;
	}

	public byte[] getImagedata() {
		return imagedata;
	}

	public void setImagedata(byte[] imagedata) {
		this.imagedata = imagedata;
	}
	
	public FaceDTO(String id, String videoid, byte[] imagedata) {
		super();
		this.id = id;
		this.videoid = videoid;
		this.imagedata = Arrays.copyOf(imagedata, imagedata.length);
	}

}
