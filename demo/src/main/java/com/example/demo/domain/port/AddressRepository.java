package com.example.demo.domain.port;

import com.example.demo.domain.model.Address;
import java.util.List;
import java.util.Optional;

public interface AddressRepository {
	Address save(Address address);
	Optional<Address> findById(Long id);
	List<Address> findByUserId(Long userId);
	void deleteById(Long id);
}


