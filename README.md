# Web Scraper para Anexos da Agência Nacional de Saúde Suplementar

## 📖 Sumário

- [Visão Geral](#-visão-geral)
  - [Objetivo](#objetivo)
  - [Tecnologias Utilizadas](#tecnologias-utilizadas)
  - [Principais Recursos](#principais-recursos)
- [Como Executar](#-como-executar)
- [Configurações](#️-configurações)
- [Saída Esperada](#-saída-esperada)
- [Testes](#-testes)

## 🔎 Visão Geral

### Objetivo
Automatizar o download e compactação dos anexos do portal da ANS (Agência Nacional de Saúde Suplementar), facilitando o acesso a esses documentos e para posterior manipulação/análise.

### Tecnologias Utilizadas


| Tecnologia | Finalidade |
|------------|------------|
| Java 17 | Linguagem principal |
| Spring Boot | Framework base |
| JSoup | Biblioteca para extração e parsing de HTML |
| NIO (Java) | Manipulação de arquivos e diretórios |
| CompletableFuture | Paralelização de downloads |
| SLF4J | Logging estruturado |

### Principais Recursos

| Feature              | Benefício                                  |
| -------------------- | ------------------------------------------ |
| Paralelização        | Reduz tempo total de download              |
| Retentativas         | Aumenta taxa de sucesso em redes instáveis |
| Validação de URLs    | Evita downloads inválidos                  |
| Logging estruturado  | Facilita debug em produção                 |
| Timeout configurável | Prevenção contra hung processes            |


## 🛠️ Como Executar

#### 1. Faça um clone [do repositório](https://github.com/anaperinii/web-scraper-ans.git) em sua máquina:

* Crie uma pasta em seu computador para esse programa
* Abra o `git bash` ou `terminal` dentro da respectiva pasta
* Copie a [URL](https://github.com/anaperinii/web-scraper-ans.git) do repositório
* Digite `git clone <URL copiada>` e pressione `enter`

#### 2. Execute o projeto com Maven:

   ```bash
   mvn spring-boot:run
   ```

#### 3. Após a execução, você encontrará:
   - 📂 Arquivos baixados no diretório `downloads/`
   - 📦 Arquivo ZIP compactado (`anexos_anss.zip`) na raiz do projeto

## ⚙️ Configurações

As principais configurações podem ser ajustadas no código-fonte:

```java
private static final String URL_DOC = "[URL_DOS_ANEXOS]";
private static final String DOWNLOAD_DIR = "downloads/";
private static final String ZIP_NAME = "anexos_anss.zip";
```

## 📊 Saída Esperada

```bash
Encontrado: /ans/.../Anexo_I.pdf
Baixado: downloads/Anexo_I.pdf
Encontrado: /ans/.../Anexo_II.xlsx
Baixado: downloads/Anexo_II.xlsx
Arquivo ZIP criado: anexos_anss.zip
```

## 🧪 Testes

Para verificar o funcionamento correto:

- **Teste de Conexão**: confirme que a aplicação consegue acessar a página da ANS
- **Teste de Download**: verifique se os arquivos aparecem no diretório `downloads/`
- **Teste de ZIP**: confira se o arquivo compactado contém os anexos corretos


