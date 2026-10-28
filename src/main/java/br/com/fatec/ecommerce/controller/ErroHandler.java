package br.com.fatec.ecommerce.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URI;

@ControllerAdvice
public class ErroHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String tamanhoExcedido(HttpServletRequest request, RedirectAttributes redirect) {
        redirect.addFlashAttribute("erro", "Arquivo muito grande: limite de 10 MB por imagem e 60 MB por envio.");
        String origem = request.getHeader("Referer");
        String caminho = origem != null ? URI.create(origem).getPath() : null;
        return "redirect:" + (caminho != null && !caminho.isBlank() ? caminho : "/produtos");
    }
}
