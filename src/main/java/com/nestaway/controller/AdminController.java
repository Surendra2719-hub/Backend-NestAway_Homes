package com.nestaway.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nestaway.dto.BookingResponse;
import com.nestaway.dto.GuestSummaryResponse;
import com.nestaway.dto.HostSummaryResponse;
import com.nestaway.dto.PropertyResponse;
import com.nestaway.entity.Booking;
import com.nestaway.entity.Property;
import com.nestaway.entity.User;
import com.nestaway.service.AdminService;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // Sabhi Hosts ki list, har ek ki total property count ke saath
    @GetMapping("/hosts")
    public List<HostSummaryResponse> getAllHosts() {

        return adminService.getAllHosts()
                .stream()
                .map(this::convertToHostSummary)
                .toList();
    }

    // Ek specific Host ki saari properties (chahe PENDING ho, APPROVED ho, ya REJECTED)
    @GetMapping("/hosts/{hostId}/properties")
    public List<PropertyResponse> getPropertiesForHost(@PathVariable Long hostId) {

        return adminService.getPropertiesForHost(hostId)
                .stream()
                .map(this::convertToPropertyResponse)
                .toList();
    }

    // Sabhi Guests ki list, har ek ki total booking count ke saath
    @GetMapping("/guests")
    public List<GuestSummaryResponse> getAllGuests() {

        return adminService.getAllGuests()
                .stream()
                .map(this::convertToGuestSummary)
                .toList();
    }

    // Ek specific Guest ne konsi-konsi properties book ki hain
    @GetMapping("/guests/{guestId}/bookings")
    public List<BookingResponse> getBookingsForGuest(@PathVariable Long guestId) {

        return adminService.getBookingsForGuest(guestId)
                .stream()
                .map(this::convertToBookingResponse)
                .toList();
    }

    //=====================================Helper Methods ===========================================
    private HostSummaryResponse convertToHostSummary(User host) {

        HostSummaryResponse response = new HostSummaryResponse();

        response.setId(host.getId());
        response.setName(host.getName());
        response.setEmail(host.getEmail());
        response.setPhone(host.getPhone());
        response.setPropertyCount(adminService.countPropertiesForHost(host.getId()));
        response.setCreatedAt(host.getCreatedAt());

        return response;
    }

    private GuestSummaryResponse convertToGuestSummary(User guest) {

        GuestSummaryResponse response = new GuestSummaryResponse();

        response.setId(guest.getId());
        response.setName(guest.getName());
        response.setEmail(guest.getEmail());
        response.setPhone(guest.getPhone());
        response.setBookingCount(adminService.countBookingsForGuest(guest.getId()));
        response.setCreatedAt(guest.getCreatedAt());

        return response;
    }

    private PropertyResponse convertToPropertyResponse(Property property) {

        PropertyResponse response = new PropertyResponse();

        response.setId(property.getId());
        response.setTitle(property.getTitle());
        response.setDescription(property.getDescription());
        response.setPropertyType(property.getPropertyType());
        response.setAddress(property.getAddress());
        response.setCity(property.getCity());
        response.setState(property.getState());
        response.setCountry(property.getCountry());
        response.setPricePerNight(property.getPricePerNight());
        response.setMaxGuests(property.getMaxGuests());
        response.setBedrooms(property.getBedrooms());
        response.setBeds(property.getBeds());
        response.setBathrooms(property.getBathrooms());
        response.setStatus(property.getStatus());
        response.setHostId(property.getHost().getId());
        response.setHostName(property.getHost().getName());
        response.setCreatedAt(property.getCreatedAt());
        response.setUpdatedAt(property.getUpdatedAt());

        return response;
    }

    private BookingResponse convertToBookingResponse(Booking booking) {

        BookingResponse response = new BookingResponse();

        response.setId(booking.getId());
        response.setPropertyId(booking.getProperty().getId());
        response.setPropertyTitle(booking.getProperty().getTitle());
        response.setGuestId(booking.getGuest().getId());
        response.setGuestName(booking.getGuest().getName());
        response.setCheckInDate(booking.getCheckInDate());
        response.setCheckOutDate(booking.getCheckOutDate());
        response.setTotalGuests(booking.getTotalGuests());
        response.setOccupantName(booking.getOccupantName());
        response.setOccupantPhone(booking.getOccupantPhone());
        response.setTotalPrice(booking.getTotalPrice());
        response.setStatus(booking.getStatus());
        response.setCreatedAt(booking.getCreatedAt());
        response.setUpdatedAt(booking.getUpdatedAt());

        return response;
    }
}