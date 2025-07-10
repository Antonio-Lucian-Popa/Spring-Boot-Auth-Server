package com.asusoftware.AuthServer.security;

import com.asusoftware.AuthServer.entity.Role;
import com.asusoftware.AuthServer.entity.User;
import com.asusoftware.AuthServer.repository.RoleRepository;
import com.asusoftware.AuthServer.repository.UserRepository;
import com.asusoftware.AuthServer.service.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setId(UUID.randomUUID());
            newUser.setEmail(email);
            newUser.setUsername(email);
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);
            newUser.setEnabled(true);

            Role role = roleRepository.findByName("USER")
                    .orElseThrow(() -> new RuntimeException("Role USER not found"));
            newUser.getRoles().add(role);

            return userRepository.save(newUser);
        });

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Redirect to frontend with tokens (you can improve this by using cookies instead)
        String redirectUrl = String.format("%s/oauth2/callback?access_token=%s&refresh_token=%s",
                "http://localhost:5173", accessToken, refreshToken);

        response.sendRedirect(redirectUrl);
    }
}

