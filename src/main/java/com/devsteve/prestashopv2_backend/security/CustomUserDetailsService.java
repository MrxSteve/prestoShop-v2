package com.devsteve.prestashopv2_backend.security;

import com.devsteve.prestashopv2_backend.models.entities.UsuarioEntity;
import com.devsteve.prestashopv2_backend.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UsuarioEntity usuario = usuarioRepository.findByEmailWithRolesAndTiendas(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        if (!usuario.getActivo()) {
            throw new UsernameNotFoundException("Usuario inactivo: " + email);
        }

        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPassword())
                .authorities(mapRolesToAuthorities(usuario))
                .accountLocked(false)
                .disabled(false)
                .build();
    }

    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(UsuarioEntity usuario) {
        return usuario.getRoles().stream()
                .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol.getNombre().toUpperCase()))
                .collect(Collectors.toList());
    }
}
