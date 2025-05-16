package otp.repository;

import otp.model.User;

import java.util.List;

public interface UserRepository {
    User findByUsername(String username);
    User findById(Long id);
    List<User> findAllUsersWithoutAdmins();
    boolean adminExists();

    void create(User user);
    void delete(Long userId);
}
