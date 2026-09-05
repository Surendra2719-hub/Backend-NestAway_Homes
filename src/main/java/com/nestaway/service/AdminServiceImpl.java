package com.nestaway.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.nestaway.entity.Booking;
import com.nestaway.entity.Property;
import com.nestaway.entity.User;
import com.nestaway.repository.BookingRepository;
import com.nestaway.repository.PropertyRepository;
import com.nestaway.repository.UserRepository;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final BookingRepository bookingRepository;

    public AdminServiceImpl(
            UserRepository userRepository,
            PropertyRepository propertyRepository,
            BookingRepository bookingRepository) {

        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public List<User> getAllHosts() {
        return userRepository.findByRole("HOST");
    }

    @Override
    public List<User> getAllGuests() {
        return userRepository.findByRole("GUEST");
    }

    @Override
    public long countPropertiesForHost(Long hostId) {
        return propertyRepository.findByHostId(hostId).size();
    }

    @Override
    public long countBookingsForGuest(Long guestId) {
        return bookingRepository.findByGuestId(guestId).size();
    }

    @Override
    public List<Property> getPropertiesForHost(Long hostId) {
        return propertyRepository.findByHostId(hostId);
    }

    @Override
    public List<Booking> getBookingsForGuest(Long guestId) {
        return bookingRepository.findByGuestId(guestId);
    }
}