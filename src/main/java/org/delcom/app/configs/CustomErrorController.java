package org.delcom.app.configs;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

// --- IMPORT YANG BENAR UNTUK SPRING BOOT 3 ---
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
// ---------------------------------------------

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

@Controller
public class CustomErrorController implements ErrorController {

    private final ErrorAttributes errorAttributes;

    public CustomErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @RequestMapping("/error")
    @ResponseBody
    public ApiResponse<Map<String, Object>> handleError(HttpServletRequest request) {
        WebRequest webRequest = new ServletWebRequest(request);
        
        // Mengambil atribut error
        Map<String, Object> errors = this.errorAttributes.getErrorAttributes(webRequest, 
            org.springframework.boot.web.error.ErrorAttributeOptions.defaults());

        String message = (String) errors.getOrDefault("message", "Terjadi kesalahan pada server");
        String path = (String) errors.getOrDefault("path", "unknown");

        return new ApiResponse<>("fail", message, Map.of(
            "path", path,
            "status", errors.getOrDefault("status", 500),
            "error", errors.getOrDefault("error", "Internal Server Error")
        ));
    }
}