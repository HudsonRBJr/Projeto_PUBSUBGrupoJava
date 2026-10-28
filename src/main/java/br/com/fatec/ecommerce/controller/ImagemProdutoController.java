package br.com.fatec.ecommerce.controller;

import br.com.fatec.ecommerce.dto.ArquivoDTO;
import br.com.fatec.ecommerce.service.ImagemProdutoService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class ImagemProdutoController {

    private final ImagemProdutoService imagemProdutoService;

    public ImagemProdutoController(ImagemProdutoService imagemProdutoService) {
        this.imagemProdutoService = imagemProdutoService;
    }

    @PostMapping("/produtos/{produtoId}/imagens")
    public String enviar(@PathVariable Long produtoId,
                         @RequestParam(value = "arquivos", required = false) List<MultipartFile> arquivos,
                         @RequestParam(value = "indicePrincipal", required = false) Integer indicePrincipal,
                         RedirectAttributes redirect) {
        try {
            int enviadas = imagemProdutoService.adicionarImagens(produtoId, arquivos, indicePrincipal);
            if (enviadas == 0) {
                redirect.addFlashAttribute("erro", "Selecione ao menos uma imagem.");
            } else {
                redirect.addFlashAttribute("sucesso", enviadas + " imagem(ns) enviada(s) para o bucket.");
            }
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erro", "Falha no envio: " + e.getMessage());
        }
        return "redirect:/produtos/" + produtoId;
    }

    @PostMapping("/imagens/{id}/principal")
    public String definirPrincipal(@PathVariable Long id, RedirectAttributes redirect) {
        Long produtoId = imagemProdutoService.definirPrincipal(id);
        redirect.addFlashAttribute("sucesso", "Imagem principal atualizada.");
        return "redirect:/produtos/" + produtoId;
    }

    @PostMapping("/imagens/{id}/apagar")
    public String apagar(@PathVariable Long id, @RequestParam Long produtoId, RedirectAttributes redirect) {
        try {
            imagemProdutoService.apagar(id);
            redirect.addFlashAttribute("sucesso", "Imagem apagada do bucket e do banco.");
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erro", "Não foi possível apagar a imagem: " + e.getMessage());
        }
        return "redirect:/produtos/" + produtoId;
    }

    @GetMapping("/imagens/{id}/download")
    public ResponseEntity<byte[]> baixar(@PathVariable Long id) {
        ArquivoDTO arquivo = imagemProdutoService.baixar(id);
        return Respostas.download(arquivo);
    }
}
