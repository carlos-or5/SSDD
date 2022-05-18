package es.um.sisdist.videofaces.backend.Service;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.logging.Logger;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.um.sisdist.videofaces.backend.Service.impl.AppLogicImpl;
import es.um.sisdist.videofaces.backend.dao.models.User;
import es.um.sisdist.videofaces.backend.dao.models.Video;
import es.um.sisdist.videofaces.backend.dao.models.Video.PROCESS_STATUS;
import es.um.sisdist.videofaces.models.VideoDTO;
import es.um.sisdist.videofaces.models.VideoDTOUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

// POJO, no interface no extends

@Path("/videos")
public class VideoEndpoint {
	private AppLogicImpl impl = AppLogicImpl.getInstance();
	private static final Logger logger = Logger.getLogger(VideoEndpoint.class.getName());

	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadVideo(@FormDataParam("file") InputStream fileInputStream,
			@FormDataParam("file") FormDataContentDisposition fileMetaData, @Context HttpHeaders httpheaders,
			@Context UriInfo uriInfo) throws Exception {

		if (!authenticate(httpheaders.getHeaderString("User"), uriInfo.getAbsolutePath().toString(),
				httpheaders.getHeaderString("Date"), httpheaders.getHeaderString("Auth-Token"))) {
			return Response.status(Status.FORBIDDEN).build();
		}

		// El fichero que se recibe se copia en /tmp/[NOMBRE_FICHERO]
		if (fileMetaData.getFileName().isEmpty()) {
			return Response.status(Status.FORBIDDEN).build();
		}

		// Un fichero podria contener como nombre, por ejemplo ../video, lo que daria
		// lugar a un Arbitrary File Upload
		// https://owasp.org/www-community/vulnerabilities/Unrestricted_File_Upload
		// Comprobamos si el nombre del fichero no provoca que nos salgamos de /tmp/
		java.nio.file.Path root = Paths.get("/tmp/");
		String path = "/tmp/" + fileMetaData.getFileName();
		java.nio.file.Path subpath = root.normalize().resolve(path).normalize();

		if (!subpath.startsWith(root) || Files.isSymbolicLink(subpath)) {
			return Response.status(Status.FORBIDDEN).build();
		}

		File targetFile = new File(path);

		Files.copy(fileInputStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		fileInputStream.close();
		Optional<Video> v = impl.storeVideo(httpheaders.getHeaderString("User"), fileMetaData.getFileName());
		
		if (!v.isPresent()) {
			return Response.status(Status.CONFLICT).build();
		}
		
		Video video = v.get();

		logger.info("El id del video subido es: " + video.getId());

		return Response.accepted(new Object() {
			private String location = video.getId();

			@SuppressWarnings("unused")
			public String getLocation() {
				return location;
			}
		}).build();
	}

	@GET
	@Path("/{videoid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response showFacesOfVideo(@PathParam("videoid") String videoid, @Context HttpHeaders httpheaders,
			@Context UriInfo uriInfo) throws JsonProcessingException {

		if (!authenticate(httpheaders.getHeaderString("User"), uriInfo.getAbsolutePath().toString(),
				httpheaders.getHeaderString("Date"), httpheaders.getHeaderString("Auth-Token"))) {
			return Response.status(Status.FORBIDDEN).build();
		}
		Optional<Video> video = impl.getVideoById(videoid);

		Video v;
		if (video.isPresent()) {
			v = video.get();
		} else {
			logger.info("No se ha obtenido el video ");
			return Response.status(Status.FORBIDDEN).build();
		}

		if (v.getPstatus() == PROCESS_STATUS.PROCESSING) {
			return Response.noContent().build();
		} else {
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

			return Response.ok(new Object() {
				private VideoDTO video = VideoDTOUtils.toDTO(v);
				private String faces = stringMapa;

				@SuppressWarnings("unused")
				public VideoDTO getVideo() {
					return video;
				}

				@SuppressWarnings("unused")
				public String getFaces() {
					return faces;
				}
			}).build();
		}
	}

	private boolean authenticate(String username, String url, String date, String authToken) {
		if (username == null || url == null || date == null || authToken == null) {
			return false;
		}

		if (username.isEmpty() || url.isEmpty() || date.isEmpty() || authToken.isEmpty() || username.isBlank()
				|| url.isBlank() || date.isBlank() || authToken.isBlank()) {
			return false;
		}
		
		username = username.replace("\n", "");
		url = url.replace("\n", "");
		date = date.replace("\n", "");
		
		logger.info("Username: " + username);
		logger.info("URL: " + url);
		logger.info("Date: " + date);
		logger.info("AuthToken: " + authToken);
		
		

		// Comprobamos que la fecha sea ISO8601
		try {
			Instant.from(DateTimeFormatter.ISO_INSTANT.parse(date));
		} catch (DateTimeParseException e) {
			logger.info("Fecha invalida");
			return false;
		}

		Optional<User> user = impl.getUserByName(username);

		String userToken;
		if (user.isPresent()) {
			userToken = user.get().getToken();
		} else {
			logger.info("User no encontrado");
			return false;
		}

		String checkAuthToken = User.md5pass(url + date + userToken);
		
		logger.info("CheckAuthToken: " + checkAuthToken);

		if (checkAuthToken.equals(authToken)) {
			return true;
		} else {
			return false;
		}
	}
}
