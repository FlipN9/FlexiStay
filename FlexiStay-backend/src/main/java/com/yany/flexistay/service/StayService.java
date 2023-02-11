package com.yany.flexistay.service;

import com.yany.flexistay.exception.StayDeleteException;
import com.yany.flexistay.exception.StayNotExistException;
import com.yany.flexistay.model.*;
import com.yany.flexistay.repository.LocationRepository;
import com.yany.flexistay.repository.ReservationRepository;
import com.yany.flexistay.repository.StayRepository;
import com.yany.flexistay.repository.StayReservationDateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StayService {
    private StayRepository stayRepository;
    private LocationRepository locationRepository;
    private ImageStorageService imageStorageService;
    private GeoCodingService geoCodingService;
    private ReservationRepository reservationRepository;
    private StayReservationDateRepository stayReservationDateRepository;

    @Autowired
    public StayService(StayRepository stayRepository, LocationRepository locationRepository,
                       ImageStorageService imageStorageService, GeoCodingService geoCodingService,
                       ReservationRepository reservationRepository, StayReservationDateRepository stayReservationDateRepository) {
        this.stayRepository = stayRepository;
        this.locationRepository = locationRepository;
        this.imageStorageService = imageStorageService;
        this.geoCodingService = geoCodingService;
        this.reservationRepository = reservationRepository;
        this.stayReservationDateRepository = stayReservationDateRepository;
    }

    public List<Stay> listByUser(String username) {
        return stayRepository.findByHost(new User.Builder().setUsername(username).build());
    }

    public Stay findByIdAndHost(Long stayId, String username) throws StayNotExistException {
        Stay stay = stayRepository.findByIdAndHost(stayId, new User.Builder().setUsername(username).build());
        if (stay == null) {
            throw new StayNotExistException("Stay doesn't exist");
        }
        return stay;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void add(Stay stay, MultipartFile[] images) {
        List<String> mediaLinks = Arrays.stream(images).parallel().map(image -> imageStorageService.save(image)).collect(Collectors.toList());
        List<StayImage> stayImages = new ArrayList<>();

        for (String mediaLink : mediaLinks) {
            stayImages.add(new StayImage(mediaLink, stay));
        }

        stay.setImages(stayImages);
        stayRepository.save(stay);
        Location location = geoCodingService.getLatLng(stay.getId(), stay.getAddress());
        locationRepository.save(location);
    }


    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void delete(Long stayId, String username) throws StayNotExistException {
        Stay stay = stayRepository.findByIdAndHost(stayId, new User.Builder().setUsername(username).build());
        if (stay == null) {
            throw new StayNotExistException("Stay doesn't exist");
        }

        List<Reservation> reservations = reservationRepository.findByStayAndCheckoutDateAfter(stay, LocalDate.now());
        if (reservations != null && reservations.size() > 0) {
            throw new StayDeleteException("Cannot delete stay with active reservation");
        }

        List<StayReservedDate> stayReservedDates = stayReservationDateRepository.findByStay(stay);

        for(StayReservedDate date : stayReservedDates) {
            stayReservationDateRepository.deleteById(date.getId());
        }

        stayRepository.deleteById(stayId);
    }
}
