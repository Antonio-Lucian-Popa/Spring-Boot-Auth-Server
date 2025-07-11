package com.asusoftware.AuthServer.security;

import com.asusoftware.AuthServer.entity.Role;
import com.asusoftware.AuthServer.entity.User;
import com.asusoftware.AuthServer.repository.RoleRepository;
import com.asusoftware.AuthServer.repository.UserRepository;
import com.asusoftware.AuthServer.service.JwtService;
import com.asusoftware.AuthServer.utils.CookieUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setUsername(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPassword(UUID.randomUUID().toString()); // Set a random password
            user.setEnabled(true);

            Role userRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new RuntimeException("Role USER not found"));

            user.getRoles().add(userRole);

            user = userRepository.save(user);

        }

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Redirect pentru aplicație mobilă (Accept: application/json)
        if (isMobileRequest(request)) {
            String redirectUrl = String.format("myapp://oauth2/callback?access_token=%s&refresh_token=%s", accessToken, refreshToken);
            response.sendRedirect(redirectUrl);
        } else {
            // Web: Setează JWT în cookies și redirecționează
            CookieUtils.addJwtCookies(response, accessToken, refreshToken);
            response.sendRedirect("http://localhost:5173"); // sau pagina dorită
        }
    }

    private boolean isMobileRequest(HttpServletRequest request) {
        String acceptHeader = request.getHeader("Accept");
        return acceptHeader != null && acceptHeader.contains("application/json");
    }
}

