package es.um.sisdist.videofaces.backend.Service;

import java.io.IOException;
import java.net.URI;

import org.glassfish.jersey.client.ClientConfig;

import com.fasterxml.jackson.core.util.JacksonFeature;

import es.um.sisdist.videofaces.models.UserDTO;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

public class TestClient {
	public static void main(String[] args) throws IOException, InterruptedException {
		// ClientConfig config = new ClientConfig();
		// config.property(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
		// ClientBuilder.newBuilder().register(JacksonFeature.class);
		Client client = ClientBuilder.newClient();
		WebTarget service = client.target(getBaseURI());
		
		// Test de registro
		String json = "{\"email\":\"prueba4@test.com\",\"name\":\"nombr22ePrueba\",\"password\":\"test\"}";
		Response response = service.path("register").request().post(Entity.json(json));

		// Response response =
		// service.request(MediaType.APPLICATION_JSON).post(Entity.entity(json,
		// MediaType.valueOf("application/json")));
		String output = response.readEntity(String.class);
		System.out.println("El usuario registrado es:`" + output);
		String r = response.toString();
		System.out.println("La respuesta entera del servidor es :" + r);
		// UserDTO usuarioRegistrado = response.readEntity(UserDTO.class);
		response.close();
		
		
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