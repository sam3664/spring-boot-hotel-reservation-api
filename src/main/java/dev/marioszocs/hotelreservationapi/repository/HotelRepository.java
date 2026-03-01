package dev.marioszocs.hotelreservationapi.repository;

import dev.marioszocs.hotelreservationapi.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Integer> {

    @Query("""
           SELECT h FROM Hotel h
           WHERE h.availableFrom <= :dateFrom
           AND h.availableTo >= :dateTo
           AND h.id NOT IN (
               SELECT r.hotelId FROM Reservation r
               WHERE (r.checkIn < :dateTo AND r.checkOut > :dateFrom)
           )
           """)
    List<Hotel> findAvailableHotels(@Param("dateFrom") String dateFrom, @Param("dateTo") String dateTo);
}
