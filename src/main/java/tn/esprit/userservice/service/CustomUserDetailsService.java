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
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found"));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername()) // ✅ identifiant = username
                .password(user.getPassword())
                .roles(user.getRole())
                .build();
    }


}
