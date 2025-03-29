package com.perini.anss;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@SpringBootApplication
public class AnssApplication {

    private static final Logger logger = LoggerFactory.getLogger(AnssApplication.class);
    private static final String URL_DOC = "https://www.gov.br/ans/pt-br/acesso-a-informacao/participacao-da-sociedade/atualizacao-do-rol-de-procedimentos";
    private static final String DOWNLOAD_DIR = "downloads";
    private static final String ZIP_NAME = "anexos_anss.zip";
    private static final int MAX_RETRIES = 3;
    private static final int TIMEOUT_MS = 15000;
    private static final int THREAD_POOL_SIZE = 4;

    public static void main(String[] args) {
        SpringApplication.run(AnssApplication.class, args);
        try {
            new AnssApplication().process();
        } catch (Exception e) {
            logger.error("Erro fatal durante a execução", e);
        }
    }

    public void process() throws Exception {
        // 1. Criar diretório de download
        createDownloadDirectory();
        
        // 2. Obter e processar os links
        List<String> fileUrls = getFileUrls();
        
        // 3. Download paralelo
        downloadFilesParallel(fileUrls);
        
        // 4. Compactação
        createZipFile();
        
        logger.info("Processo concluído com sucesso");
    }

    private void createDownloadDirectory() throws IOException {
        Path downloadPath = Paths.get(DOWNLOAD_DIR);
        if (!Files.exists(downloadPath)) {
            Files.createDirectories(downloadPath);
            logger.info("Diretório de downloads criado: {}", downloadPath);
        }
    }

    private List<String> getFileUrls() throws IOException {
        List<String> fileUrls = new ArrayList<>();
        
        logger.info("Conectando-se à página: {}", URL_DOC);
        Document doc = Jsoup.connect(URL_DOC)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .timeout(TIMEOUT_MS)
                .get();

        Elements links = doc.select("a[href]");
        logger.info("Encontrados {} links na página", links.size());

        for (Element link : links) {
            String href = link.attr("href");
            if (isTargetFile(href)) {
                String fileUrl = href.startsWith("http") ? href : "https://www.gov.br" + href;
                fileUrls.add(fileUrl);
                logger.debug("Link válido encontrado: {}", fileUrl);
            }
        }
        
        return fileUrls;
    }

    private boolean isTargetFile(String href) {
        return href.matches(".*(Anexo_I|Anexo_II).*\\.(pdf|xlsx?|docx?)");
    }

    private void downloadFilesParallel(List<String> fileUrls) {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (String fileUrl : fileUrls) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
                    try {
                        downloadFileWithRetry(fileUrl);
                        break;
                    } catch (IOException e) {
                        logger.warn("Tentativa {} falhou para {}: {}", attempt, fileUrl, e.getMessage());
                        if (attempt == MAX_RETRIES) {
                            logger.error("Falha ao baixar após {} tentativas: {}", MAX_RETRIES, fileUrl);
                        }
                    }
                }
            }, executor);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();
    }

    private void downloadFileWithRetry(String fileUrl) throws IOException {
        String fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
        Path filePath = Paths.get(DOWNLOAD_DIR, fileName);
        
        logger.info("Iniciando download: {}", fileUrl);
        URL url = new URL(fileUrl);
        
        try (InputStream in = url.openStream()) {
            Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Download concluído: {}", filePath);
        }
    }

    private void createZipFile() throws IOException {
        logger.info("Criando arquivo ZIP: {}", ZIP_NAME);
        
        try (FileOutputStream fos = new FileOutputStream(ZIP_NAME);
             ZipOutputStream zipOut = new ZipOutputStream(fos)) {
            
            Files.list(Paths.get(DOWNLOAD_DIR))
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    try {
                        addToZipFile(file, zipOut);
                    } catch (IOException e) {
                        logger.error("Erro ao adicionar arquivo ao ZIP: {}", file, e);
                    }
                });
        }
        
        logger.info("Arquivo ZIP criado com sucesso: {}", ZIP_NAME);
    }

    private void addToZipFile(Path file, ZipOutputStream zipOut) throws IOException {
        try (InputStream fis = Files.newInputStream(file)) {
            ZipEntry zipEntry = new ZipEntry(file.getFileName().toString());
            zipOut.putNextEntry(zipEntry);
            
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            
            zipOut.closeEntry();
        }
    }
}