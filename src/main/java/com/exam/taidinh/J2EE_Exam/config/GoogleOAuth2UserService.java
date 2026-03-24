package com.exam.taidinh.J2EE_Exam.config;

import com.exam.taidinh.J2EE_Exam.models.Patient;
import com.exam.taidinh.J2EE_Exam.models.Role;
import com.exam.taidinh.J2EE_Exam.repositories.PatientRepository;
import com.exam.taidinh.J2EE_Exam.repositories.RoleRepository;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class GoogleOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final PatientRepository patientRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public GoogleOAuth2UserService(
        PatientRepository patientRepository,
        RoleRepository roleRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.patientRepository = patientRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = delegate.loadUser(userRequest);
        String email = oauth2User.getAttribute("email");
        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException("Google account does not provide an email");
        }

        Patient patient = patientRepository.findByEmail(email)
            .orElseGet(() -> createPatientFromGoogle(email));

        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        patient.getRoles().forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName())));
        authorities.addAll(oauth2User.getAuthorities());

        String userNameAttributeName = userRequest.getClientRegistration()
            .getProviderDetails()
            .getUserInfoEndpoint()
            .getUserNameAttributeName();

        if (userNameAttributeName == null || userNameAttributeName.isBlank()) {
            userNameAttributeName = "sub";
        }

        return new DefaultOAuth2User(authorities, oauth2User.getAttributes(), userNameAttributeName);
    }

    private Patient createPatientFromGoogle(String email) {
        Role patientRole = roleRepository.findByName("PATIENT")
            .orElseGet(() -> roleRepository.save(new Role("PATIENT")));

        Patient patient = new Patient(
            buildUniqueUsername(email),
            passwordEncoder.encode(UUID.randomUUID().toString()),
            email
        );
        patient.addRole(patientRole);
        return patientRepository.save(patient);
    }

    private String buildUniqueUsername(String email) {
        String base = email.substring(0, email.indexOf('@'))
            .replaceAll("[^a-zA-Z0-9._-]", "")
            .toLowerCase();
        if (base.isBlank()) {
            base = "googleuser";
        }
        if (base.length() > 40) {
            base = base.substring(0, 40);
        }

        String candidate = base;
        int suffix = 1;
        while (patientRepository.existsByUsername(candidate)) {
            candidate = base + suffix;
            if (candidate.length() > 60) {
                candidate = candidate.substring(0, 60);
            }
            suffix++;
        }
        return candidate;
    }
}
