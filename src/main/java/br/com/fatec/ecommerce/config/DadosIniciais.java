package br.com.fatec.ecommerce.config;

import br.com.fatec.ecommerce.dto.ProdutoForm;
import br.com.fatec.ecommerce.model.Produto;
import br.com.fatec.ecommerce.service.ImagemProdutoService;
import br.com.fatec.ecommerce.service.ProdutoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.seed.ativo", havingValue = "true")
public class DadosIniciais implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DadosIniciais.class);
    private static final int IMAGENS_POR_PRODUTO = 3;

    private record ProdutoExemplo(String nome, String descricao, String preco) {
    }

    private static final List<ProdutoExemplo> PRODUTOS = List.of(
            new ProdutoExemplo("Fone de Ouvido Bluetooth", "Fone sem fio com cancelamento de ruído e bateria de 30 horas.", "349.90"),
            new ProdutoExemplo("Teclado Mecânico RGB", "Teclado ABNT2 com switches blue e iluminação RGB personalizável.", "279.00"),
            new ProdutoExemplo("Mouse Gamer 16000 DPI", "Mouse com sensor óptico de alta precisão e 7 botões programáveis.", "189.90"),
            new ProdutoExemplo("Monitor 27\" 4K", "Monitor IPS 27 polegadas, resolução 4K e HDR10.", "2199.00"),
            new ProdutoExemplo("Cadeira Ergonômica", "Cadeira com apoio lombar ajustável e braços 4D.", "1499.90"),
            new ProdutoExemplo("Smartwatch Fit Pro", "Relógio inteligente com GPS, monitor cardíaco e resistência à água.", "899.00"),
            new ProdutoExemplo("Caixa de Som Portátil", "Caixa de som à prova d'água com 20W de potência.", "259.90"),
            new ProdutoExemplo("Webcam Full HD", "Webcam 1080p com microfone embutido e foco automático.", "229.00"),
            new ProdutoExemplo("SSD NVMe 1TB", "SSD M.2 NVMe com leitura de até 3500 MB/s.", "459.90"),
            new ProdutoExemplo("Mochila para Notebook", "Mochila impermeável com compartimento acolchoado para até 15,6\".", "199.90")
    );

    private final ProdutoService produtoService;
    private final ImagemProdutoService imagemProdutoService;

    public DadosIniciais(ProdutoService produtoService, ImagemProdutoService imagemProdutoService) {
        this.produtoService = produtoService;
        this.imagemProdutoService = imagemProdutoService;
    }

    @Override
    public void run(String... args) throws IOException {
        if (produtoService.contar() > 0) {
            log.info("Carga inicial ignorada: já existem produtos cadastrados.");
            return;
        }
        for (int p = 0; p < PRODUTOS.size(); p++) {
            ProdutoExemplo exemplo = PRODUTOS.get(p);
            ProdutoForm form = new ProdutoForm();
            form.setNome(exemplo.nome());
            form.setDescricao(exemplo.descricao());
            form.setPreco(new BigDecimal(exemplo.preco()));
            Produto produto = produtoService.criar(form);

            float matiz = (float) p / PRODUTOS.size();
            for (int i = 1; i <= IMAGENS_POR_PRODUTO; i++) {
                byte[] png = gerarImagem(exemplo.nome(), i, matiz);
                imagemProdutoService.adicionarImagem(produto.getId(), png, "foto-" + i + ".png", "image/png", i == 1);
            }
            log.info("Produto '{}' criado com {} imagens.", exemplo.nome(), IMAGENS_POR_PRODUTO);
        }
    }

    private byte[] gerarImagem(String texto, int numero, float matiz) throws IOException {
        int largura = 800;
        int altura = 600;
        BufferedImage imagem = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = imagem.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Color inicio = Color.getHSBColor(matiz, 0.55f, 0.95f - numero * 0.12f);
        Color fim = Color.getHSBColor((matiz + 0.12f) % 1f, 0.75f, 0.55f);
        g.setPaint(new GradientPaint(0, 0, inicio, largura, altura, fim));
        g.fillRect(0, 0, largura, altura);

        g.setColor(new Color(255, 255, 255, 40));
        g.fillOval(largura - 300 - numero * 40, -120, 420, 420);

        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 46));
        centralizar(g, texto, largura, altura / 2);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 28));
        centralizar(g, "Foto " + numero + " de " + IMAGENS_POR_PRODUTO, largura, altura / 2 + 60);
        g.dispose();

        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        ImageIO.write(imagem, "png", saida);
        return saida.toByteArray();
    }

    private void centralizar(Graphics2D g, String texto, int largura, int y) {
        FontMetrics metricas = g.getFontMetrics();
        g.drawString(texto, (largura - metricas.stringWidth(texto)) / 2, y);
    }
}
