/**
 *
 */
package es.um.sisdist.videofaces.backend.dao;

import es.um.sisdist.videofaces.backend.dao.user.IUserDAO;
import es.um.sisdist.videofaces.backend.dao.video.IVideoDAO;

/**
 * @author dsevilla
 *
 */
public interface IDAOFactory
{
    public IUserDAO createSQLUserDAO();
    public IVideoDAO createSQLVideoDAO();
}
