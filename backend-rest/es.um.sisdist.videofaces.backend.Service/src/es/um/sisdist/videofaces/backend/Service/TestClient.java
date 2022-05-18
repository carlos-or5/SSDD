package es.um.sisdist.videofaces.backend.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

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
		Client client = ClientBuilder.newClient();
		WebTarget service = client.target(getBaseURI());

		// Test de registro
		String json = "{\"email\":\"prueba@2te2st.com\",\"name\":\"pr22ueba\",\"password\":\"test\"}";
		Response registro = service.path("register").request().post(Entity.json(json));
		int statusCode1 = registro.getStatus();
		String username = "";
		// Si el registro se ha efectuado corectamente
		if (statusCode1 == 200) {
			String outputRegistro = registro.readEntity(String.class);
			System.out.println("El usuario registrado es: " + outputRegistro);
			JSONObject jsonObject = new JSONObject(outputRegistro);
			username = (String) jsonObject.get("name");
			String r = registro.toString();
			System.out.println("La respuesta entera del servidor es: " + r);
			registro.close();
		} else {
			System.out.println("Registro invalido, nombre o email repetido.");
		}

		// Test envio del video

		File inFile = new File("video.mp4");
		FileInputStream fis = null;
		fis = new FileInputStream(inFile);
		DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());

		// Creo peticion post al endpoint correspondiente
		HttpPost httppost = new HttpPost("http://localhost:8080/rest/users/" + username + "/uploadVideo");
		MultipartEntity entity = new MultipartEntity();

		entity.addPart("file", new InputStreamBody(fis, inFile.getName()));
		// Le aniado el video en la peticion
		httppost.setEntity(entity);

		// Hago la peticion
		HttpResponse response = httpclient.execute(httppost);

		int statusCode2 = response.getStatusLine().getStatusCode();
		String idVideo = "";
		// Si se ha podido subir el video correctamente
		if (statusCode2 == 200) {
			HttpEntity responseEntity = response.getEntity();
			String outputVideo = EntityUtils.toString(responseEntity, "UTF-8");
			// Saco los campos necesarios del video
			JSONObject jsonObject = new JSONObject(outputVideo);
			String filename = (String) jsonObject.get("filename");
			idVideo = (String) jsonObject.get("id");
			System.out.println("[" + statusCode2 + "] " + "El video: " + filename + " con id: " + idVideo
					+ "se ha subido con exito.");
		}

		else {
			System.out.println("Video nulo o video con nombre repetido");
		}

		// Test checkeo de videos si he podido registrar al usuario y he podido subir el
		// video en los pasos anteriores
		if (!username.isEmpty() && !idVideo.isEmpty()) {
			Response checkStatus = service.path("users").path(username).path(idVideo).path("checkStatus").request()
					.get();
			int statusCode3 = checkStatus.getStatus();
			System.out.println("El estado inicial del video es: " + statusCode3);
			// Mientras el video no este procesado (no reciba 200 OK, seguir checkeando el video cada 2 segundos)
			while (statusCode3 != 200) {
				checkStatus = service.path("users").path(username).path(idVideo).path("checkStatus").request().get();
				statusCode3 = checkStatus.getStatus();
				System.out.println("Monitorizo el estado del video con el codigo de respuesta: " + statusCode3);
				TimeUnit.SECONDS.sleep(2);
			}
			System.out.println("El video esta procesado, ultimo codigo de respuesta fue: "+ statusCode3);
		// Una vez el video se haya procesado, test que nos devuelve las caras
		}

	}

	private static URI getBaseURI() {
		return UriBuilder.fromUri("http://localhost:8080/rest").build();
	}
}