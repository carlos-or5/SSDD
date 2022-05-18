package es.um.sisdist.videofaces.models;

import es.um.sisdist.videofaces.backend.dao.models.Face;

public class FaceDTOUtils {
	public static Face fromDTO(FaceDTO fdto) {
		return new Face(fdto.getId(), fdto.getVideoid(), fdto.getImagedata());
	}

	public static FaceDTO toDTO(Face f) {
		return new FaceDTO(f.getId(), f.getVideoid(), f.getImagedata());
	}
}
