package es.um.sisdist.videofaces.backend.dao.face;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import es.um.sisdist.videofaces.backend.dao.models.Face;
import es.um.sisdist.videofaces.backend.dao.models.User;

public class SQLFaceDAO implements IFaceDAO {

	Connection conn;

	@SuppressWarnings("deprecation")
	public SQLFaceDAO() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();

			// Si el nombre del host se pasa por environment, se usa aquí.
			// Si no, se usa localhost. Esto permite configurarlo de forma
			// sencilla para cuando se ejecute en el contenedor, y a la vez
			// se pueden hacer pruebas locales
			Optional<String> sqlServerName = Optional.ofNullable(System.getenv("SQL_SERVER"));
			conn = DriverManager.getConnection(
					"jdbc:mysql://" + sqlServerName.orElse("localhost") + "/videofaces?user=root&password=root");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public Optional<Face> getFaceById(String id) {
		PreparedStatement stm;
		try {
			stm = conn.prepareStatement("SELECT * from faces WHERE id = ?");
			stm.setString(1, id);
			ResultSet result = stm.executeQuery();
			if (result.next())
				return createFace(result);
		} catch (SQLException e) {
			// Fallthrough
		}
		return Optional.empty();
	}

	@Override
	public InputStream getStreamForFace(String id) {
		PreparedStatement stm;
		try {
			stm = conn.prepareStatement("SELECT imagedata from faces WHERE id = ?");
			stm.setString(1, id);
			ResultSet result = stm.executeQuery();
			if (result.next()) {
				Blob objetoblob = result.getBlob(1);
				InputStream resultado = objetoblob.getBinaryStream();
				return resultado;
			}

		} catch (SQLException e) {
			// Fallthrough
		}
		return null;
	}

	public Optional<Face> storeFace(String videoid, InputStream fis) {
		PreparedStatement stm;
		try {
			//File file = new File(filename);
			//FileInputStream inputStream = new FileInputStream(file);

			String id = User.md5pass(videoid + (new String(fis.readNBytes(50))));
			stm = conn.prepareStatement("INSERT INTO faces VALUES (?,?,?)");
			stm.setString(1, id);
			stm.setString(2, videoid);
			stm.setBlob(3, fis);
			int row = stm.executeUpdate();
			if (row == 1)
				return this.getFaceById(id);
		} catch (SQLException | FileNotFoundException e) {
			// Fallthrough
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Optional.empty();
	}

	private Optional<Face> createFace(ResultSet result) {

		// String id, String userid, PROCESS_STATUS pstatus, String date, String
		// filename
		try {
			return Optional.of(new Face(result.getString(1), // id
					result.getString(2))); // email
		} catch (SQLException e) {
			return Optional.empty();
		}
	}

}
