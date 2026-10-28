package br.com.fatec.ecommerce.repository;

import br.com.fatec.ecommerce.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    @Query("select distinct p from Produto p left join fetch p.imagens order by p.nome")
    List<Produto> findAllComImagens();

    @Query("select p from Produto p left join fetch p.imagens where p.id = :id")
    Optional<Produto> findByIdComImagens(Long id);
}
