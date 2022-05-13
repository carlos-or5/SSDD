package es.um.sisdist.videofaces.backend.Service;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import es.um.sisdist.videofaces.backend.Service.impl.AppLogicImpl;
import jakarta.ws.rs.GET;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

// POJO, no interface no extends

@Path("/videos")
public class VideosEndpoint
{
    private AppLogicImpl impl = AppLogicImpl.getInstance();
	@GET
    @Path("/{videoid}/showFaces")
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerUser(@PathParam("videoid") String videoid) throws JsonProcessingException
    {
        // Llamar a metodo SQL para devolver los videos (ID Video + Filename en JSON)
        Map<String, byte[]> mapaFaces = impl.getFacesOfVideo(videoid);
        
        // Convertimos el byte array en base64 para devolverlo en el json
        
        Map<String, String> result = new HashMap<String, String>();
        
        for (Entry<String, byte[]> element : mapaFaces.entrySet()) {
			result.put(element.getKey(), new String(Base64.getEncoder().encode(element.getValue())));
		}
        
        ObjectMapper objectMapper = new ObjectMapper();
        String stringMapa = objectMapper.writeValueAsString(result);
        if (result.isEmpty()){
        	Response.status(Status.FORBIDDEN).build();
        }
        // Devolver el hahsmap
        return Response.ok(stringMapa).build();
        
    }
    

}
