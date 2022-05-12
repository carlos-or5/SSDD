package es.um.sisdist.videofaces.backend.facedetect;

import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplay.EndAction;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.VideoPositionListener;
import org.openimaj.video.xuggle.XuggleVideo;

import es.um.sisdist.videofaces.backend.dao.DAOFactoryImpl;
import es.um.sisdist.videofaces.backend.dao.IDAOFactory;
import es.um.sisdist.videofaces.backend.dao.models.Face;
import es.um.sisdist.videofaces.backend.dao.video.IVideoDAO;
import es.um.sisdist.videofaces.backend.dao.face.IFaceDAO;

/**
 * OpenIMAJ Hello world!
 *
 */
public class VideoFaces extends Thread {
	IDAOFactory daoFactory;
	IVideoDAO daoV;
	IFaceDAO daoF;
	private String videoID;

	public VideoFaces(String videoID) {
		daoFactory = new DAOFactoryImpl();
		daoV = daoFactory.createSQLVideoDAO();
		daoF = daoFactory.createSQLFaceDAO();

		this.videoID = videoID;
	}

	@Override
	public void run() {
		{
			InputStream videoStream = daoV.getStreamForVideo(this.videoID);

			Video<MBFImage> video = new XuggleVideo(videoStream);
			VideoDisplay<MBFImage> vd = VideoDisplay.createOffscreenVideoDisplay(video);

			// El Thread de procesamiento de vídeo se termina al terminar el vídeo.
			vd.setEndAction(EndAction.CLOSE_AT_END);

			vd.addVideoListener(new VideoDisplayListener<MBFImage>() {
				// Número de imagen
				int imgn = 0;

				@Override
				public void beforeUpdate(MBFImage frame) {
					FaceDetector<DetectedFace, FImage> fd = new HaarCascadeDetector(40);
					List<DetectedFace> faces = fd.detectFaces(Transforms.calculateIntensity(frame));

					for (DetectedFace face : faces) {
						
						frame.drawShape(face.getBounds(), RGBColour.RED);
						try {
							// También permite enviar la imagen a un OutputStream
							// TODO guardar imagenes en BBDD
							// Tambi�n permite enviar la imagen a un OutputStream
							String path = "/tmp/" + imgn + ".jpg";
							imgn++;
							MBFImage imagen = frame.extractROI(face.getBounds());
							ByteArrayOutputStream os = new ByteArrayOutputStream();
							ImageIO.write((RenderedImage) imagen,"jpg", os); 
							InputStream fis = new ByteArrayInputStream(os.toByteArray());
	                        //ImageUtilities.write(frame.extractROI(face.getBounds()),
	                                //new File(path));
							daoF.storeFace(videoID, fis);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						System.out.println("!");
					}
				}

				@Override
				public void afterUpdate(VideoDisplay<MBFImage> display) {
				}
			});

			vd.addVideoPositionListener(new VideoPositionListener() {
				@Override
				public void videoAtStart(VideoDisplay<? extends Image<?, ?>> vd) {
				}

				@Override
				public void videoAtEnd(VideoDisplay<? extends Image<?, ?>> vd) {
					System.out.println("End of video");
				}
			});

			System.out.println("Fin.");
		}
	}
	public String getVideoID() {
		return this.videoID;
	}
}
