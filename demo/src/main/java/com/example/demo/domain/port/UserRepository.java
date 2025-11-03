package com.example.demo.domain.port;

import com.example.demo.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
	User save(User user);
	Optional<User> findById(Long id);
	Optional<User> findByEmail(String email);
	boolean existsByEmail(String email);
	boolean existsById(Long id);
	List<User> findAll();
	
}


