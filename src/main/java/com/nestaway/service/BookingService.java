package com.nestaway.service;

import java.util.List;

import com.nestaway.entity.Booking;

public interface BookingService {

    Booking createBooking(Long propertyId, Long guestId, Booking booking);

    Booking getBookingById(Long bookingId);

    List<Booking> getBookingsByGuestId(Long guestId);

    List<Booking> getBookingsByHostId(Long hostId);

    Booking cancelBooking(Long bookingId, Long guestId);

    Booking confirmBooking(Long bookingId, Long hostId);
}