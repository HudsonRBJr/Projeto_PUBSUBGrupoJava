package br.com.fatec.ecommerce.service;

import br.com.fatec.ecommerce.dto.ProdutoForm;
import br.com.fatec.ecommerce.model.ImagemProduto;
import br.com.fatec.ecommerce.model.Produto;
import br.com.fatec.ecommerce.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final BucketService bucketService;

    public ProdutoService(ProdutoRepository produtoRepository, BucketService bucketService) {
        this.produtoRepository = produtoRepository;
        this.bucketService = bucketService;
    }

    @Transactional(readOnly = true)
    public List<Produto> listar() {
        return produtoRepository.findAllComImagens();
    }

    @Transactional(readOnly = true)
    public Produto buscar(Long id) {
        return produtoRepository.findByIdComImagens(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Produto não encontrado"));
    }

    @Transactional(readOnly = true)
    public long contar() {
        return produtoRepository.count();
    }

    @Transactional
    public Produto criar(ProdutoForm form) {
        Produto produto = new Produto();
        form.copiarPara(produto);
        return produtoRepository.save(produto);
    }

    @Transactional
    public Produto atualizar(Long id, ProdutoForm form) {
        Produto produto = buscar(id);
        form.copiarPara(produto);
        return produto;
    }

    @Transactional
    public void apagar(Long id) {
        Produto produto = buscar(id);
        for (ImagemProduto imagem : produto.getImagens()) {
            bucketService.apagar(imagem.getNomeObjeto());
        }
        produtoRepository.delete(produto);
    }
}
