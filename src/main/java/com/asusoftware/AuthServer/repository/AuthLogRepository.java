package com.asusoftware.AuthServer.repository;

import com.asusoftware.AuthServer.entity.AuthLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuthLogRepository extends JpaRepository<AuthLog, UUID> {

}
