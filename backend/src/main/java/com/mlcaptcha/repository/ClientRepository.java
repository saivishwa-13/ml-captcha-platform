package com.mlcaptcha.repository;

import com.mlcaptcha.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {
    Optional<Client> findByApiKey(String apiKey);
    Optional<Client> findByApiKeyAndIsActiveTrue(String apiKey);
    boolean existsByApiKey(String apiKey);
}
