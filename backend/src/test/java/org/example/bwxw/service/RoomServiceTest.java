package org.example.bwxw.service;

import org.example.bwxw.entity.Room;
import org.example.bwxw.entity.User;
import org.example.bwxw.repository.RoomRepository;
import org.example.bwxw.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RoomService roomService;

    @Test
    void checkoutKeepsStudentAndRoomOccupancyInSync() {
        Room room = new Room();
        room.setCapacity(4);
        room.setOccupied(4);
        room.setStatus(Room.RoomStatus.FULL);

        User student = new User();
        student.setId(1L);
        student.setRole(User.UserRole.STUDENT);
        student.setRoom(room);
        student.setStatus("ACTIVE");
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));

        roomService.removeStudentFromRoom(1L);

        assertNull(student.getRoom());
        assertEquals("INACTIVE", student.getStatus());
        assertEquals(3, room.getOccupied());
        assertEquals(Room.RoomStatus.AVAILABLE, room.getStatus());
        verify(userRepository).save(student);
        verify(roomRepository).save(room);
    }
}
