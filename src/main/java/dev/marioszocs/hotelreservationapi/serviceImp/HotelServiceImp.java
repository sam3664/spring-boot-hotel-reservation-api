package dev.marioszocs.hotelreservationapi.serviceImp;

import dev.marioszocs.hotelreservationapi.constants.ErrorMessages;
import dev.marioszocs.hotelreservationapi.dto.IdEntity;
import dev.marioszocs.hotelreservationapi.dto.SuccessEntity;
import dev.marioszocs.hotelreservationapi.entity.Hotel;
import dev.marioszocs.hotelreservationapi.entity.Reservation;
import dev.marioszocs.hotelreservationapi.exception.InvalidRequestException;
import dev.marioszocs.hotelreservationapi.repository.HotelRepository;
import dev.marioszocs.hotelreservationapi.repository.ReservationRepository;
import dev.marioszocs.hotelreservationapi.service.HotelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Hotel Service that preforms operations regarding Hotel API Calls
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class HotelServiceImp implements HotelService {

    private final HotelRepository hotelRepository;
    private final ReservationRepository reservationRepository;

    /**
     * Return all existing Hotel objects in the database
     *
     * @return List<Hotel>
     */
    @Override
    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    /**
     * Return existing Hotel with pagination
     *
     * @param pageNo
     * @param pageSize
     * @param sortBy
     * @return
     */
    @Override
    public List<Hotel> getHotelPagedList(Integer pageNo, Integer pageSize, String sortBy) {
        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());
        return hotelRepository.findAll(paging).getContent();
    }

    /**
     * Returns a user specified Hotel item through the Hotel id
     *
     * @param id
     * @return Hotel
     */
    @Override
    public Hotel getHotel(Integer id) {
        return hotelRepository.findById(id)
                .orElseThrow(() -> new InvalidRequestException(ErrorMessages.INVALID_ID_EXISTENCE));
    }

    /**
     * Returns all Hotel objects in the database that are available in between user specified dates
     *
     * @param dateFrom
     * @param dateTo
     * @return
     */
    @Override
    public List<Hotel> getAvailable(String dateFrom, String dateTo) {
        return hotelRepository.findAvailableHotels(dateFrom, dateTo);
    }

    /**
     * Saves a user specified Hotel object to the database
     *
     * @param hotel
     * @return
     */
    @Override
    public IdEntity saveHotel(@Valid Hotel hotel) {
        // If dates are empty strings make them null values so that they can be accepted by the database
        if (!StringUtils.hasText(hotel.getAvailableFrom())) {
            hotel.setAvailableFrom(null);
        }
        if (!StringUtils.hasText(hotel.getAvailableTo())) {
            hotel.setAvailableTo(null);
        }
        Hotel savedHotel = hotelRepository.save(hotel);
        IdEntity idEntity = new IdEntity();
        idEntity.setId(savedHotel.getId());
        return idEntity;
    }

    /**
     * Deletes a user specified Hotel object from the database
     *
     * @param id
     * @return
     */
    @Override
    public SuccessEntity deleteHotel(Integer id) {
        if (!hotelRepository.existsById(id)) {
            throw new InvalidRequestException(ErrorMessages.INVALID_ID_EXISTENCE);
        }
        if (reservationRepository.existsByHotelId(id)) {
            throw new InvalidRequestException(ErrorMessages.INVALID_HOTEL_DELETE);
        }
        hotelRepository.deleteById(id);
        SuccessEntity successEntity = new SuccessEntity();
        successEntity.setSuccess(true);
        return successEntity;
    }

    /**
     * Updates a pre-existing Hotel object in the database
     *
     * @param hotel
     * @return
     */
    @Override
    public SuccessEntity patchHotel(Hotel hotel) {
        if (!hotelRepository.existsById(hotel.getId())) {
            throw new InvalidRequestException(ErrorMessages.INVALID_ID_EXISTENCE);
        }
        doesReservationOverlap(hotel);
        hotelRepository.save(hotel);
        SuccessEntity successEntity = new SuccessEntity();
        successEntity.setSuccess(true);
        return successEntity;
    }

    /**
     * Checks to see if a reservation date overlaps with the inventory dates
     *
     * @param hotel
     */
    @Override
    public void doesReservationOverlap(Hotel hotel) {
        if (!StringUtils.hasText(hotel.getAvailableTo()) || !StringUtils.hasText(hotel.getAvailableFrom())) {
            if (reservationRepository.existsByHotelId(hotel.getId())) {
                throw new InvalidRequestException(ErrorMessages.INVALID_DATE_CHANGE_NULL);
            }
            return;
        }

        boolean hasOverlap = reservationRepository.findAll().stream()
                .filter(r -> r.getHotelId().equals(hotel.getId()))
                .anyMatch(r -> r.getCheckIn().compareTo(hotel.getAvailableFrom()) < 0 
                            || r.getCheckOut().compareTo(hotel.getAvailableTo()) > 0);

        if (hasOverlap) {
            throw new InvalidRequestException(ErrorMessages.INVALID_HOTEL_UPDATE);
        }
    }

    /**
     * Checks the existence of a Hotel object in the database
     *
     * @param id
     * @return
     */
    @Override
    public boolean validateHotelExistenceById(Integer id) {
        if (!hotelRepository.existsById(id)) {
            log.error("Invalid ID: The entered id = {} does not exist.", id);
            throw new InvalidRequestException(ErrorMessages.INVALID_ID_EXISTENCE);
        }
        return true;
    }
}
