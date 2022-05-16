package es.um.sisdist.videofaces.backend.Service;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.logging.Logger;

import es.um.sisdist.videofaces.backend.Service.impl.AppLogicImpl;
import es.um.sisdist.videofaces.backend.dao.models.Video;
import es.um.sisdist.videofaces.models.UserDTO;
import es.um.sisdist.videofaces.models.UserDTOUtils;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

// POJO, no interface no extends

@Path("/users")
public class UsersEndpoint {
	private AppLogicImpl impl = AppLogicImpl.getInstance();
	private static final Logger logger = Logger.getLogger(UsersEndpoint.class.getName());

	@GET
	@Path("/{username}")
	@Produces(MediaType.APPLICATION_JSON)
	public UserDTO getUserInfo(@PathParam("username") String username) {
		return UserDTOUtils.toDTO(impl.getUserByEmail(username).orElse(null));
	}

	@POST
	@Path("/{username}/uploadVideo")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadVideo(@FormDataParam("file") InputStream fileInputStream,
			@FormDataParam("file") FormDataContentDisposition fileMetaData, @PathParam("username") String username)
			throws Exception {
		// El fichero que se recibe se copia en /tmp/[NOMBRE_FICHERO]
		if (fileMetaData.getFileName().isEmpty()) {
			return Response.status(Status.FORBIDDEN).build();
		}

		// Un fichero podria contener como nombre, por ejemplo ../video, lo que daria
		// lugar a un Arbitrary File Upload
		// https://owasp.org/www-community/vulnerabilities/Unrestricted_File_Upload
		// Comprobamos si el nombre del fichero no provoca que nos salgamos de /tmp/
		java.nio.file.Path root = java.nio.file.Paths.get("/tmp/");
		String path = "/tmp/" + fileMetaData.getFileName();
		java.nio.file.Path subpath = root.normalize().resolve(path).normalize();

		if (!subpath.startsWith(root) || Files.isSymbolicLink(subpath)) {
			return Response.status(Status.FORBIDDEN).build();
		}

		File targetFile = new File(path);

		java.nio.file.Files.copy(fileInputStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		fileInputStream.close();
		Optional<Video> v = impl.storeVideo(username, fileMetaData.getFileName());
		Video video = v.get();

		logger.info("El id del video subido es: " + video.getId());

		return Response.ok(fileMetaData.getFileName()).build();
	}

	@GET
	@Path("/{username}/showVideos")
	@Produces(MediaType.APPLICATION_JSON)
	public Response registerUser(@PathParam("username") String username) throws JsonProcessingException {
		// Llamar a metodo SQL para devolver los videos (ID Video + Filename en JSON)
		Map<String, String> mapaVideos = impl.getVideos(username);
		ObjectMapper objectMapper = new ObjectMapper();
		String stringMapa = objectMapper.writeValueAsString(mapaVideos);
		if (mapaVideos.isEmpty()) {
			Response.status(Status.FORBIDDEN).build();
		}
		// Devolver el hahsmap
		return Response.ok(stringMapa).build();

	}

	@GET
	@Path("/{username}/{videoid}/showFaces")
	@Produces(MediaType.APPLICATION_JSON)
	public Response showFacesOfVideo(@PathParam("videoid") String videoid) throws JsonProcessingException {
		// Llamar a metodo SQL para devolver los videos (ID Video + Filename en JSON)
		Map<String, byte[]> mapaFaces = impl.getFacesOfVideo(videoid);

		// Convertimos el byte array en base64 para devolverlo en el json

		Map<String, String> result = new HashMap<String, String>();

		for (Entry<String, byte[]> element : mapaFaces.entrySet()) {
			result.put(element.getKey(), new String(Base64.getEncoder().encode(element.getValue())));
		}

		ObjectMapper objectMapper = new ObjectMapper();
		String stringMapa = objectMapper.writeValueAsString(result);
		if (result.isEmpty()) {
			Response.status(Status.FORBIDDEN).build();
		}
		// Devolver el hahsmap
		return Response.ok(stringMapa).build();

	}
	
	@GET
    @Path("/{username}/{videoid}/deleteVideo")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteVideo(@PathParam("videoid") String videoid)
    {
        boolean resultado = impl.deleteVideo(videoid);
        if (resultado)
            return Response.ok().build();
        else
            return Response.status(Status.FORBIDDEN).build();
    }

}
