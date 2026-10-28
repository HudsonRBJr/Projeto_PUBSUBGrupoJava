package br.com.fatec.ecommerce.controller;

import br.com.fatec.ecommerce.dto.ProdutoForm;
import br.com.fatec.ecommerce.model.Produto;
import br.com.fatec.ecommerce.service.ImagemProdutoService;
import br.com.fatec.ecommerce.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class ProdutoController {

    private final ProdutoService produtoService;
    private final ImagemProdutoService imagemProdutoService;

    public ProdutoController(ProdutoService produtoService, ImagemProdutoService imagemProdutoService) {
        this.produtoService = produtoService;
        this.imagemProdutoService = imagemProdutoService;
    }

    @GetMapping("/")
    public String inicio() {
        return "redirect:/produtos";
    }

    @GetMapping("/produtos")
    public String listar(Model model) {
        model.addAttribute("produtos", produtoService.listar());
        return "produtos/lista";
    }

    @GetMapping("/produtos/novo")
    public String novo(Model model) {
        model.addAttribute("produtoForm", new ProdutoForm());
        return "produtos/formulario";
    }

    @PostMapping("/produtos")
    public String criar(@Valid @ModelAttribute ProdutoForm produtoForm,
                        BindingResult resultado,
                        @RequestParam(value = "arquivos", required = false) List<MultipartFile> arquivos,
                        @RequestParam(value = "indicePrincipal", required = false) Integer indicePrincipal,
                        RedirectAttributes redirect) {
        if (resultado.hasErrors()) {
            return "produtos/formulario";
        }
        Produto produto = produtoService.criar(produtoForm);
        try {
            int enviadas = imagemProdutoService.adicionarImagens(produto.getId(), arquivos, indicePrincipal);
            redirect.addFlashAttribute("sucesso", "Produto cadastrado com " + enviadas + " imagem(ns).");
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erro", "Produto cadastrado, mas houve falha no envio das imagens: " + e.getMessage());
        }
        return "redirect:/produtos/" + produto.getId();
    }

    @GetMapping("/produtos/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        model.addAttribute("produto", produtoService.buscar(id));
        return "produtos/detalhe";
    }

    @GetMapping("/produtos/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("produtoId", id);
        model.addAttribute("produtoForm", ProdutoForm.de(produtoService.buscar(id)));
        return "produtos/formulario";
    }

    @PostMapping("/produtos/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute ProdutoForm produtoForm,
                            BindingResult resultado,
                            Model model,
                            RedirectAttributes redirect) {
        if (resultado.hasErrors()) {
            model.addAttribute("produtoId", id);
            return "produtos/formulario";
        }
        produtoService.atualizar(id, produtoForm);
        redirect.addFlashAttribute("sucesso", "Produto atualizado.");
        return "redirect:/produtos/" + id;
    }

    @PostMapping("/produtos/{id}/apagar")
    public String apagar(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            produtoService.apagar(id);
            redirect.addFlashAttribute("sucesso", "Produto e imagens apagados.");
            return "redirect:/produtos";
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erro", "Não foi possível apagar o produto: " + e.getMessage());
            return "redirect:/produtos/" + id;
        }
    }
}
