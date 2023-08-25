/**
 * 
 */
package com.wibmo.dao;

import java.util.List;
import java.util.Set;

import com.wibmo.entity.User;
import com.wibmo.enums.RegistrationStatus;

/**
 * 
 */
public interface UserDAO {
	
	public List<Integer> find();
	
	public Boolean update(String status,int userId);
	/**
	 * 
	 * @param registrationStatus
	 * @return
	 */
	public List<User> findAllByRegistrationStatus(
			RegistrationStatus registrationStatus);

	/**
	 * Inserts the new user
	 * @param user
	 */
	public void save(User user);
	
	/**
	 * 
	 * @param email
	 * @return
	 */
	public Integer findUserIdByEmail(String email);

	/**
	 * 
	 * @param rejected
	 * @param userIds
	 * @return
	 */
	public Boolean updateRegistrationStatusAsByIdIn(
			RegistrationStatus registrationStatus, 
			Set<Integer> userIds);

	/**
	 * 
	 * @param userId
	 * @return
	 */
	public Boolean existsById(Integer userId);

	User findUserByEmail(String email);
}
