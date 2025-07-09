package tn.esprit.userservice.service;


import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import tn.esprit.userservice.entity.User;
import tn.esprit.userservice.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .or(() -> userRepository.findByUsername(email)) // 👈 login par username aussi
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email or username: " + email));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail()) // on utilise l’email comme identifiant
                .password(user.getPassword())
                .roles(user.getRole()) // "USER"
                .build();
    }
}
