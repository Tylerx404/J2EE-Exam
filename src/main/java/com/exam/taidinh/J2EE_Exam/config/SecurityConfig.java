package com.exam.taidinh.J2EE_Exam.config;

import com.exam.taidinh.J2EE_Exam.models.Patient;
import com.exam.taidinh.J2EE_Exam.repositories.PatientRepository;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        GoogleOAuth2UserService googleOAuth2UserService
    ) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/", "/home", "/home/search-fragment", "/courses", "/register", "/login", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/enroll/**").hasRole("PATIENT")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/home", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/home?logout")
                .permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/home", true)
                .userInfoEndpoint(userInfo -> userInfo.userService(googleOAuth2UserService))
            );

        return http.build();
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(
        @Value("${app.oauth.google.client-id:}") String clientId,
        @Value("${app.oauth.google.client-secret:}") String clientSecret
    ) {
        List<ClientRegistration> registrations = new ArrayList<>();
        if (!clientId.isBlank() && !clientSecret.isBlank()) {
            registrations.add(
                CommonOAuth2Provider.GOOGLE.getBuilder("google")
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .scope("openid", "profile", "email")
                    .build()
            );
        }
        return new OptionalClientRegistrationRepository(registrations);
    }

    @Bean
    public UserDetailsService userDetailsService(PatientRepository patientRepository) {
        return username -> {
            Patient patient = patientRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            String[] roles = patient.getRoles().stream()
                .map(role -> role.getName())
                .toArray(String[]::new);

            return User.withUsername(patient.getUsername())
                .password(patient.getPassword())
                .roles(roles)
                .build();
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private static final class OptionalClientRegistrationRepository
        implements ClientRegistrationRepository, Iterable<ClientRegistration> {

        private final List<ClientRegistration> registrations;

        private OptionalClientRegistrationRepository(List<ClientRegistration> registrations) {
            this.registrations = List.copyOf(registrations);
        }

        @Override
        public ClientRegistration findByRegistrationId(String registrationId) {
            return registrations.stream()
                .filter(registration -> registration.getRegistrationId().equals(registrationId))
                .findFirst()
                .orElse(null);
        }

        @Override
        public Iterator<ClientRegistration> iterator() {
            return registrations.iterator();
        }
    }
}
