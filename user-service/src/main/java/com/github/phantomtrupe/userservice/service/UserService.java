package com.github.phantomtrupe.userservice.service;

import com.github.phantomtrupe.commons.dto.UserDTO;
import java.util.List;

public interface UserService {
    UserDTO createUser(UserDTO userDto);
    UserDTO getUserById(Long id);
    UserDTO updateUser(Long id, UserDTO userDto);
    void deleteUser(Long id);
    List<UserDTO> getAllUsers();
    List<String> getDistinctCities();
}
