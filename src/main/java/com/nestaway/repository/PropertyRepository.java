package com.nestaway.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nestaway.entity.Property;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
	
	//all approved properties for guests
	List<Property> findByStatus(String status);

    List<Property> findByHostId(Long hostId);

    // Search + Pagination — city aur price-range dono optional hain.
    // Jo filter null aaye, uski condition ignore ho jaati hai (:param IS NULL check se).
    @Query("SELECT p FROM Property p WHERE p.status = 'APPROVED' " +
           "AND (:city IS NULL OR LOWER(p.city) = LOWER(:city)) " +
           "AND (:minPrice IS NULL OR p.pricePerNight >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.pricePerNight <= :maxPrice)")
    Page<Property> searchApprovedProperties(
            @Param("city") String city,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

}