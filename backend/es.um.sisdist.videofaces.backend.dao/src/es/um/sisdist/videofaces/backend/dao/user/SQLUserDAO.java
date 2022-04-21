/**
 * 
 */
package es.um.sisdist.videofaces.backend.dao.user;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import es.um.sisdist.videofaces.backend.dao.models.User;

/**
 * @author dsevilla
 *
 */
@SuppressWarnings("deprecation")
public class SQLUserDAO implements IUserDAO
{
	Connection conn;
	
	public SQLUserDAO()
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
	public Optional<User> getUserById(String id)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<User> getUserByEmail(String id)
	{
		PreparedStatement stm;
		try
		{
			stm = conn.prepareStatement("SELECT * from users WHERE email = ?");
			stm.setString(1, id);
			ResultSet result = stm.executeQuery();
			if (result.next())
				return createUser(result);
		} catch (SQLException e)
		{
			// Fallthrough
		}
		return Optional.empty();
	}

	private Optional<User> createUser(ResultSet result)
	{
		try
		{
			return Optional.of(new User(result.getString(1), // id
					result.getString(2), // email
					result.getString(3), // pwhash
					result.getString(4), // name
					result.getString(5), // token
					result.getInt(6)));  // visits
		} catch (SQLException e)
		{
			return Optional.empty();
		}
	}

}
