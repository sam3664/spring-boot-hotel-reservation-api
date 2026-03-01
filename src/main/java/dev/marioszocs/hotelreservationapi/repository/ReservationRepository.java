package dev.marioszocs.hotelreservationapi.repository;

import dev.marioszocs.hotelreservationapi.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
    boolean existsByHotelId(Integer hotelId);
    
    @Query("""
           SELECT COUNT(r) > 0 FROM Reservation r
           WHERE r.hotelId = :hotelId
           AND (r.checkIn < :checkOut AND r.checkOut > :checkIn)
           """)
    boolean existsOverlappingReservation(@Param("hotelId") Integer hotelId, 
                                        @Param("checkIn") String checkIn, 
                                        @Param("checkOut") String checkOut);
}
