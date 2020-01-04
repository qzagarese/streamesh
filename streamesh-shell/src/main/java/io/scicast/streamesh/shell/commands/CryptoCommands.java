package io.scicast.streamesh.shell.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.scicast.streamesh.shell.Constants;
import io.scicast.streamesh.shell.web.RestClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Map;

import static io.scicast.streamesh.shell.Constants.ERROR_STATUS_MSG;
import static io.scicast.streamesh.shell.Constants.GENERIC_ERROR_MSG;

@ShellComponent
public class CryptoCommands {

    private ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @ShellMethod(key = "generate-keypair", value = "Generates a RSA keypair in the file specified as --output. Default key length is 3072 bits.")
    public String generateKeypair(@ShellOption("--output") String file,
                                  @ShellOption(value = "--key-length", defaultValue = "3072") int keyLength,
                                  @ShellOption(value = "--pub-key-file", defaultValue = ShellOption.NULL) String pubKeyFileName) throws IOException {
        File outputFile = new File(file);
        outputFile.createNewFile();
        if (pubKeyFileName == null) {
            pubKeyFileName = outputFile.getName() + ".pub";
        }
        String absolutePath = outputFile.getAbsolutePath();
        File pubKeyOutput = new File(absolutePath.substring(0, absolutePath.lastIndexOf(File.separator)) + File.separator + pubKeyFileName);
        KeyPair kp = ShellCryptoUtil.getRSAKeyPair(keyLength);
        FileOutputStream kpStream = new FileOutputStream(outputFile);
        kpStream.write(kp.getPrivate().getEncoded());
        kpStream.flush();
        kpStream.close();
        System.out.println("Keypair generated: " + outputFile.getAbsolutePath());

        FileOutputStream pubKeyStream = new FileOutputStream(pubKeyOutput);
        pubKeyStream.write(kp.getPublic().getEncoded());
        pubKeyStream.flush();
        pubKeyStream.close();
        System.out.println("Public key generated: " + pubKeyOutput.getAbsolutePath());

        return "";
    }

    @ShellMethod(key = "decrypt", value = "Decrypts an encrypted file that has previously been downloaded using the get-result command. " +
            "It takes a job-id to recover the correct encrypted decrytion key and a private key file to decrypt the encryption key.")
    public void decrypt(@ShellOption("--job-id") String jobId,
                        @ShellOption("--private-key") String privateKey,
                        @ShellOption("--input-file") String inputFile,
                        @ShellOption(value = "--output-file", defaultValue = "output.plain") String outputFile) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        RestClient client = new RestClient(System.getProperty(Constants.SERVER_URL_PROPERTY, Constants.SERVER_URL_DEFAULT))
                .onClientError(ce -> {
                    if (ce.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                        System.err.println("Could not find job with id " + jobId);
                    } else {
                        System.err.println(ERROR_STATUS_MSG + ce.getStatusCode());
                    }
                }).onServerError(se -> System.err.println(ERROR_STATUS_MSG + se.getStatusCode()))
                .onGenericError(e -> System.err.println(GENERIC_ERROR_MSG));

        ResponseEntity<String> json = client.getJson("/jobs/" + jobId);
        Map<?, ?> responseBody = mapper.readerFor(new TypeReference<Map<?, ?>>() {
        }).readValue(json.getBody());

        Map<String, Object> key = (Map<String, Object>) responseBody.get("key");
        File privateKeyFile = new File(privateKey);

        File input = new File(inputFile);
        File output = new File(outputFile);
        byte[] wrappedEncryptionKeyBytes = Base64.getDecoder().decode((String) key.get("wrappedEncryptionKey"));
        byte[] ivBytes = Base64.getDecoder().decode((String) key.get("iv"));

        byte[] decryptedKey = ShellCryptoUtil.rsaDecrypt(privateKeyFile, wrappedEncryptionKeyBytes);

        ShellCryptoUtil.decryptFile(input, output, decryptedKey, ivBytes);

    }

}
