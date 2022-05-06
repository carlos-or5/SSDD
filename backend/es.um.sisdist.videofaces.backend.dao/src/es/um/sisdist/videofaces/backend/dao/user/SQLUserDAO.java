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
		PreparedStatement stm;
		try
		{
			stm = conn.prepareStatement("SELECT * from users WHERE id = ?");
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

	public Optional<User> register(String email, String name, String pass)
    {
        PreparedStatement stm;
        try
        {
            stm = conn.prepareStatement("INSERT INTO users VALUES (?,?,?,?,?,?)");
            stm.setString(1, User.md5pass(email));
            stm.setString(2, email);
            stm.setString(3, User.md5pass(pass));
            stm.setString(4, name);
            stm.setString(5, "TOKEN");
            stm.setInt(6, 0);
            int row = stm.executeUpdate();
            if (row == 1)
                return this.getUserByEmail(email);
        } catch (SQLException e)
        {
            // Fallthrough
        }
        return Optional.empty();
    }

	public void incrementsVisits(String email){
		PreparedStatement stm;
        try{
			stm = conn.prepareStatement("UPDATE users SET visits = visits+1 WHERE email = ?");
			stm.setString(1, email);
			stm.executeUpdate();
		} catch (SQLException e)
		{
			// Fallthrough
		}
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
