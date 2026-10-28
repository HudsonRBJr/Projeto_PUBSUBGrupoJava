package br.com.fatec.ecommerce.service;

import br.com.fatec.ecommerce.dto.ArquivoDTO;
import br.com.fatec.ecommerce.model.ImagemProduto;
import br.com.fatec.ecommerce.model.Produto;
import br.com.fatec.ecommerce.repository.ImagemProdutoRepository;
import br.com.fatec.ecommerce.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class ImagemProdutoService {

    private final ImagemProdutoRepository imagemProdutoRepository;
    private final ProdutoRepository produtoRepository;
    private final BucketService bucketService;

    public ImagemProdutoService(ImagemProdutoRepository imagemProdutoRepository,
                                ProdutoRepository produtoRepository,
                                BucketService bucketService) {
        this.imagemProdutoRepository = imagemProdutoRepository;
        this.produtoRepository = produtoRepository;
        this.bucketService = bucketService;
    }

    @Transactional
    public int adicionarImagens(Long produtoId, List<MultipartFile> arquivos, Integer indicePrincipal) {
        Produto produto = buscarProduto(produtoId);
        List<MultipartFile> validos = arquivos == null ? List.of()
                : arquivos.stream().filter(a -> a != null && !a.isEmpty()).toList();
        validos.forEach(this::validarImagem);

        for (int i = 0; i < validos.size(); i++) {
            MultipartFile arquivo = validos.get(i);
            try {
                enviar(produto, arquivo.getBytes(), arquivo.getOriginalFilename(), arquivo.getContentType(),
                        indicePrincipal != null && indicePrincipal == i);
            } catch (IOException e) {
                throw new UncheckedIOException("Falha ao ler o arquivo " + arquivo.getOriginalFilename(), e);
            }
        }
        garantirPrincipal(produto);
        return validos.size();
    }

    @Transactional
    public void adicionarImagem(Long produtoId, byte[] conteudo, String nomeOriginal, String contentType, boolean principal) {
        Produto produto = buscarProduto(produtoId);
        enviar(produto, conteudo, nomeOriginal, contentType, principal);
        garantirPrincipal(produto);
    }

    @Transactional
    public Long definirPrincipal(Long imagemId) {
        ImagemProduto imagem = buscar(imagemId);
        Produto produto = imagem.getProduto();
        produto.getImagens().forEach(i -> i.setPrincipal(i.getId().equals(imagemId)));
        return produto.getId();
    }

    @Transactional
    public Long apagar(Long imagemId) {
        ImagemProduto imagem = buscar(imagemId);
        Produto produto = imagem.getProduto();
        bucketService.apagar(imagem.getNomeObjeto());
        produto.getImagens().remove(imagem);
        garantirPrincipal(produto);
        return produto.getId();
    }

    @Transactional(readOnly = true)
    public ArquivoDTO baixar(Long imagemId) {
        ImagemProduto imagem = buscar(imagemId);
        ArquivoDTO arquivo = bucketService.baixar(imagem.getNomeObjeto());
        String nome = imagem.getNomeArquivoOriginal() != null ? imagem.getNomeArquivoOriginal() : arquivo.nome();
        return new ArquivoDTO(nome, arquivo.contentType(), arquivo.conteudo());
    }

    private void enviar(Produto produto, byte[] conteudo, String nomeOriginal, String contentType, boolean principal) {
        String nomeObjeto = "produtos/" + produto.getId() + "/" + UUID.randomUUID() + "-" + sanitizar(nomeOriginal);
        bucketService.enviar(nomeObjeto, conteudo, contentType);

        if (principal) {
            produto.getImagens().forEach(i -> i.setPrincipal(false));
        }
        ImagemProduto imagem = new ImagemProduto();
        imagem.setProduto(produto);
        imagem.setNomeObjeto(nomeObjeto);
        imagem.setUrlPublica(bucketService.urlPublica(nomeObjeto));
        imagem.setNomeArquivoOriginal(nomeOriginal);
        imagem.setContentType(contentType);
        imagem.setPrincipal(principal);
        produto.getImagens().add(imagem);
        imagemProdutoRepository.save(imagem);
    }

    private void garantirPrincipal(Produto produto) {
        boolean temPrincipal = produto.getImagens().stream().anyMatch(ImagemProduto::isPrincipal);
        if (!temPrincipal) {
            produto.getImagens().stream()
                    .min(Comparator.comparing(ImagemProduto::getDataUpload))
                    .ifPresent(i -> i.setPrincipal(true));
        }
    }

    private void validarImagem(MultipartFile arquivo) {
        String contentType = arquivo.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("O arquivo \"" + arquivo.getOriginalFilename() + "\" não é uma imagem.");
        }
    }

    private String sanitizar(String nomeOriginal) {
        String nome = (nomeOriginal == null || nomeOriginal.isBlank()) ? "imagem" : nomeOriginal;
        nome = nome.substring(Math.max(nome.lastIndexOf('/'), nome.lastIndexOf('\\')) + 1);
        nome = Normalizer.normalize(nome, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return nome.replaceAll("[^A-Za-z0-9._-]", "_").toLowerCase();
    }

    private ImagemProduto buscar(Long imagemId) {
        return imagemProdutoRepository.findById(imagemId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Imagem não encontrada"));
    }

    private Produto buscarProduto(Long produtoId) {
        return produtoRepository.findByIdComImagens(produtoId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Produto não encontrado"));
    }
}
