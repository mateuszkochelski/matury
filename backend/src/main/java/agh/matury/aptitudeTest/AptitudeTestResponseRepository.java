package agh.matury.aptitudeTest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AptitudeTestResponseRepository extends JpaRepository<AptitudeTestResponse, Long> {
    Optional<AptitudeTestResponse> findBySessionId(String sessionId);
} 