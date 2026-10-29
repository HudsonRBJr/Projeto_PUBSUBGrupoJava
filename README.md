# Loja Grupo H — Catálogo com imagens no Google Cloud Storage
ALUNOS: Hudson Ribeiro Barbara Junior, Maria Clara Costa Cardoso, Gustavo Schizari, Eduardo Gibertoni Camillo

Aplicação Spring Boot (Java 21) do tema **ecommerce**:

- dados dos produtos e das imagens (nome do objeto, URL pública, flag de principal, data) no **PostgreSQL local**;
- arquivos de imagem no bucket **`fatec-2026-grupo-h`**, usando a chave da Service Account.

## Funcionalidades

| Requisito | Onde |
|---|---|
| Cadastro do produto (nome, descrição, preço) | `/produtos/novo` |
| Upload de n imagens e marcação da principal | formulário de cadastro e página de detalhe |
| Catálogo com miniatura da imagem principal | `/produtos` |
| Detalhe com imagem em destaque + galeria | `/produtos/{id}` |
| Apagar imagens (bucket + banco) | botão "Apagar" na galeria |
| Listagem e download de objetos do bucket pela aplicação | `/bucket` |

## Pré-requisitos

- JDK 21 e Maven 3.9+
- PostgreSQL rodando em `localhost:5432`
- Chave JSON da Service Account do grupo

## Configuração

1. Crie o banco:

   ```sql
   CREATE DATABASE ecommerce;
   ```

2. Copie a chave da SA para `credenciais/chave-sa.json` (a pasta `credenciais/` está no `.gitignore` — nunca suba a chave).

3. Variáveis de ambiente (todas têm valor padrão, exceto a chave):

   | Variável | Padrão |
   |---|---|
   | `GOOGLE_APPLICATION_CREDENTIALS` | — (caminho da chave) |
   | `DB_URL` | `jdbc:postgresql://localhost:5432/ecommerce` |
   | `DB_USUARIO` | `postgres` |
   | `DB_SENHA` | `postgres` |
   | `GCP_BUCKET` | `fatec-2026-grupo-h` |

As tabelas `produto` e `imagem_produto` são criadas automaticamente pelo Hibernate.

## Executando

### VS Code

Instale as extensões recomendadas (Extension Pack for Java e Spring Boot Extension Pack). O arquivo `.vscode/launch.json` já define as variáveis de ambiente — ajuste `DB_SENHA` se necessário e rode pela aba *Run and Debug*:

- **EcommerceApplication** — execução normal;
- **EcommerceApplication (popular dados)** — cria 10 produtos com 3 imagens cada (só se o banco estiver vazio).

### Terminal (PowerShell)

```powershell
$env:GOOGLE_APPLICATION_CREDENTIALS = "$PWD\credenciais\chave-sa.json"
$env:DB_SENHA = "sua-senha"
mvn spring-boot:run
```

Para popular os dados de demonstração:

```powershell
mvn spring-boot:run "-Dspring-boot.run.arguments=--app.seed.ativo=true"
```

Acesse <http://localhost:8080>.

## Leitura pública

As imagens são exibidas pelas URLs `https://storage.googleapis.com/fatec-2026-grupo-h/produtos/{id}/...`, que devem abrir sem autenticação.
Se o bucket usar ACL por objeto (sem *uniform bucket-level access*), defina `app.bucket.acl-publica=true` em `application.properties` para que cada upload seja enviado como `publicRead`.

## Estrutura

```
src/main/java/br/com/fatec/ecommerce
├── config        StorageConfig, DadosIniciais
├── controller    ProdutoController, ImagemProdutoController, BucketController
├── dto           ProdutoForm, ObjetoBucketDTO, ArquivoDTO
├── model         Produto, ImagemProduto
├── repository    ProdutoRepository, ImagemProdutoRepository
└── service       ProdutoService, ImagemProdutoService, BucketService
```
