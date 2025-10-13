package com.example.demo.adapter.user;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.demo.domain.model.User;
import com.example.demo.domain.port.UserRepository;

@Repository
public class UserPersistenceAdapter implements UserRepository {

	private final UserJpaRepository jpa;

	public UserPersistenceAdapter(UserJpaRepository jpa) { this.jpa = jpa; }

	@Override
	public User save(User user) { return jpa.save(user); }

	@Override
	public Optional<User> findById(Long id) { return jpa.findById(id); }

	@Override
	public Optional<User> findByEmail(String email) { return jpa.findByEmail(email); }

	@Override
	public boolean existsByEmail(String email) { return jpa.existsByEmail(email); }
}


