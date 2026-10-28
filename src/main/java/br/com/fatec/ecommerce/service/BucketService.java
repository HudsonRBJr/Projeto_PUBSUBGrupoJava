package br.com.fatec.ecommerce.service;

import br.com.fatec.ecommerce.dto.ArquivoDTO;
import br.com.fatec.ecommerce.dto.ObjetoBucketDTO;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class BucketService {

    private final Storage storage;
    private final String nomeBucket;
    private final boolean aclPublica;

    public BucketService(Storage storage,
                         @Value("${app.bucket.nome}") String nomeBucket,
                         @Value("${app.bucket.acl-publica:false}") boolean aclPublica) {
        this.storage = storage;
        this.nomeBucket = nomeBucket;
        this.aclPublica = aclPublica;
    }

    public String getNomeBucket() {
        return nomeBucket;
    }

    public String urlPublica(String nomeObjeto) {
        return "https://storage.googleapis.com/" + nomeBucket + "/" + nomeObjeto;
    }

    public void enviar(String nomeObjeto, byte[] conteudo, String contentType) {
        BlobInfo info = BlobInfo.newBuilder(BlobId.of(nomeBucket, nomeObjeto))
                .setContentType(contentType)
                .setCacheControl("public, max-age=3600")
                .build();
        if (aclPublica) {
            storage.create(info, conteudo, Storage.BlobTargetOption.predefinedAcl(Storage.PredefinedAcl.PUBLIC_READ));
        } else {
            storage.create(info, conteudo);
        }
    }

    public List<ObjetoBucketDTO> listar(String prefixo) {
        List<ObjetoBucketDTO> objetos = new ArrayList<>();
        Iterable<Blob> blobs = (prefixo == null || prefixo.isBlank())
                ? storage.list(nomeBucket).iterateAll()
                : storage.list(nomeBucket, Storage.BlobListOption.prefix(prefixo)).iterateAll();
        for (Blob blob : blobs) {
            objetos.add(new ObjetoBucketDTO(blob.getName(), blob.getSize(), blob.getContentType(),
                    blob.getUpdateTimeOffsetDateTime(), urlPublica(blob.getName())));
        }
        return objetos;
    }

    public ArquivoDTO baixar(String nomeObjeto) {
        Blob blob = storage.get(BlobId.of(nomeBucket, nomeObjeto));
        if (blob == null) {
            throw new ResponseStatusException(NOT_FOUND, "Objeto não encontrado no bucket: " + nomeObjeto);
        }
        String nome = nomeObjeto.substring(nomeObjeto.lastIndexOf('/') + 1);
        return new ArquivoDTO(nome, blob.getContentType(), blob.getContent());
    }

    public boolean apagar(String nomeObjeto) {
        return storage.delete(BlobId.of(nomeBucket, nomeObjeto));
    }
}
