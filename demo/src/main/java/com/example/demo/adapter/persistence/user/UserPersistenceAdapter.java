package com.example.demo.adapter.persistence.user;

import java.util.List;
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

	@Override
	public List<User> findAll() {
		return jpa.findAll();
	}

	@Override
	public boolean existsById(Long id) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'existsById'");
	}
}


