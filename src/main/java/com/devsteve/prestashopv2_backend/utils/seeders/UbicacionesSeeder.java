package com.devsteve.prestashopv2_backend.utils.seeders;

import com.devsteve.prestashopv2_backend.models.entities.DepartamentoEntity;
import com.devsteve.prestashopv2_backend.models.entities.MunicipioEntity;
import com.devsteve.prestashopv2_backend.repositories.DepartamentoRepository;
import com.devsteve.prestashopv2_backend.repositories.MunicipioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2) // Se ejecuta después del AuthSeeder
public class UbicacionesSeeder implements CommandLineRunner {

    private final DepartamentoRepository departamentoRepository;
    private final MunicipioRepository municipioRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("🗺️  Iniciando seeder de ubicaciones...");

        // Crear departamentos
        crearDepartamentos();

        // Crear municipios
        crearMunicipios();

        log.info("✅ Seeder de ubicaciones completado");
    }

    private void crearDepartamentos() {
        String[] departamentos = {
            "Ahuachapán", "Cabañas", "Chalatenango", "Cuscatlán",
            "La Libertad", "La Paz", "La Unión", "Morazán",
            "Santa Ana", "San Miguel", "San Salvador", "San Vicente",
            "Sonsonate", "Usulután"
        };

        for (String nombreDepartamento : departamentos) {
            if (departamentoRepository.findByNombre(nombreDepartamento).isEmpty()) {
                DepartamentoEntity departamento = DepartamentoEntity.builder()
                    .nombre(nombreDepartamento)
                    .municipios(new HashSet<>())
                    .build();
                departamentoRepository.save(departamento);
                log.info("✅ Departamento creado: {}", nombreDepartamento);
            } else {
                log.info("ℹ️  Departamento ya existe: {}", nombreDepartamento);
            }
        }
    }

    private void crearMunicipios() {
        // Mapa con todos los municipios por departamento
        Map<String, String[]> municipiosPorDepartamento = new HashMap<>();

        // Ahuachapán
        municipiosPorDepartamento.put("Ahuachapán", new String[]{
            "Ahuachapán Norte", "Ahuachapán Centro", "Ahuachapán Sur"
        });

        // Cabañas
        municipiosPorDepartamento.put("Cabañas", new String[]{
            "Cabañas Este", "Cabañas Oeste"
        });

        // Chalatenango
        municipiosPorDepartamento.put("Chalatenango", new String[]{
            "Chalatenango Norte", "Chalatenango Centro", "Chalatenango Sur"
        });

        // Cuscatlán
        municipiosPorDepartamento.put("Cuscatlán", new String[]{
            "Cuscatlán Norte", "Cuscatlán Sur"
        });

        // La Libertad
        municipiosPorDepartamento.put("La Libertad", new String[]{
            "La Libertad Norte", "La Libertad Centro", "La Libertad Oeste",
            "La Libertad Este", "La Libertad Sur", "La Libertad Costa"
        });

        // La Paz
        municipiosPorDepartamento.put("La Paz", new String[]{
            "La Paz Oeste", "La Paz Centro", "La Paz Este"
        });

        // La Unión
        municipiosPorDepartamento.put("La Unión", new String[]{
            "La Unión Norte", "La Unión Sur"
        });

        // Morazán
        municipiosPorDepartamento.put("Morazán", new String[]{
            "Morazán Norte", "Morazán Sur"
        });

        // Santa Ana
        municipiosPorDepartamento.put("Santa Ana", new String[]{
            "Santa Ana Norte", "Santa Ana Centro", "Santa Ana Este", "Santa Ana Oeste"
        });

        // San Miguel
        municipiosPorDepartamento.put("San Miguel", new String[]{
            "San Miguel Norte", "San Miguel Centro", "San Miguel Oeste"
        });

        // San Salvador
        municipiosPorDepartamento.put("San Salvador", new String[]{
            "San Salvador Norte", "San Salvador Oeste", "San Salvador Este",
            "San Salvador Centro", "San Salvador Sur"
        });

        // San Vicente
        municipiosPorDepartamento.put("San Vicente", new String[]{
            "San Vicente Norte", "San Vicente Sur"
        });

        // Sonsonate
        municipiosPorDepartamento.put("Sonsonate", new String[]{
            "Sonsonate Norte", "Sonsonate Centro", "Sonsonate Este", "Sonsonate Oeste"
        });

        // Usulután
        municipiosPorDepartamento.put("Usulután", new String[]{
            "Usulután Norte", "Usulután Este", "Usulután Oeste"
        });

        // Crear municipios para cada departamento
        for (Map.Entry<String, String[]> entry : municipiosPorDepartamento.entrySet()) {
            String nombreDepartamento = entry.getKey();
            String[] municipios = entry.getValue();

            // Buscar el departamento
            DepartamentoEntity departamento = departamentoRepository.findByNombre(nombreDepartamento)
                .orElseThrow(() -> new RuntimeException("Departamento no encontrado: " + nombreDepartamento));

            // Crear municipios
            for (String nombreMunicipio : municipios) {
                if (municipioRepository.findByDepartamentoAndNombre(departamento, nombreMunicipio).isEmpty()) {
                    MunicipioEntity municipio = MunicipioEntity.builder()
                        .departamento(departamento)
                        .nombre(nombreMunicipio)
                        .tiendas(new HashSet<>())
                        .build();
                    municipioRepository.save(municipio);
                    log.info("✅ Municipio creado: {} - {}", nombreDepartamento, nombreMunicipio);
                } else {
                    log.info("ℹ️  Municipio ya existe: {} - {}", nombreDepartamento, nombreMunicipio);
                }
            }
        }
    }
}
