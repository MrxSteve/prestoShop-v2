package com.devsteve.prestashopv2_backend.services;

import com.devsteve.prestashopv2_backend.models.dto.response.MunicipioResponse;
import com.devsteve.prestashopv2_backend.repositories.MunicipioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MunicipioService {
    private final MunicipioRepository municipioRepository;

    public List<MunicipioResponse> listarTodos() {
        List<MunicipioResponse> municipios = municipioRepository.findAll()
                .stream()
                .map(municipio -> MunicipioResponse.builder()
                        .id(municipio.getId())
                        .nombre(municipio.getNombre())
                        .build())
                .toList();
        return municipios;
    }
}
