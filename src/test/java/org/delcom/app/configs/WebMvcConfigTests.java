package org.delcom.app.configs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebMvcConfigTests {

    @InjectMocks
    private WebMvcConfig webMvcConfig;

    @Mock
    private ResourceHandlerRegistry registry;

    @Mock
    private ResourceHandlerRegistration registration;

    @Test
    void addResourceHandlers_ShouldRegisterUploadsAndStatic() {
        // 1. SETUP MOCKING
        // Gunakan anyString() untuk path handler
        when(registry.addResourceHandler(anyString())).thenReturn(registration);
        
        // PENTING: Gunakan any(String[].class) untuk menghindari "Ambiguous method call" pada varargs
        when(registration.addResourceLocations(any(String[].class))).thenReturn(registration);

        // 2. JALANKAN METHOD
        webMvcConfig.addResourceHandlers(registry);

        // 3. VERIFIKASI Registry dipanggil 2 kali (untuk uploads dan static)
        verify(registry).addResourceHandler("/uploads/**");
        verify(registry).addResourceHandler("/**");

        // 4. VERIFIKASI ResourceLocations menggunakan CAPTOR
        // Karena parameter addResourceLocations adalah varargs (String...), kita capture sebagai String[]
        ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);
        
        // Verifikasi dipanggil minimal 2 kali & tangkap argumennya
        verify(registration, atLeast(2)).addResourceLocations(captor.capture());

        List<String[]> allCapturedArguments = captor.getAllValues();

        // 5. ASSERT ISI ARGUMEN
        // Kita cek apakah dari semua pemanggilan, ada yang mengandung path file upload dan classpath
        boolean hasUploadPath = false;
        boolean hasStaticPath = false;

        for (String[] args : allCapturedArguments) {
            // Varargs biasanya dikirim sebagai elemen pertama array atau array itu sendiri
            if (args != null && args.length > 0) {
                String location = args[0]; // Ambil string pertama
                if (location.startsWith("file:/") && location.endsWith("/")) {
                    hasUploadPath = true;
                }
                if (location.equals("classpath:/static/")) {
                    hasStaticPath = true;
                }
            }
        }

        assertTrue(hasUploadPath, "Harus mendaftarkan lokasi folder uploads (diawali file:/)");
        assertTrue(hasStaticPath, "Harus mendaftarkan lokasi static assets (classpath:/static/)");
    }
}