package dev.marioszocs.hotelreservationapi.serviceImp;

import dev.marioszocs.hotelreservationapi.constants.ErrorMessages;
import dev.marioszocs.hotelreservationapi.dto.IdEntity;
import dev.marioszocs.hotelreservationapi.dto.SuccessEntity;
import dev.marioszocs.hotelreservationapi.entity.Hotel;
import dev.marioszocs.hotelreservationapi.entity.Reservation;
import dev.marioszocs.hotelreservationapi.exception.InvalidRequestException;
import dev.marioszocs.hotelreservationapi.repository.HotelRepository;
import dev.marioszocs.hotelreservationapi.repository.ReservationRepository;
import dev.marioszocs.hotelreservationapi.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.transaction.Transactional;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Reservation Service tha performs operations regarding Reservation API Calls
 */
@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class ReservationServiceImp implements ReservationService {
    private final ReservationRepository reservationRepository;
    private final HotelRepository hotelRepository;

    /**
     * Returns all existing Reservation objects in the database
     * @return
     */
    @Override
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    /**
     * Finds a user specified Reservation in the database
     * @param id
     * @return
     */
    @Override
    public Reservation getReservation(Integer id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new InvalidRequestException(ErrorMessages.INVALID_ID_EXISTENCE));
    }

    /**
     * Saves a user created Reservation object to the database
     * @param reservations
     * @return
     */
    @Override
    public IdEntity saveReservation(Reservation reservations) {
        Integer hotelId = reservations.getHotelId();
        
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new InvalidRequestException(ErrorMessages.INVALID_HOTEL_IN_RESERVATION));

        if (!StringUtils.hasText(hotel.getAvailableFrom()) || !StringUtils.hasText(hotel.getAvailableTo())) {
            throw new InvalidRequestException(ErrorMessages.EMPTY_HOTEL_DATES);
        }

        if (reservationRepository.existsOverlappingReservation(hotelId, reservations.getCheckIn(), reservations.getCheckOut())) {
            throw new InvalidRequestException(ErrorMessages.INVALID_DATE_OVERLAP);
        }

        if (reservations.getCheckIn().compareTo(hotel.getAvailableFrom()) >= 0 
                && reservations.getCheckOut().compareTo(hotel.getAvailableTo()) <= 0) {
            Reservation savedReservation = reservationRepository.save(reservations);
            IdEntity idEntity = new IdEntity();
            idEntity.setId(savedReservation.getId());
            return idEntity;
        } else {
            throw new InvalidRequestException(ErrorMessages.INVALID_RESERVATION_DATES);
        }
    }

    /**
     * Deletes a user specified Reservation object from the database
     *
     * @param id
     * @return
     */
    @Override
    public SuccessEntity deleteReservation(Integer id) {
        if (!reservationRepository.existsById(id)) {
            throw new InvalidRequestException(ErrorMessages.INVALID_ID_EXISTENCE);
        }
        reservationRepository.deleteById(id);
        SuccessEntity successEntity = new SuccessEntity();
        successEntity.setSuccess(true);
        return successEntity;
    }

    /**
     * Checks to existene of a Hotel object in the database
     * @param id
     * @return
     */
    @Override
    public boolean validateHotelExistenceById(Integer id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new InvalidRequestException(ErrorMessages.INVALID_ID_EXISTENCE));
        
        if (!StringUtils.hasText(hotel.getAvailableFrom()) || !StringUtils.hasText(hotel.getAvailableTo())) {
            throw new InvalidRequestException(ErrorMessages.EMPTY_HOTEL_DATES);
        }
        return true;
    }

    /**
     * Checks the chronological order of user specified dates
     *
     * @param date1
     * @param date2
     * @return
     */
    @Override
    public boolean dateIsBefore(String date1, String date2) {
        return date1.compareTo(date2) < 0;
    }

    /**
     * Checks to see if a user specified Reservation overlaps with a pre-existing Reservation in the database
     *
     * @param reservations
     * @return
     */
    @Override
    public boolean reservationOverlaps(Reservation reservations) {
        return reservationRepository.existsOverlappingReservation(
                reservations.getHotelId(), 
                reservations.getCheckIn(), 
                reservations.getCheckOut());
    }

    /**
     * Checks the existence of a user specified Reservation object in the database
     *
     * @param id
     * @return
     */
    @Override
    public boolean validateReservationExistence(Integer id) {
        if (!reservationRepository.existsById(id)) {
            throw new InvalidRequestException(ErrorMessages.INVALID_ID_EXISTENCE);
        }
        return true;
    }
}
