package agh.matury.university;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UniversityRepository extends JpaRepository<University, Long> {

    @Query("""
        SELECT u FROM University u
        WHERE
            (LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) 
            OR LOWER(u.acronym) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
        AND LOWER(u.city) LIKE LOWER(CONCAT('%', :city, '%'))
    """)
    Page<University> findByNameOrAcronymAndCity(
            @Param("searchTerm") String searchTerm,
            @Param("city") String city,
            Pageable pageable
    );
}
