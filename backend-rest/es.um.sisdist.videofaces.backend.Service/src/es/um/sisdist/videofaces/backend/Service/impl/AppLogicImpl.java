/**
 *
 */
package es.um.sisdist.videofaces.backend.Service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.logging.Logger;

import es.um.sisdist.videofaces.backend.dao.DAOFactoryImpl;
import es.um.sisdist.videofaces.backend.dao.IDAOFactory;
import es.um.sisdist.videofaces.backend.dao.models.User;
import es.um.sisdist.videofaces.backend.dao.models.Video;
import es.um.sisdist.videofaces.backend.dao.user.IUserDAO;
import es.um.sisdist.videofaces.backend.dao.video.IVideoDAO;
import es.um.sisdist.videofaces.backend.grpc.GrpcServiceGrpc;
import es.um.sisdist.videofaces.backend.grpc.VideoAvailability;
import es.um.sisdist.videofaces.backend.grpc.VideoSpec;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * @author dsevilla
 *
 */
public class AppLogicImpl
{
    IDAOFactory daoFactory;
    IUserDAO dao;
    IVideoDAO daoV;

    private static final Logger logger = Logger.getLogger(AppLogicImpl.class.getName());

    private final ManagedChannel channel;
    private final GrpcServiceGrpc.GrpcServiceBlockingStub blockingStub;
    private final GrpcServiceGrpc.GrpcServiceStub asyncStub;

    static AppLogicImpl instance = new AppLogicImpl();

    private AppLogicImpl()
    {
        daoFactory = new DAOFactoryImpl();
        dao = daoFactory.createSQLUserDAO();
        daoV = daoFactory.createSQLVideoDAO();

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

    public static AppLogicImpl getInstance()
    {
        return instance;
    }

    public Optional<User> getUserByEmail(String userId)
    {
        Optional<User> u = dao.getUserByEmail(userId);
        return u;
    }

    public Optional<User> getUserById(String userId)
    {
        return dao.getUserById(userId);
    }

    public boolean isVideoReady(String videoId)
    {
        // Test de grpc, puede hacerse con la BD
        VideoAvailability available = blockingStub.isVideoReady(VideoSpec.newBuilder().setId(videoId).build());
        return available.getAvailable();
    }

    // El frontend, a través del formulario de login,
    // envía el usuario y pass, que se convierte a un DTO. De ahí
    // obtenemos la consulta a la base de datos, que nos retornará,
    // si procede,
    public Optional<User> checkLogin(String email, String pass)
    {
        Optional<User> u = dao.getUserByEmail(email);

        if (u.isPresent())
        {
            String hashed_pass = User.md5pass(pass);
            if (0 == hashed_pass.compareTo(u.get().getPassword_hash())){
                dao.incrementsVisits(email);
                return u;
            }
        }

        return Optional.empty();
    }

    public Optional<User> register(String email, String name, String pass)
    {

        if (email.isEmpty() || name.isEmpty() || pass.isEmpty()) {
            return Optional.empty();
        }

        Optional<User> u = dao.getUserByEmail(email);


        // Si el email a registrar esta creado, entonces devolver error
        if (u.isPresent())
        {
            //String hashed_pass = User.md5pass(pass);
            //if (0 == hashed_pass.compareTo(u.get().getPassword_hash()))
            //return u;
            return Optional.empty();
        }

        // Si no hay email registrado en la base de datos, entonces registramos

        u = dao.register(email, name, pass);

        return u;
    }
    public void storeVideo(String username, String filename){

        Optional<User> u = dao.getUserByName(username);
        if (u.isPresent()){
            User usuario = u.get();
            String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());

            daoV.storeVideo(usuario.getId(), Video.PROCESS_STATUS.PROCESSING, date, filename);
        }
    }
}
