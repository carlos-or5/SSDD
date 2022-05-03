package es.um.sisdist.videofaces.models;

import es.um.sisdist.videofaces.backend.dao.models.Video;

public class VideoDTOUtils
{
	public static User fromDTO(VideoDTO vdto)
	{
		return new Video(vdto.getId(),
				vdto.getuserId(),
				vdto.getPstatus(),
				vdto.getDate(),
				vdto.getFilename(),
				);
	}
	
	public static UserDTO toDTO(Video v)
	{
		return new VideoDTO(v.getId(),
				v.getuserId(),
				v.getPstatus(),
				v.getDate(),
				v.getFilename(),
				);
	}
}
