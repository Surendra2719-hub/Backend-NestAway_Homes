package com.nestaway.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nestaway.entity.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByGuestId(Long guestId);

    List<Booking> findByPropertyId(Long propertyId);

    // Host ke saare properties ki bookings (nested property traversal)
    List<Booking> findByProperty_HostId(Long hostId);

    // Overlap check: ek property pe koi CANCELLED-nahi booking already
    // in dates ke beech mein exist karti hai kya
    @Query("SELECT b FROM Booking b WHERE b.property.id = :propertyId " +
           "AND b.status <> 'CANCELLED' " +
           "AND b.checkInDate < :checkOutDate " +
           "AND b.checkOutDate > :checkInDate")
    List<Booking> findOverlappingBookings(
            @Param("propertyId") Long propertyId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate);
}