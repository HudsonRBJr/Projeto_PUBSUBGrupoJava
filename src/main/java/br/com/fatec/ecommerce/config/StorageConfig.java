package br.com.fatec.ecommerce.config;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class StorageConfig {

    @Bean
    public Storage storage(@Value("${app.gcp.credenciais:}") String caminhoCredenciais) throws IOException {
        if (caminhoCredenciais == null || caminhoCredenciais.isBlank()) {
            return StorageOptions.getDefaultInstance().getService();
        }
        try (InputStream entrada = new FileInputStream(caminhoCredenciais)) {
            ServiceAccountCredentials credenciais = ServiceAccountCredentials.fromStream(entrada);
            return StorageOptions.newBuilder()
                    .setCredentials(credenciais)
                    .setProjectId(credenciais.getProjectId())
                    .build()
                    .getService();
        }
    }
}
