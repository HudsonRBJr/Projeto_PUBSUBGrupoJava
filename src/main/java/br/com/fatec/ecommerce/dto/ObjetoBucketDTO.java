package br.com.fatec.ecommerce.dto;

import java.time.OffsetDateTime;

public record ObjetoBucketDTO(String nome, Long tamanho, String contentType, OffsetDateTime atualizadoEm, String urlPublica) {
}
