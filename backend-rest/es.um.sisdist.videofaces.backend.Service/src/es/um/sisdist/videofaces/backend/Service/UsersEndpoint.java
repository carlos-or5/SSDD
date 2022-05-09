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

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.File;
import java.io.InputStream;
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
            @FormDataParam("file") FormDataContentDisposition fileMetaData) throws Exception
    {
	// El fichero que se recibe se copia en /tmp/output
        File targetFile = new File("/tmp/output");

        java.nio.file.Files.copy(fileInputStream, targetFile.toPath(),
			StandardCopyOption.REPLACE_EXISTING);

        fileInputStream.close();
        return Response.ok(fileMetaData.getFileName()).build();
    }

}
