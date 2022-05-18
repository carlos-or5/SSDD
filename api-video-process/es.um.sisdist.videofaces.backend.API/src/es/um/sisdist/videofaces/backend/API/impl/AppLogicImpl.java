/**
 *
 */
package es.um.sisdist.videofaces.backend.API.impl;

import java.security.SecureRandom;
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
import es.um.sisdist.videofaces.backend.grpc.VideoAvailability;
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

	private static final int RANDOM_TOKEN_LEN = 12;

	private final ManagedChannel channel;
	private final GrpcServiceGrpc.GrpcServiceBlockingStub blockingStub;
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
		blockingStub = GrpcServiceGrpc.newBlockingStub(channel);
		asyncStub = GrpcServiceGrpc.newStub(channel);
	}

	public static AppLogicImpl getInstance() {
		return instance;
	}

	public Optional<User> getUserByEmail(String userEmail) {
		Optional<User> u = dao.getUserByEmail(userEmail);
		return u;
	}
	
	public Optional<User> getUserByName(String userName) {
		Optional<User> u = dao.getUserByName(userName);
		return u;
	}

	public Optional<User> getUserById(String userId) {
		return dao.getUserById(userId);
	}

	public boolean isVideoReady(String videoId) {
		// Test de grpc, puede hacerse con la BD
		VideoAvailability available = blockingStub.isVideoReady(VideoSpec.newBuilder().setId(videoId).build());
		return available.getAvailable();
	}

	// El frontend, a través del formulario de login,
	// envía el usuario y pass, que se convierte a un DTO. De ahí
	// obtenemos la consulta a la base de datos, que nos retornará,
	// si procede,
	public Optional<User> checkLogin(String email, String pass) {
		Optional<User> u = dao.getUserByEmail(email);

		if (u.isPresent()) {
			String hashed_pass = User.md5pass(pass);
			if (0 == hashed_pass.compareTo(u.get().getPassword_hash())) {
				dao.incrementsVisits(email);
				u = dao.getUserByEmail(email);
				return u;
			}
		}

		return Optional.empty();
	}

	public Optional<User> register(String email, String name, String pass) {

		if (email.isEmpty() || name.isEmpty() || pass.isEmpty()) {
			return Optional.empty();
		}

		Optional<User> u = dao.getUserByEmail(email);
		Optional<User> u2 = dao.getUserByName(name);

		// Si el email a registrar esta creado, o el nombre, entonces devolver error
		if (u.isPresent() || u2.isPresent()) {
			// String hashed_pass = User.md5pass(pass);
			// if (0 == hashed_pass.compareTo(u.get().getPassword_hash()))
			// return u;
			return Optional.empty();
		}

		// Si no hay email registrado en la base de datos, entonces registramos
		String token = getRandomString(RANDOM_TOKEN_LEN);

		u = dao.register(email, name, pass, token);

		return u;
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

	public Map<String, String> getVideos(String username) {
		Optional<User> u = dao.getUserByName(username);
		HashMap<String, String> mapVideos = new HashMap<String, String>();
		// Si existe el usuario
		if (u.isPresent()) {
			User usuario = u.get();
			String id = usuario.getId();
			List<Optional<Video>> listaVideos = daoV.getVideosByUserId(id);
			for (Optional<Video> video : listaVideos) {
				if (video.isPresent()) {
					Video v = video.get();
					String videoId = v.getId();
					String filename = v.getFilename();
					mapVideos.put(videoId, filename);
				}
			}
		}
		return Collections.unmodifiableMap(mapVideos);

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

	public boolean deleteVideo(String videoid) {
		boolean resul = daoV.deleteVideo(videoid);
		return resul;
	}

	public boolean deleteFace(String faceid) {
		boolean resul = daoF.deleteFace(faceid);
		return resul;
	}

	private static String getRandomString(int len) {
		SecureRandom secureRandom = new SecureRandom();

		char CHARACTER_SET[] = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

		StringBuffer buff = new StringBuffer(len);
		for (int i = 0; i < len; i++) {
			int offset = secureRandom.nextInt(CHARACTER_SET.length);
			buff.append(CHARACTER_SET[offset]);
		}
		return buff.toString();
	}

}
