package com.github.phantomtrupe.userservice.repository;

import com.github.phantomtrupe.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT DISTINCT u.city FROM User u")
    List<String> findDistinctCities();

    List<User> findByCity(String city);
}
