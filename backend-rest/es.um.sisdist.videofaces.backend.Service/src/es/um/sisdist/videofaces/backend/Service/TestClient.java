package es.um.sisdist.videofaces.backend.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.util.EntityUtils;

import org.json.*;


@SuppressWarnings("deprecation")
public class TestClient {
	public static void main(String[] args) throws IOException, InterruptedException {
		// ClientConfig config = new ClientConfig();
		// config.property(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
		// ClientBuilder.newBuilder().register(JacksonFeature.class);
		Client client = ClientBuilder.newClient();
		WebTarget service = client.target(getBaseURI());

		// Test de registro
		String json = "{\"email\":\"a@ss2.com\",\"name\":\"ssna\",\"password\":\"test\"}";
		Response registro = service.path("register").request().post(Entity.json(json));
		
		String outputRegistro = registro.readEntity(String.class);
		System.out.println("El usuario registrado es: " + outputRegistro);

		JSONObject jsonObject = new JSONObject(outputRegistro);
		String username = (String) jsonObject.get("name");
		String r = registro.toString();
		System.out.println("La respuesta entera del servidor es: " + r);
		registro.close();

		// Test envio del video

		File inFile = new File("video.mp4");
        FileInputStream fis = null;
        fis = new FileInputStream(inFile);
        DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
		
        // server back-end URL
        HttpPost httppost = new HttpPost("http://localhost:8080/rest/users/"+username+"/uploadVideo");
        MultipartEntity entity = new MultipartEntity();
        
        entity.addPart("file", new InputStreamBody(fis, inFile.getName()));
        httppost.setEntity(entity);
        
        HttpResponse response = httpclient.execute(httppost);
        
        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity responseEntity = response.getEntity();
        String responseString = EntityUtils.toString(responseEntity, "UTF-8");
        
        System.out.println("[" + statusCode + "] " + responseString);
        
		//Response uploadVideo = service.path("users").path(username).path("uploadVideo").request(MediaType.APPLICATION_JSON_TYPE)
				//.post(Entity.json(jsonVideo));
		
		
		
		//String outputVideo = uploadVideo.readEntity(String.class);
		
		//System.out.println(outputVideo);
		// if (usuarioRegistrado.getName().equals(usuario.getName())) {
		// System.out.println("Perfesto!");
		// }

		// Fluent interfaces
		/*
		 * System.out.println(
		 * service.path("jaxrs").path("hello").request(MediaType.TEXT_PLAIN).get(String.
		 * class).toString()); // Get plain text
		 * System.out.println(service.path("jaxrs").path("hello").request(MediaType.
		 * TEXT_PLAIN).get(String.class)); // Get XML
		 * System.out.println(service.path("jaxrs").path("hello").request(MediaType.
		 * TEXT_XML).get(String.class)); // The HTML
		 * System.out.println(service.path("jaxrs").path("hello").request(MediaType.
		 * TEXT_HTML).get(String.class));
		 */

	}

	private static URI getBaseURI() {
		return UriBuilder.fromUri("http://localhost:8080/rest").build();
	}
}