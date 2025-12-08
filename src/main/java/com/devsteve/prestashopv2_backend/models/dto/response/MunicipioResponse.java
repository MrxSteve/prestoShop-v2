package com.devsteve.prestashopv2_backend.models.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MunicipioResponse {
    private Integer id;
    private String nombre;
}
