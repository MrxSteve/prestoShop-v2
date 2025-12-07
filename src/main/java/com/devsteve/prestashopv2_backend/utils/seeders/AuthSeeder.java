package com.devsteve.prestashopv2_backend.utils.seeders;

import com.devsteve.prestashopv2_backend.models.entities.RolEntity;
import com.devsteve.prestashopv2_backend.models.entities.UsuarioEntity;
import com.devsteve.prestashopv2_backend.repositories.RolRepository;
import com.devsteve.prestashopv2_backend.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1) // Se ejecuta primero
public class AuthSeeder implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("🔐 Iniciando seeder de autenticación...");

        // Crear roles básicos
        crearRoles();

        // Crear usuario SYSADMIN
        crearUsuarioAdmin();

        log.info("✅ Seeder de autenticación completado");
    }

    private void crearRoles() {
        String[] roles = {"SYSADMIN", "ENCARGADO", "EMPLEADO", "CLIENTE"};

        for (String nombreRol : roles) {
            if (!rolRepository.findByNombre(nombreRol).isPresent()) {
                RolEntity rol = RolEntity.builder()
                    .nombre(nombreRol)
                    .usuarios(new HashSet<>())
                    .build();
                rolRepository.save(rol);
                log.info("✅ Rol creado: {}", nombreRol);
            } else {
                log.info("ℹ️  Rol ya existe: {}", nombreRol);
            }
        }
    }

    private void crearUsuarioAdmin() {
        if (!usuarioRepository.findByEmail(adminEmail).isPresent()) {
            // Obtener el rol SYSADMIN desde la base de datos (entidad managed)
            RolEntity rolAdmin = rolRepository.findByNombre("SYSADMIN")
                .orElseThrow(() -> new RuntimeException("Rol SYSADMIN no encontrado"));

            // Crear usuario administrador
            UsuarioEntity admin = UsuarioEntity.builder()
                .nombreCompleto("Administrador del Sistema")
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .telefono("503-0000-0000")
                .activo(true)
                .empleadoTiendas(new HashSet<>())
                .cuentasCliente(new HashSet<>())
                .movimientosComoOperador(new HashSet<>())
                .movimientosComoCliente(new HashSet<>())
                .notificaciones(new HashSet<>())
                .build();

            // Guardar el usuario primero
            admin = usuarioRepository.save(admin);

            // Luego agregar el rol usando la entidad managed
            admin.getRoles().add(rolAdmin);

            // Guardar de nuevo para persistir la relación
            usuarioRepository.save(admin);

            log.info("✅ Usuario SYSADMIN creado: {}", adminEmail);
            log.info("🔑 Password: {}", adminPassword);

        } else {
            log.info("ℹ️  Usuario SYSADMIN ya existe: {}", adminEmail);
        }
    }
}
