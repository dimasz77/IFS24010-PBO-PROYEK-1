package org.delcom.app.utils;

import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    // --- TEST 1: Constructor (Coverage Baris 10) ---
    @Test
    void testConstructor() {
        // Class Utility biasanya method-nya static semua.
        // Namun, coverage tool menganggap class declaration belum ter-cover 
        // jika constructor default tidak dipanggil.
        assertNotNull(new JwtUtil());
    }

    // --- TEST 2: Get Key (Coverage Baris 17-18) ---
    @Test
    void testGetKey() {
        assertNotNull(JwtUtil.getKey(), "Secret key tidak boleh null");
    }

    // --- TEST 3: Generate & Extract Token (Happy Path) ---
    // Coverage: Baris 21-27 dan 30-38
    @Test
    void testGenerateAndExtractToken_Success() {
        UUID userId = UUID.randomUUID();
        
        // 1. Generate Token
        String token = JwtUtil.generateToken(userId);
        assertNotNull(token);

        // 2. Extract User ID
        UUID extractedId = JwtUtil.extractUserId(token);
        assertEquals(userId, extractedId);
    }

    // --- TEST 4: Extract Token - Invalid/Malformed ---
    // Coverage: Baris 39-40 (Catch Exception -> return null)
    @Test
    void testExtractUserId_InvalidToken() {
        String invalidToken = "ini.bukan.token.valid";
        
        // Parsing gagal, masuk catch, return null
        UUID result = JwtUtil.extractUserId(invalidToken);
        assertNull(result);
    }

    // --- TEST 5: Validate Token - Valid (Happy Path) ---
    // Coverage: Baris 50-56 (return true)
    @Test
    void testValidateToken_Success() {
        UUID userId = UUID.randomUUID();
        String token = JwtUtil.generateToken(userId);

        boolean isValid = JwtUtil.validateToken(token, false);
        assertTrue(isValid);
    }

    // --- TEST 6: Validate Token - Tampered/Rusak ---
    // Coverage: Baris 62-63 (Catch general Exception -> return false)
    @Test
    void testValidateToken_InvalidSignature() {
        UUID userId = UUID.randomUUID();
        String token = JwtUtil.generateToken(userId);
        
        // Kita rusak tokennya dengan mengubah karakter terakhir
        String tamperedToken = token.substring(0, token.length() - 5) + "palsu";

        boolean isValid = JwtUtil.validateToken(tamperedToken, false);
        assertFalse(isValid);
    }

    // --- TEST 7: Validate Token - Expired & ignoreExpired = FALSE ---
    // Coverage: Baris 57, 58, 61 (Catch Expired -> if false -> return false)
    @Test
    void testValidateToken_Expired_DoNotIgnore() {
        UUID userId = UUID.randomUUID();

        // Kita buat token expired secara MANUAL
        // Gunakan key dari JwtUtil agar signature valid, tapi waktunya expired
        String expiredToken = Jwts.builder()
                .subject(userId.toString())
                .issuedAt(new Date(System.currentTimeMillis() - 100000)) // Dibuat 100 detik lalu
                .expiration(new Date(System.currentTimeMillis() - 1000)) // Expired 1 detik lalu
                .signWith(JwtUtil.getKey())
                .compact();

        // ignoreExpired = false, berarti token expired dianggap TIDAK valid
        boolean isValid = JwtUtil.validateToken(expiredToken, false);
        assertFalse(isValid);
    }

    // --- TEST 8: Validate Token - Expired & ignoreExpired = TRUE ---
    // Coverage: Baris 57, 58, 59 (Catch Expired -> if true -> return true)
    @Test
    void testValidateToken_Expired_Ignore() {
        UUID userId = UUID.randomUUID();

        // Buat token expired manual
        String expiredToken = Jwts.builder()
                .subject(userId.toString())
                .issuedAt(new Date(System.currentTimeMillis() - 100000))
                .expiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(JwtUtil.getKey())
                .compact();

        // ignoreExpired = true, berarti token expired dianggap VALID
        boolean isValid = JwtUtil.validateToken(expiredToken, true);
        assertTrue(isValid);
    }
}