package org.delcom.app.services;

import org.delcom.app.entities.Schedule;
import org.delcom.app.entities.User;
import org.delcom.app.repositories.ScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private ScheduleRepository repository;

    @Mock
    private User mockUser;

    @InjectMocks
    private ScheduleService scheduleService;

    private Schedule mockSchedule;
    private final Long TEST_ID = 1L;

    @BeforeEach
    void setUp() {
        mockSchedule = new Schedule();
        mockSchedule.setId(TEST_ID);
    }

    @Test
    void getSchedulesByUser_ShouldReturnListFromRepository() {
        List<Schedule> mockList = Collections.singletonList(mockSchedule);
        when(repository.findByUser(mockUser)).thenReturn(mockList);
        
        // Method main: getSchedulesByUser
        List<Schedule> result = scheduleService.getSchedulesByUser(mockUser);
        
        assertEquals(1, result.size());
        verify(repository, times(1)).findByUser(mockUser);
    }

    @Test
    void saveSchedule_ShouldSetUserAndSaveToRepository() {
        // Method main: saveSchedule
        scheduleService.saveSchedule(mockSchedule, mockUser);
        
        assertEquals(mockUser, mockSchedule.getUser());
        verify(repository, times(1)).save(mockSchedule);
    }

    @Test
    void getScheduleById_Found() {
        when(repository.findById(TEST_ID)).thenReturn(Optional.of(mockSchedule));
        // Method main: getScheduleById
        assertNotNull(scheduleService.getScheduleById(TEST_ID));
    }

    @Test
    void deleteSchedule_ShouldCallDeleteByIdOnRepository() {
        // Method main: deleteSchedule
        scheduleService.deleteSchedule(TEST_ID);
        verify(repository, times(1)).deleteById(TEST_ID);
    }
}