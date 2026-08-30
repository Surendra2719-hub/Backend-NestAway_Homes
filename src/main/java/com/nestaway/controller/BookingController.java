package com.nestaway.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nestaway.dto.BookingRequest;
import com.nestaway.dto.BookingResponse;
import com.nestaway.entity.Booking;
import com.nestaway.service.BookingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/property/{propertyId}/guest/{guestId}")
    public BookingResponse createBooking(
            @PathVariable Long propertyId,
            @PathVariable Long guestId,
            @Valid @RequestBody BookingRequest bookingRequest) {

        Booking booking = convertToBooking(bookingRequest);

        Booking savedBooking = bookingService.createBooking(
                propertyId,
                guestId,
                booking
        );

        return convertToBookingResponse(savedBooking);
    }

    @GetMapping("/{bookingId}")
    public BookingResponse getBookingById(@PathVariable Long bookingId) {

        Booking booking = bookingService.getBookingById(bookingId);

        return convertToBookingResponse(booking);
    }

    @GetMapping("/guest/{guestId}")
    public List<BookingResponse> getBookingsByGuestId(
            @PathVariable Long guestId) {

        return bookingService.getBookingsByGuestId(guestId)
                .stream()
                .map(this::convertToBookingResponse)
                .toList();
    }

    @GetMapping("/host/{hostId}")
    public List<BookingResponse> getBookingsByHostId(
            @PathVariable Long hostId) {

        return bookingService.getBookingsByHostId(hostId)
                .stream()
                .map(this::convertToBookingResponse)
                .toList();
    }

    @PutMapping("/{bookingId}/guest/{guestId}/cancel")
    public BookingResponse cancelBooking(
            @PathVariable Long bookingId,
            @PathVariable Long guestId) {

        Booking booking = bookingService.cancelBooking(bookingId, guestId);

        return convertToBookingResponse(booking);
    }

    @PreAuthorize("hasRole('HOST')")
    @PutMapping("/{bookingId}/host/{hostId}/confirm")
    public BookingResponse confirmBooking(
            @PathVariable Long bookingId,
            @PathVariable Long hostId) {

        Booking booking = bookingService.confirmBooking(bookingId, hostId);

        return convertToBookingResponse(booking);
    }

    //=====================================Helper Methods ===========================================
    private Booking convertToBooking(BookingRequest bookingRequest) {

        Booking booking = new Booking();

        booking.setCheckInDate(bookingRequest.getCheckInDate());
        booking.setCheckOutDate(bookingRequest.getCheckOutDate());
        booking.setTotalGuests(bookingRequest.getTotalGuests());
        booking.setOccupantName(bookingRequest.getOccupantName());
        booking.setOccupantPhone(bookingRequest.getOccupantPhone());

        return booking;
    }

    private BookingResponse convertToBookingResponse(Booking booking) {

        BookingResponse bookingResponse = new BookingResponse();

        bookingResponse.setId(booking.getId());
        bookingResponse.setPropertyId(booking.getProperty().getId());
        bookingResponse.setPropertyTitle(booking.getProperty().getTitle());
        bookingResponse.setGuestId(booking.getGuest().getId());
        bookingResponse.setGuestName(booking.getGuest().getName());
        bookingResponse.setCheckInDate(booking.getCheckInDate());
        bookingResponse.setCheckOutDate(booking.getCheckOutDate());
        bookingResponse.setTotalGuests(booking.getTotalGuests());
        bookingResponse.setOccupantName(booking.getOccupantName());
        bookingResponse.setOccupantPhone(booking.getOccupantPhone());
        bookingResponse.setTotalPrice(booking.getTotalPrice());
        bookingResponse.setStatus(booking.getStatus());
        bookingResponse.setCreatedAt(booking.getCreatedAt());
        bookingResponse.setUpdatedAt(booking.getUpdatedAt());

        return bookingResponse;
    }
}