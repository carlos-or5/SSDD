package es.um.sisdist.videofaces.backend.dao.face;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import es.um.sisdist.videofaces.backend.dao.models.Face;

public interface IFaceDAO {
	public Optional<Face> getFaceById(String id);

	// Get stream of video data
	public InputStream getStreamForFace(String id);

	public Optional<Face> storeFace(String videoid, String path);

	public List<Optional<Face>> getFaceByVideoId(String videoid);

}
