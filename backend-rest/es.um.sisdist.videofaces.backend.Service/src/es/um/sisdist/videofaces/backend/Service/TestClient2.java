package es.um.sisdist.videofaces.backend.Service;

import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;

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

import com.google.gson.Gson;

@SuppressWarnings("deprecation")
public class TestClient2 {
	public static void main(String[] args) throws IOException, InterruptedException {
		Client client = ClientBuilder.newClient();
		WebTarget service = client.target(getBaseURI());

		String username = "prueba";

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
			// Mientras el video no este procesado (no reciba 200 OK, seguir checkeando el
			// video cada 2 segundos)
			while (statusCode3 != 200) {
				checkStatus = service.path("users").path(username).path(idVideo).path("checkStatus").request().get();
				statusCode3 = checkStatus.getStatus();
				String procesado = "";
				if (statusCode3 == 204) {
					procesado = ", se esta procesando.";
				} else {
					procesado = ", se acaba de procesar.";
				}
				System.out.println(
						"Monitorizo el estado del video con el codigo de respuesta: " + statusCode3 + procesado);
				TimeUnit.SECONDS.sleep(2);
			}
			System.out.println("El video esta procesado, ultimo codigo de respuesta fue: " + statusCode3);
			// Una vez el video se haya procesado, test que nos devuelve las caras
			Response consultaCaras = service.path("users").path(username).path(idVideo).path("showFaces").request()
					.get();
			int statusCode4 = consultaCaras.getStatus();
			if (statusCode4 == 200) {
				String outputCaras = consultaCaras.readEntity(String.class);
				HashMap<String, Object> map = new Gson().fromJson(outputCaras, HashMap.class);
				Collection<Object> values = map.values();
				ArrayList<Object> listOfValues = new ArrayList<>(values);
				System.out.println("Las caras detectadas en el video son: ");
				map.forEach((key, value) -> System.out.println(key + ":" + value));
				displayFaces(listOfValues);
				// System.out.println("La respuesta a la consulta de las caras es: " +
				// respuesta);
				System.out.println(outputCaras);
			}
			// Borro el video para poder volver a ejecutar el test sin tener que reiniciar
			// las maquinas
			Response borrarVideo = service.path("users").path(username).path(idVideo).path("deleteVideo").request()
					.get();
			String respuestaBorrar = borrarVideo.toString();
			System.out.println("La respuesta a borrar es: " + respuestaBorrar);
		}
	}

	private static URI getBaseURI() {
		return UriBuilder.fromUri("http://localhost:8080/rest").build();
	}

	private static void displayFaces(ArrayList<Object> array) throws IOException {

		JFrame f = new JFrame();
		JPanel p = new JPanel();
		p.setLayout(new FlowLayout());
		for (Object object : array) {
			byte[] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary((String) object);
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
			ImageIcon icon = new ImageIcon(img);
			JLabel lbl = new JLabel();
			lbl.setIcon(icon);
			p.add(lbl);
		}

		JScrollPane scrollPane = new JScrollPane(p);
		f.add(scrollPane);
		f.pack();
		f.setLocationRelativeTo(null);
		f.setVisible(true);
		f.setSize(1000, 400);
	}

}