package es.um.sisdist.videofaces.backend.dao.video;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import es.um.sisdist.videofaces.backend.dao.models.User;
import es.um.sisdist.videofaces.backend.dao.models.Video;
import es.um.sisdist.videofaces.backend.dao.models.Video.PROCESS_STATUS;

public class SQLVideoDAO implements IVideoDAO {

    Connection conn;

    public SQLVideoDAO()
	{
        try
		{
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
			
			// Si el nombre del host se pasa por environment, se usa aquí.
			// Si no, se usa localhost. Esto permite configurarlo de forma
			// sencilla para cuando se ejecute en el contenedor, y a la vez
			// se pueden hacer pruebas locales
			Optional<String> sqlServerName = 
					Optional.ofNullable(System.getenv("SQL_SERVER"));
			conn = DriverManager.getConnection("jdbc:mysql://" +
					sqlServerName.orElse("localhost") +
					"/videofaces?user=root&password=root");
			
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }




    @Override
    public Optional<Video> getVideoById(String id) {
        PreparedStatement stm;
		try
		{
			stm = conn.prepareStatement("SELECT * from videos WHERE id = ?");
			stm.setString(1, id);
			ResultSet result = stm.executeQuery();
			if (result.next())
				return createVideo(result);
		} catch (SQLException e)
		{
			// Fallthrough
		}
		return Optional.empty();
    }

    @Override
    public InputStream getStreamForVideo(String id) {
        PreparedStatement stm;
		try
		{
			stm = conn.prepareStatement("SELECT videodata from videos WHERE id = ?");
			stm.setString(1, id);
			ResultSet result = stm.executeQuery();
			Blob objetoblob = result.getBlob(1);
            InputStream resultado =  objetoblob.getBinaryStream();
            return resultado;

            
		} catch (SQLException e)
		{
			// Fallthrough
		}
		return null;
    }

    @Override
    public PROCESS_STATUS getVideoStatus(String id) {
        PreparedStatement stm;
		try
		{
			stm = conn.prepareStatement("SELECT process_status from videos WHERE id = ?");
			stm.setString(1, id);
			ResultSet result = stm.executeQuery();
			if(result.getInt(1) == 0){
                return Video.PROCESS_STATUS.PROCESSING;
            }
            else{
                return Video.PROCESS_STATUS.PROCESSED;
            }
		} catch (SQLException e)
		{
			// Fallthrough
		}
		return Video.PROCESS_STATUS.PROCESSING;
    }

    public Optional<Video> storeVideo(String userid, PROCESS_STATUS pstatus, String date, String filename)
    {
        PreparedStatement stm;
        try
        {
			String id = User.md5pass(filename+userid);
            stm = conn.prepareStatement("INSERT INTO videos VALUES (?,?,?,?,?,?)");
            stm.setString(1, id);
            stm.setString(2, userid);
            stm.setInt(3, pstatus.ordinal());
            stm.setString(4, date);
            stm.setString(5, filename);

            File file = new File("/tmp/output");
			FileInputStream inputStream = new FileInputStream(file);
            stm.setBlob(6, inputStream);
            int row = stm.executeUpdate();
            if (row == 1)
                return this.getVideoById(id);
        } catch (SQLException | FileNotFoundException e)
        {
            // Fallthrough
        }
        return Optional.empty();
    }

    private Optional<Video> createVideo(ResultSet result)
	{


        // String id, String userid, PROCESS_STATUS pstatus, String date, String filename
		try
		{
            if(result.getInt(5) == 1){
                return Optional.of(new Video(result.getString(1), // id
					result.getString(2), // userid
                    Video.PROCESS_STATUS.PROCESSED,  // status
					result.getString(3), // filename
					result.getString(4))); // name
            }
            else{
                return Optional.of(new Video(result.getString(1), // id
					result.getString(2), // userid
                    Video.PROCESS_STATUS.PROCESSING,  // status
					result.getString(3), // filename
					result.getString(4))); // name

            }
		} catch (SQLException e)
		{
			return Optional.empty();
		}
	}

}
