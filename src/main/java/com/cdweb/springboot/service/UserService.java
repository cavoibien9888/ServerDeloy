package com.cdweb.springboot.service;

import com.cdweb.springboot.entities.User;

public interface UserService {

	public User getUserById(Long userld);
	public User getUserProfileByJwt(String jwt);
	public User getUserByEmailAndPassword(String email, String password);
	
}
