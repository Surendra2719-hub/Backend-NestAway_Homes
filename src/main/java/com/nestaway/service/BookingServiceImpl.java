package com.nestaway.service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nestaway.entity.Booking;
import com.nestaway.entity.Property;
import com.nestaway.entity.User;
import com.nestaway.exception.BookingConflictException;
import com.nestaway.exception.ResourceNotFoundException;
import com.nestaway.exception.UnauthorizedOperationException;
import com.nestaway.repository.BookingRepository;
import com.nestaway.repository.PropertyRepository;
import com.nestaway.repository.UserRepository;
import com.nestaway.config.AuthUtil;

@Service
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final AuthUtil authUtil;

    public BookingServiceImpl(
            BookingRepository bookingRepository,
            PropertyRepository propertyRepository,
            UserRepository userRepository,
            AuthUtil authUtil) {

        this.bookingRepository = bookingRepository;
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
        this.authUtil = authUtil;
    }

    @Override
    public Booking createBooking(Long propertyId, Long guestId, Booking booking) {

        verifySameUser(guestId);

        Property property = getPropertyById(propertyId);
        User guest = getUserById(guestId);

        if (!"APPROVED".equalsIgnoreCase(property.getStatus())) {
            throw new BookingConflictException(
                    "This property is not available for booking");
        }

        if (!booking.getCheckOutDate().isAfter(booking.getCheckInDate())) {
            throw new BookingConflictException(
                    "Check-out date must be after check-in date");
        }

        if (booking.getTotalGuests() > property.getMaxGuests()) {
            throw new BookingConflictException(
                    "Total guests exceeds this property's max guest limit of "
                            + property.getMaxGuests());
        }

        List<Booking> overlapping = bookingRepository.findOverlappingBookings(
                propertyId,
                booking.getCheckInDate(),
                booking.getCheckOutDate());

        if (!overlapping.isEmpty()) {
            throw new BookingConflictException(
                    "Property is already booked for the selected dates");
        }

        long nights = ChronoUnit.DAYS.between(
                booking.getCheckInDate(), booking.getCheckOutDate());

        BigDecimal totalPrice = property.getPricePerNight()
                .multiply(BigDecimal.valueOf(nights));

        if (booking.getOccupantName() == null || booking.getOccupantName().isBlank()) {
            booking.setOccupantName(guest.getName());
        }

        if (booking.getOccupantPhone() == null || booking.getOccupantPhone().isBlank()) {
            booking.setOccupantPhone(guest.getPhone());
        }

        booking.setProperty(property);
        booking.setGuest(guest);
        booking.setTotalPrice(totalPrice);
        booking.setStatus("PENDING");

        return bookingRepository.save(booking);
    }

    @Override
    public Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Booking not found with id: " + bookingId));
    }

    @Override
    public List<Booking> getBookingsByGuestId(Long guestId) {

        verifySameUser(guestId);
        getUserById(guestId);

        return bookingRepository.findByGuestId(guestId);
    }

    @Override
    public List<Booking> getBookingsByHostId(Long hostId) {

        verifySameUser(hostId);
        getUserById(hostId);

        return bookingRepository.findByProperty_HostId(hostId);
    }

    @Override
    public Booking cancelBooking(Long bookingId, Long guestId) {

        verifySameUser(guestId);

        Booking booking = getBookingById(bookingId);

        if (!booking.getGuest().getId().equals(guestId)) {
            throw new UnauthorizedOperationException(
                    "You can cancel only your own booking");
        }

        booking.setStatus("CANCELLED");

        return bookingRepository.save(booking);
    }

    @Override
    public Booking confirmBooking(Long bookingId, Long hostId) {

        verifySameUser(hostId);

        Booking booking = getBookingById(bookingId);

        if (!booking.getProperty().getHost().getId().equals(hostId)) {
            throw new UnauthorizedOperationException(
                    "You can confirm bookings only for your own property");
        }

        booking.setStatus("CONFIRMED");

        return bookingRepository.save(booking);
    }

    private Property getPropertyById(Long propertyId) {
        return propertyRepository.findById(propertyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Property not found with id: " + propertyId));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + userId));
    }

    private void verifySameUser(Long claimedId) {
        Long actualLoggedInUserId = authUtil.getCurrentUserId();
        if (!actualLoggedInUserId.equals(claimedId)) {
            throw new UnauthorizedOperationException(
                    "You are not authorized to perform this action for another user");
        }
    }
}