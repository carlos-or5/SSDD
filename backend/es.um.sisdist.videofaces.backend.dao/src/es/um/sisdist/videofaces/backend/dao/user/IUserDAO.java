package es.um.sisdist.videofaces.backend.dao.user;

import java.util.Optional;

import es.um.sisdist.videofaces.backend.dao.models.User;

public interface IUserDAO {
	public Optional<User> getUserById(String id);

	public Optional<User> getUserByEmail(String id);

	public Optional<User> getUserByName(String username);

	public Optional<User> register(String email, String name, String pass, String token);

	public void incrementsVisits(String email);
}
