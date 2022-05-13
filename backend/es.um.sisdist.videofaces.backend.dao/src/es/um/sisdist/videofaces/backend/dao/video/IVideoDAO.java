package es.um.sisdist.videofaces.backend.dao.video;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import es.um.sisdist.videofaces.backend.dao.models.Video;
import es.um.sisdist.videofaces.backend.dao.models.Video.PROCESS_STATUS;

public interface IVideoDAO
{
    public Optional<Video> getVideoById(String id);

    // Get stream of video data
    public InputStream getStreamForVideo(String id);

    public Video.PROCESS_STATUS getVideoStatus(String id);

    public Optional<Video> storeVideo(String userid, PROCESS_STATUS pstatus, String date, String filename);
    
    public void setProcessed(String videoid);

    public List<Optional<Video>> getVideosByUserId(String id);
}
