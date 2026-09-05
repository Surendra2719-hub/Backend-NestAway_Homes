package com.nestaway.service;

import java.util.List;

import com.nestaway.entity.Booking;
import com.nestaway.entity.Property;
import com.nestaway.entity.User;

public interface AdminService {

    List<User> getAllHosts();

    List<User> getAllGuests();

    long countPropertiesForHost(Long hostId);

    long countBookingsForGuest(Long guestId);

    List<Property> getPropertiesForHost(Long hostId);

    List<Booking> getBookingsForGuest(Long guestId);
}