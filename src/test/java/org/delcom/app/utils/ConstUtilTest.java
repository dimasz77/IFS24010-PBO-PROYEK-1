package org.delcom.app.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConstUtilTest {

    @Test
    void testConstructor() {
        // Test ini penting untuk menutupi baris "public class ConstUtil"
        // Java secara implisit membuat default constructor, kita harus memanggilnya
        // agar coverage tools (seperti Jacoco/EclEmma) menganggap class ini ter-cover 100%
        ConstUtil constUtil = new ConstUtil();
        assertNotNull(constUtil);
    }

    @Test
    void testConstantValues() {
        // Verifikasi nilai konstanta (sekaligus mengakses field static agar terhitung covered)
        assertEquals("AUTH_TOKEN", ConstUtil.KEY_AUTH_TOKEN);
        assertEquals("USER_ID", ConstUtil.KEY_USER_ID);

        assertEquals("pages/auth/login", ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN);
        assertEquals("pages/auth/register", ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER);
        assertEquals("pages/home", ConstUtil.TEMPLATE_PAGES_HOME);

        assertEquals("pages/todos/home", ConstUtil.TEMPLATE_PAGES_TODOS_HOME);
        assertEquals("pages/cashflows/home", ConstUtil.TEMPLATE_PAGES_CASHFLOWS_HOME);
        assertEquals("pages/todos/detail", ConstUtil.TEMPLATE_PAGES_TODOS_DETAIL);
    }
}