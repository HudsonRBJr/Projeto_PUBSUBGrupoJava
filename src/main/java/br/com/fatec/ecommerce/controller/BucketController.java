package br.com.fatec.ecommerce.controller;

import br.com.fatec.ecommerce.service.BucketService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class BucketController {

    private final BucketService bucketService;

    public BucketController(BucketService bucketService) {
        this.bucketService = bucketService;
    }

    @GetMapping("/bucket")
    public String listar(@RequestParam(value = "prefixo", required = false) String prefixo, Model model) {
        model.addAttribute("nomeBucket", bucketService.getNomeBucket());
        model.addAttribute("prefixo", prefixo);
        try {
            model.addAttribute("objetos", bucketService.listar(prefixo));
        } catch (RuntimeException e) {
            model.addAttribute("objetos", List.of());
            model.addAttribute("erro", "Não foi possível listar o bucket: " + e.getMessage());
        }
        return "bucket/lista";
    }

    @GetMapping("/bucket/download")
    public ResponseEntity<byte[]> baixar(@RequestParam("objeto") String nomeObjeto) {
        return Respostas.download(bucketService.baixar(nomeObjeto));
    }
}
