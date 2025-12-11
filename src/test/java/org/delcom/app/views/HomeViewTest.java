package org.delcom.app.views;

import org.delcom.app.entities.Plant;
import org.delcom.app.entities.PlantHealth;
import org.delcom.app.entities.Schedule;
import org.delcom.app.entities.User;
import org.delcom.app.services.PlantHealthService;
import org.delcom.app.services.PlantService;
import org.delcom.app.services.ScheduleService;
import org.delcom.app.services.UserService;
import org.delcom.app.utils.ConstUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HomeViewTest {

    @Mock
    private UserService userService;

    @Mock
    private PlantService plantService;

    @Mock
    private ScheduleService scheduleService;

    @Mock
    private PlantHealthService plantHealthService;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private HomeView homeView;

    private User penggunaTest;

    @BeforeEach
    void setUp() {
        penggunaTest = new User();
        penggunaTest.setEmail("test@example.com");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userService.getUserByEmail("test@example.com")).thenReturn(penggunaTest);
    }

    private Plant buatTanaman(LocalDate tanggalPenyiraman, Integer frekuensiPenyiraman) {
        Plant tanaman = new Plant();
        tanaman.setLastWatered(tanggalPenyiraman);
        tanaman.setWateringFrequency(frekuensiPenyiraman);
        return tanaman;
    }

    @Test
    void testHome_TanpaTanaman_MengembalikanSkorNol() {
        // Arrange
        when(plantService.getPlantsByUser(penggunaTest)).thenReturn(Collections.emptyList());
        when(scheduleService.getSchedulesByUser(penggunaTest)).thenReturn(Collections.emptyList());
        when(plantHealthService.getLogsByUser(penggunaTest)).thenReturn(Collections.emptyList());

        // Act
        String namaView = homeView.home(model);

        // Assert
        assertEquals(ConstUtil.TEMPLATE_PAGES_HOME, namaView);
        verify(model).addAttribute(eq("healthStat"), eq(0));
        verify(model).addAttribute(eq("waterStat"), eq(0));
        verify(model).addAttribute(eq("scheduleStat"), eq(0));
    }

    @Test
    void testHome_DenganTanamanSehat_Mengembalikan100SkorKesehatan() {
        // Arrange
        Plant tanaman = buatTanaman(LocalDate.now(), 7);
        List<Plant> daftarTanaman = Arrays.asList(tanaman);
        
        PlantHealth logKesehatan = new PlantHealth();
        logKesehatan.setStatus("Sembuh");
        
        when(plantService.getPlantsByUser(penggunaTest)).thenReturn(daftarTanaman);
        when(scheduleService.getSchedulesByUser(penggunaTest)).thenReturn(Collections.emptyList());
        when(plantHealthService.getLogsByUser(penggunaTest)).thenReturn(Arrays.asList(logKesehatan));

        // Act
        String namaView = homeView.home(model);

        // Assert
        assertEquals(ConstUtil.TEMPLATE_PAGES_HOME, namaView);
        verify(model).addAttribute(eq("healthStat"), eq(100));
    }

    @Test
    void testHome_DenganTanamanSakit_MenurunkanSkorKesehatan() {
        // Arrange
        Plant tanaman = buatTanaman(LocalDate.now(), 7);
        List<Plant> daftarTanaman = Arrays.asList(tanaman);
        
        PlantHealth logSakit = new PlantHealth();
        logSakit.setStatus("Sakit");
        
        when(plantService.getPlantsByUser(penggunaTest)).thenReturn(daftarTanaman);
        when(scheduleService.getSchedulesByUser(penggunaTest)).thenReturn(Collections.emptyList());
        when(plantHealthService.getLogsByUser(penggunaTest)).thenReturn(Arrays.asList(logSakit));

        // Act
        String namaView = homeView.home(model);

        // Assert
        assertEquals(ConstUtil.TEMPLATE_PAGES_HOME, namaView);
        verify(model).addAttribute(eq("healthStat"), eq(0)); // 100% sakit = 0 kesehatan
    }

    @Test
    void testHome_TanamanDisiramBaik_Mengembalikan100SkorAir() {
        // Arrange
        LocalDate hariIni = LocalDate.now();
        Plant tanaman = buatTanaman(hariIni, 7); // Disiram hari ini, perlu air setiap 7 hari
        
        when(plantService.getPlantsByUser(penggunaTest)).thenReturn(Arrays.asList(tanaman));
        when(scheduleService.getSchedulesByUser(penggunaTest)).thenReturn(Collections.emptyList());
        when(plantHealthService.getLogsByUser(penggunaTest)).thenReturn(Collections.emptyList());

        // Act
        String namaView = homeView.home(model);

        // Assert
        assertEquals(ConstUtil.TEMPLATE_PAGES_HOME, namaView);
        verify(model).addAttribute(eq("waterStat"), eq(100));
    }

    @Test
    void testHome_TanamanTerlambatDisiram_Mengembalikan0SkorAir() {
        // Arrange
        LocalDate sepuluhHariLalu = LocalDate.now().minusDays(10);
        Plant tanaman = buatTanaman(sepuluhHariLalu, 7); // Terakhir disiram 10 hari lalu, perlu air setiap 7 hari
        
        when(plantService.getPlantsByUser(penggunaTest)).thenReturn(Arrays.asList(tanaman));
        when(scheduleService.getSchedulesByUser(penggunaTest)).thenReturn(Collections.emptyList());
        when(plantHealthService.getLogsByUser(penggunaTest)).thenReturn(Collections.emptyList());

        // Act
        String namaView = homeView.home(model);

        // Assert
        assertEquals(ConstUtil.TEMPLATE_PAGES_HOME, namaView);
        verify(model).addAttribute(eq("waterStat"), eq(0));
    }

    @Test
    void testHome_TanamanDenganLastWateredNull_MelompatiKalkulasiAir() {
        // Arrange
        Plant tanaman = new Plant();
        tanaman.setLastWatered(null);
        tanaman.setWateringFrequency(7);
        
        when(plantService.getPlantsByUser(penggunaTest)).thenReturn(Arrays.asList(tanaman));
        when(scheduleService.getSchedulesByUser(penggunaTest)).thenReturn(Collections.emptyList());
        when(plantHealthService.getLogsByUser(penggunaTest)).thenReturn(Collections.emptyList());

        // Act
        String namaView = homeView.home(model);

        // Assert
        assertEquals(ConstUtil.TEMPLATE_PAGES_HOME, namaView);
        verify(model).addAttribute(eq("waterStat"), eq(0));
    }

    @Test
    void testHome_TanamanDenganWateringFrequencyNull_MelompatiKalkulasiAir() {
        // Arrange
        Plant tanaman = new Plant();
        tanaman.setLastWatered(LocalDate.now());
        tanaman.setWateringFrequency(null);
        
        when(plantService.getPlantsByUser(penggunaTest)).thenReturn(Arrays.asList(tanaman));
        when(scheduleService.getSchedulesByUser(penggunaTest)).thenReturn(Collections.emptyList());
        when(plantHealthService.getLogsByUser(penggunaTest)).thenReturn(Collections.emptyList());

        // Act
        String namaView = homeView.home(model);

        // Assert
        assertEquals(ConstUtil.TEMPLATE_PAGES_HOME, namaView);
        verify(model).addAttribute(eq("waterStat"), eq(0));
    }

    @Test
    void testHome_DenganJadwal_MenghitungSkorJadwal() {
        // Arrange
        Plant tanaman = buatTanaman(LocalDate.now(), 7);
        Schedule jadwal = new Schedule();
        
        when(plantService.getPlantsByUser(penggunaTest)).thenReturn(Arrays.asList(tanaman));
        when(scheduleService.getSchedulesByUser(penggunaTest)).thenReturn(Arrays.asList(jadwal));
        when(plantHealthService.getLogsByUser(penggunaTest)).thenReturn(Collections.emptyList());

        // Act
        String namaView = homeView.home(model);

        // Assert
        assertEquals(ConstUtil.TEMPLATE_PAGES_HOME, namaView);
        verify(model).addAttribute(eq("scheduleStat"), eq(100)); // 1 jadwal / 1 tanaman = 100%
    }

    @Test
    void testHome_JadwalLebihDariTanaman_MaksimalDi100() {
        // Arrange
        Plant tanaman = buatTanaman(LocalDate.now(), 7);
        Schedule jadwal1 = new Schedule();
        Schedule jadwal2 = new Schedule();
        Schedule jadwal3 = new Schedule();
        
        when(plantService.getPlantsByUser(penggunaTest)).thenReturn(Arrays.asList(tanaman));
        when(scheduleService.getSchedulesByUser(penggunaTest)).thenReturn(Arrays.asList(jadwal1, jadwal2, jadwal3));
        when(plantHealthService.getLogsByUser(penggunaTest)).thenReturn(Collections.emptyList());

        // Act
        String namaView = homeView.home(model);

        // Assert
        assertEquals(ConstUtil.TEMPLATE_PAGES_HOME, namaView);
        verify(model).addAttribute(eq("scheduleStat"), eq(100)); // Maksimal di 100
    }

    @Test
    void testHome_SkenarioCampuran_MenghitungSemuaSkor() {
        // Arrange
        Plant tanaman1 = buatTanaman(LocalDate.now(), 7);
        Plant tanaman2 = buatTanaman(LocalDate.now().minusDays(10), 7);
        List<Plant> daftarTanaman = Arrays.asList(tanaman1, tanaman2);
        
        PlantHealth logKesehatan1 = new PlantHealth();
        logKesehatan1.setStatus("Sembuh");
        PlantHealth logKesehatan2 = new PlantHealth();
        logKesehatan2.setStatus("Sakit");
        
        Schedule jadwal = new Schedule();
        
        when(plantService.getPlantsByUser(penggunaTest)).thenReturn(daftarTanaman);
        when(scheduleService.getSchedulesByUser(penggunaTest)).thenReturn(Arrays.asList(jadwal));
        when(plantHealthService.getLogsByUser(penggunaTest)).thenReturn(Arrays.asList(logKesehatan1, logKesehatan2));

        // Act
        String namaView = homeView.home(model);

        // Assert
        assertEquals(ConstUtil.TEMPLATE_PAGES_HOME, namaView);
        verify(model).addAttribute(eq("healthStat"), eq(50)); // 50% sakit
        verify(model).addAttribute(eq("waterStat"), eq(50)); // 1 dari 2 disiram baik
        verify(model).addAttribute(eq("scheduleStat"), eq(50)); // 1 jadwal untuk 2 tanaman
    }

    @Test
    void testHome_BeberaTanamanDenganNullValues_TetapMenghitung() {
        // Arrange
        Plant tanaman1 = buatTanaman(LocalDate.now(), 7);
        Plant tanaman2 = new Plant();
        tanaman2.setLastWatered(null);
        tanaman2.setWateringFrequency(null);
        
        when(plantService.getPlantsByUser(penggunaTest)).thenReturn(Arrays.asList(tanaman1, tanaman2));
        when(scheduleService.getSchedulesByUser(penggunaTest)).thenReturn(Collections.emptyList());
        when(plantHealthService.getLogsByUser(penggunaTest)).thenReturn(Collections.emptyList());

        // Act
        String namaView = homeView.home(model);

        // Assert
        assertEquals(ConstUtil.TEMPLATE_PAGES_HOME, namaView);
        verify(model).addAttribute(eq("waterStat"), eq(50)); // Hanya 1 dari 2 yang valid
    }
}