package com.hospital.security;

import com.hospital.service.AuditLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final String LOGIN_SUCCESS_DESCRIPTION =
            "Ng\u01b0\u1eddi d\u00f9ng \u0111\u0103ng nh\u1eadp th\u00e0nh c\u00f4ng v\u00e0o h\u1ec7 th\u1ed1ng.";

    private final AuditLogService auditLogService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        auditLogService.log(authentication.getName(), "LOGIN_SUCCESS", "USER", authentication.getName(),
                LOGIN_SUCCESS_DESCRIPTION);
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            response.sendRedirect("/dashboard");
            return;
        }
        response.sendRedirect("/dashboard?mode=staff");
    }
}
