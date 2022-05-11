package es.um.sisdist.videofaces.backend.Service;

import java.util.Optional;

import es.um.sisdist.videofaces.backend.Service.impl.AppLogicImpl;
import es.um.sisdist.videofaces.backend.dao.models.User;
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

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

// POJO, no interface no extends

@Path("/users")
public class UsersEndpoint
{
    private AppLogicImpl impl = AppLogicImpl.getInstance();
    
    @GET
    @Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public UserDTO getUserInfo(@PathParam("username") String username)
    {
    	return UserDTOUtils.toDTO(impl.getUserByEmail(username).orElse(null));    	
    }

    @POST
    @Path("/{username}/uploadVideo")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadVideo(@FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FormDataContentDisposition fileMetaData, @PathParam("username") String username) throws Exception
    {
	// El fichero que se recibe se copia en /tmp/[NOMBRE_FICHERO]
        if (fileMetaData.getFileName().isEmpty()) {
            return Response.status(Status.FORBIDDEN).build();
        }  

        // Un fichero podria contener como nombre, por ejemplo ../video, lo que daria lugar a un Arbitrary File Upload
        // https://owasp.org/www-community/vulnerabilities/Unrestricted_File_Upload
        // Comprobamos si el nombre del fichero no provoca que nos salgamos de /tmp/
        java.nio.file.Path root = java.nio.file.Paths.get("/tmp/");
        String path = "/tmp/" + fileMetaData.getFileName();
        java.nio.file.Path subpath = root.normalize().resolve(path).normalize();

        if(!subpath.startsWith(root) || Files.isSymbolicLink(subpath)) {
            return Response.status(Status.FORBIDDEN).build();
        }

        File targetFile = new File(path);

        java.nio.file.Files.copy(fileInputStream, targetFile.toPath(),
			StandardCopyOption.REPLACE_EXISTING);

        fileInputStream.close();
        impl.storeVideo(username, fileMetaData.getFileName());
        return Response.ok(fileMetaData.getFileName()).build();
    }

}
