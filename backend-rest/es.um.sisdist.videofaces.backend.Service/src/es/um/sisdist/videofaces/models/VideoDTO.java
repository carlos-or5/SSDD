package es.um.sisdist.videofaces.models;

public class VideoDTO {
    
    private String id;
    private String userid;


    public String getId()
	{
		return id;
	}

    public String getuserId()
	{
		return userid;
	}

    public void setId(String id)
	{
		this.id = id;
	}

    public void setuserId(String userid)
	{
		this.userid = userid;
	}


    public VideoDTO(String id, String userid)
	{
		super();
		this.id = id;
		this.userid = userid;
	}

	public VideoDTO()
	{
	}

}
