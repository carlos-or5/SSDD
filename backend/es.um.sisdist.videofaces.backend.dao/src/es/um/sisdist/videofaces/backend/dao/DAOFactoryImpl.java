/**
 * 
 */
package es.um.sisdist.videofaces.backend.dao;

import es.um.sisdist.videofaces.backend.dao.user.IUserDAO;
import es.um.sisdist.videofaces.backend.dao.user.SQLUserDAO;
import es.um.sisdist.videofaces.backend.dao.video.IVideoDAO;
import es.um.sisdist.videofaces.backend.dao.video.SQLVideoDAO;

/**
 * @author dsevilla
 *
 */
public class DAOFactoryImpl implements IDAOFactory
{
	@Override
	public IUserDAO createSQLUserDAO()
	{
		return new SQLUserDAO();
	}

	@Override
	public IVideoDAO createSQLVideoDAO()
	{
		return new SQLVideoDAO();
	}

}
