/**
 *
 */
package es.um.sisdist.videofaces.backend.Service.impl;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.protobuf.Empty;

import es.um.sisdist.videofaces.backend.dao.DAOFactoryImpl;
import es.um.sisdist.videofaces.backend.dao.IDAOFactory;
import es.um.sisdist.videofaces.backend.dao.face.IFaceDAO;
import es.um.sisdist.videofaces.backend.dao.models.Face;
import es.um.sisdist.videofaces.backend.dao.models.User;
import es.um.sisdist.videofaces.backend.dao.models.Video;
import es.um.sisdist.videofaces.backend.dao.user.IUserDAO;
import es.um.sisdist.videofaces.backend.dao.video.IVideoDAO;
import es.um.sisdist.videofaces.backend.grpc.GrpcServiceGrpc;
import es.um.sisdist.videofaces.backend.grpc.VideoSpec;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

/**
 * @author dsevilla
 *
 */
public class AppLogicImpl {
	IDAOFactory daoFactory;
	IUserDAO dao;
	IVideoDAO daoV;
	IFaceDAO daoF;

	private static final Logger logger = Logger.getLogger(AppLogicImpl.class.getName());

	private final ManagedChannel channel;
	private final GrpcServiceGrpc.GrpcServiceStub asyncStub;

	static AppLogicImpl instance = new AppLogicImpl();

	private AppLogicImpl() {
		daoFactory = new DAOFactoryImpl();
		dao = daoFactory.createSQLUserDAO();
		daoV = daoFactory.createSQLVideoDAO();
		daoF = daoFactory.createSQLFaceDAO();

		Optional<String> grpcServerName = Optional.ofNullable(System.getenv("GRPC_SERVER"));
		Optional<String> grpcServerPort = Optional.ofNullable(System.getenv("GRPC_SERVER_PORT"));

		channel = ManagedChannelBuilder
				.forAddress(grpcServerName.orElse("localhost"), Integer.parseInt(grpcServerPort.orElse("50051")))
				// Channels are secure by default (via SSL/TLS). For the example we disable TLS
				// to avoid
				// needing certificates.
				.usePlaintext().build();
		asyncStub = GrpcServiceGrpc.newStub(channel);
	}

	public static AppLogicImpl getInstance() {
		return instance;
	}

	public Optional<User> getUserById(String userId) {
		return dao.getUserById(userId);
	}

	public Optional<Video> storeVideo(String username, String filename) {

		logger.info("Me ha llegado una peticion para almacenar un video");
		Optional<User> u = dao.getUserByName(username);
		if (u.isPresent()) {
			logger.info("Usuario presente, voy a subirlo");
			User usuario = u.get();
			String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());

			Optional<Video> v = daoV.storeVideo(usuario.getId(), Video.PROCESS_STATUS.PROCESSING, date, filename);
			logger.info("Store Video en MySQL ejecutado, voy a comprobar si esta creado en base de datos: ");
			if (v.isPresent()) {
				logger.info("Video creado en base de datos, voy llamar a GRPC para procesarlo");
				Video video = v.get();
				final CountDownLatch finisLatch = new CountDownLatch(1);
				StreamObserver<Empty> soEmpty = new StreamObserver<Empty>() {
					@Override
					public void onCompleted() {
						finisLatch.countDown();
					}

					@Override
					public void onError(Throwable t) {
						finisLatch.countDown();
					}

					@Override
					public void onNext(Empty value) {
					}
				};

				StreamObserver<VideoSpec> soV = this.asyncStub.processVideo(soEmpty);

				soV.onNext(VideoSpec.newBuilder().setId(video.getId()).build());
				soV.onCompleted();

				try {
					if (finisLatch.await(1, TimeUnit.SECONDS))
						logger.info(" Received response . ");
					else
						logger.info(" Not received response ! ");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				logger.info("");
				return v;
			}
			logger.info("Video no creado en base de datos");
			return Optional.empty();
		}
		return Optional.empty();

	}

	public Optional<Video> getVideoById(String videoID) {
		Optional<Video> v = daoV.getVideoById(videoID);
		// Si existe el usuario
		if (v.isPresent()) {
			v.get();
		}
		return Optional.empty();
	}

	public Map<String, byte[]> getFacesOfVideo(String videoid) {
		List<Optional<Face>> faces = daoF.getFaceByVideoId(videoid);
		HashMap<String, byte[]> resul = new HashMap<String, byte[]>();

		for (Optional<Face> face : faces) {
			if (face.isPresent()) {
				resul.put(face.get().getId(), face.get().getImagedata());
			}
		}

		return Collections.unmodifiableMap(resul);
	}
	
}
