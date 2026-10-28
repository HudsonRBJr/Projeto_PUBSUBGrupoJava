package br.com.fatec.ecommerce.dto;

import br.com.fatec.ecommerce.model.Produto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class ProdutoForm {

    @NotBlank(message = "Informe o nome do produto")
    @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres")
    private String nome;

    @Size(max = 2000, message = "A descrição deve ter no máximo 2000 caracteres")
    private String descricao;

    @NotNull(message = "Informe o preço")
    @DecimalMin(value = "0.00", message = "O preço não pode ser negativo")
    @Digits(integer = 10, fraction = 2, message = "Preço inválido")
    private BigDecimal preco;

    public static ProdutoForm de(Produto produto) {
        ProdutoForm form = new ProdutoForm();
        form.setNome(produto.getNome());
        form.setDescricao(produto.getDescricao());
        form.setPreco(produto.getPreco());
        return form;
    }

    public void copiarPara(Produto produto) {
        produto.setNome(nome.trim());
        produto.setDescricao(descricao == null ? null : descricao.trim());
        produto.setPreco(preco);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }
}
