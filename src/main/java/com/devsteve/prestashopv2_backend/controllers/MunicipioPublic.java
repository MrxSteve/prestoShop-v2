package com.devsteve.prestashopv2_backend.controllers;

import com.devsteve.prestashopv2_backend.models.dto.response.MunicipioResponse;
import com.devsteve.prestashopv2_backend.services.MunicipioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/municipios")
@RequiredArgsConstructor
@Tag(name = "Municipios disponibles", description = "Municipios para uso publico")
public class MunicipioPublic {
    private final MunicipioService municipioService;

    @GetMapping
    @Operation(summary = "Listar municipios", description = "Lista de municpios disponibles")
    public ResponseEntity<List<MunicipioResponse>> getAllMunicipios() {
        List<MunicipioResponse> municipios = municipioService.listarTodos();
        return ResponseEntity.ok(municipios);
    }
}
