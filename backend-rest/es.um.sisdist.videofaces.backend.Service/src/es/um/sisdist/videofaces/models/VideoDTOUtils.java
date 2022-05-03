package es.um.sisdist.videofaces.models;

import es.um.sisdist.videofaces.backend.dao.models.Video;

public class VideoDTOUtils
{
	public static Video fromDTO(VideoDTO vdto)
	{
		return new Video(vdto.getId(),
				vdto.getUserid(),
				vdto.getPstatus(),
				vdto.getDate(),
				vdto.getFilename()
				);
	}
	
	public static VideoDTO toDTO(Video v)
	{
		return new VideoDTO(v.getId(),
				v.getUserid(),
				v.getPstatus(),
				v.getDate(),
				v.getFilename()
				);
	}
}
