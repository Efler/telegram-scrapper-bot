package edu.eflerrr.bot.service.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class SecurityService {

    public String getClientIP(HttpServletRequest request) {
        String xForwardedHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedHeader == null) {
            return request.getRemoteAddr();
        }
        return xForwardedHeader.split(",")[0];
    }

}
