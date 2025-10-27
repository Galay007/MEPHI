package com.example.hotel;

import com.example.hotel.model.Hotel;
import com.example.hotel.model.Room;
import com.example.hotel.model.RoomReservationLock;
import com.example.hotel.repo.HotelRepository;
import com.example.hotel.service.HotelService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
public class HotelMoreTests {

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private HotelService hotelService;

    @Test
    @Transactional
    void availableFlagDoesNotAffectDateOccupancy() {
        Hotel h = new Hotel();
        h.setName("H");
        h.setCity("C");
        h = hotelRepository.save(h);
        Room r = new Room();
        r.setHotel(h);
        r.setNumber("102");
        r.setCapacity(2);
        r.setAvailable(false);
        r = hotelService.saveRoom(r);

        // Даже если available=false, занятость по датам определяется блокировками/бронями
        LocalDate s1 = LocalDate.now();
        LocalDate e1 = s1.plusDays(1);
        RoomReservationLock lock = hotelService.holdRoom("req-c", r.getId(), s1, e1);
        Assertions.assertEquals(RoomReservationLock.Status.HELD, lock.getStatus());
    }


}


