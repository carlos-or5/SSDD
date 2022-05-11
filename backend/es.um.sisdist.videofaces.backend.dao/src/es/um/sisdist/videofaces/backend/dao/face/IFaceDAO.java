package es.um.sisdist.videofaces.backend.dao.face;

import java.io.InputStream;
import java.util.Optional;

import es.um.sisdist.videofaces.backend.dao.models.Video;
import es.um.sisdist.videofaces.backend.dao.models.Video.PROCESS_STATUS;

public interface IFaceDAO
{
    public Optional<Video> getFaceById(String id);

    // Get stream of video data
    public InputStream getStreamForFace(String id);

    public Optional<Video> storeFace(String id, String videoid);
}
