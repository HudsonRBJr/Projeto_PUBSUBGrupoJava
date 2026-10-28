package br.com.fatec.ecommerce.controller;

import br.com.fatec.ecommerce.dto.ArquivoDTO;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;

final class Respostas {

    private Respostas() {
    }

    static ResponseEntity<byte[]> download(ArquivoDTO arquivo) {
        MediaType tipo = arquivo.contentType() != null
                ? MediaType.parseMediaType(arquivo.contentType())
                : MediaType.APPLICATION_OCTET_STREAM;
        ContentDisposition disposicao = ContentDisposition.attachment()
                .filename(arquivo.nome(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(tipo)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposicao.toString())
                .body(arquivo.conteudo());
    }
}
