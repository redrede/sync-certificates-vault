package com.redrede.sync.certificates.vault;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

/**
 * Sync certificates to Vault or Vault to certificates
 *
 * @author marcelo
 */
public class SyncCertificatesVault {

    public static void main(String[] args) {
        try {
            switch (args.length) {
                case 6:
                    System.out.println("Parameters");
                    System.out.println("mode: " + args[0]); //file2Vault | vault2File
                    System.out.println("certificates-path: " + args[1]);
                    System.out.println("certificates-extension: " + args[2]);
                    System.out.println("vault-address " + args[3]);
                    // hide token
                    System.out.println("vault-token: " + args[4].replaceAll("[A-Za-z0-9]", "*"));
                    System.out.println("vault-path: " + args[5]);
                    if (args[0].equalsIgnoreCase("file2Vault")) {
                        syncFiles(args[1], args[2], args[3], args[4], args[5], null);
                    } else if (args[0].equalsIgnoreCase("vault2File")) {
                        syncVault(args[1], args[2], args[3], args[4], args[5]);
                    } else {
                        System.err.println("Error:");
                        System.err.println("Enter mode parameter correctly");
                        System.err.println(" file2Vault -> sync file to vault");
                        System.err.println(" vault2File -> sync vault to file");
                        System.out.println("java -jar sync-certificates-vault.jar <mode> <certificates-path> <certificates-extension> <vault-address> <vault-token>  <vault-path> [<dynamic-path>]");
                        System.out.println("Example: ");
                        System.out.println("java -jar sync-certificates-vault.jar \"file2Vault\" \"/etc/letsencrypt/live\" \"pem\" \"http://127.0.0.1:1234\" \"myroot\" \"secrets/certificates\"  \"/etc/letsencrypt/dynamic\"");
                    }
                    break;
                case 7:
                    System.out.println("Parameters");
                    System.out.println("mode: " + args[0]); //file2Vault | vault2File
                    System.out.println("certificates-path: " + args[1]);
                    System.out.println("certificates-extension: " + args[2]);
                    System.out.println("vault-address " + args[3]);
                    // hide token
                    System.out.println("vault-token: " + args[4].replaceAll("[A-Za-z0-9]", "*"));
                    System.out.println("vault-path: " + args[5]);
                    System.out.println("dynamic-path: " + args[6]);
                    if (args[0].equalsIgnoreCase("file2Vault")) {
                        syncFiles(args[1], args[2], args[3], args[4], args[5], args[6]);
                    } else {
                        System.err.println("Error:");
                        System.err.println("Enter mode parameter correctly");
                        System.err.println(" vault2File does not support \"dynamic-path\"");
                        System.out.println("java -jar sync-certificates-vault.jar <mode> <certificates-path> <certificates-extension> <vault-address> <vault-token>  <vault-path> [<dynamic-path>]");
                        System.out.println("Example: ");
                        System.out.println("java -jar sync-certificates-vault.jar \"file2Vault\" \"/etc/letsencrypt/live\" \"pem\" \"http://127.0.0.1:1234\" \"myroot\" \"secrets/certificates\"  \"/etc/letsencrypt/dynamic\"");
                    }
                    break;
                default:
                    System.err.println("Error:");
                    System.err.println("Enter the parameters");
                    System.out.println("java -jar sync-certificates-vault.jar <mode> <certificates-path> <certificates-extension> <vault-address> <vault-token>  <vault-path> [<dynamic-path>]");
                    System.out.println("Example: ");
                    System.out.println("java -jar sync-certificates-vault.jar \"file2Vault\" \"/etc/letsencrypt/live\" \"pem\" \"http://127.0.0.1:1234\" \"myroot\" \"secrets/certificates\"  \"/etc/letsencrypt/dynamic\"");
                    break;
            }
        } catch (IOException e) {
            System.err.println("X - " + e.getMessage());
        }
    }

    /**
     * Monitors change in certificate folder
     *
     * @param certificatesPath
     * @param certificatesExtension
     * @param vaultAddress
     * @param vaultToken
     * @param vaultPath
     * @param dynamicPath
     * @throws IOException
     */
    public static void syncFiles(String certificatesPath, String certificatesExtension, String vaultAddress, String vaultToken, String vaultPath, String dynamicPath) throws IOException {
        //get the file object

        List<String> extensionsList = Arrays.asList(certificatesExtension.split(","));

        File fromDir = FileUtils.getFile(certificatesPath);

        FileAlterationObserver observer = new FileAlterationObserver(fromDir);

        observer.addListener(new FileAlterationListenerAdaptor() {

            @Override
            public void onDirectoryCreate(File srcFile) {
                System.out.println("X - Folder creation skipped: " + srcFile);
            }

            @Override
            public void onDirectoryDelete(File srcFile) {
                System.out.println("V - Folder delete: " + srcFile);
                sendVault(vaultAddress, vaultToken, vaultPath, srcFile.getName().replace(certificatesPath, "").replace("/", ""), srcFile, dynamicPath, false);
            }

            @Override
            public void onFileCreate(File srcFile) {
                if (extensionsList.contains(FilenameUtils.getExtension(srcFile.getName()))) {
                    System.out.println("V - File create: " + srcFile);
                    sendVault(vaultAddress, vaultToken, vaultPath, srcFile.getParent().replace(certificatesPath, "").replace("/", ""), srcFile, dynamicPath, true);
                }
            }

            @Override
            public void onFileDelete(File srcFile) {
                System.out.println("X - File deleted skipped: " + srcFile);
            }

            @Override
            public void onFileChange(File srcFile) {
                if (extensionsList.contains(FilenameUtils.getExtension(srcFile.getName()))) {
                    System.out.println("V - File change: " + srcFile);
                    sendVault(vaultAddress, vaultToken, vaultPath, srcFile.getParent().replace(certificatesPath, "").replace("/", ""), srcFile, dynamicPath, true);
                }
            }

        });

        FileAlterationMonitor monitor = new FileAlterationMonitor(1000, observer);

        try {
            monitor.start();
            System.out.println("Start Certificate Sync Files to Vault");
        } catch (IOException e) {
            System.err.println("X - " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("X - " + e.getMessage());
        } catch (Exception e) {
            System.err.println("X - " + e.getMessage());
        }
    }

    /**
     * Monitors change in Vault
     *
     * @param certificatesPath
     * @param certificatesExtension
     * @param vaultAddress
     * @param vaultToken
     * @param vaultPath
     * @throws IOException
     */
    public static void syncVault(String certificatesPath, String certificatesExtension, String vaultAddress, String vaultToken, String vaultPath) throws IOException {
        try {
            System.out.println("Start Certificate Sync Vault to files - every 10 minutes");
            while (true) {
                List<String> list = getVaultList(vaultAddress, vaultToken, vaultPath);
                for (String domain : list) {
                    getCertificate(vaultAddress, vaultToken, vaultPath, domain, certificatesPath, certificatesExtension);
                }
                Thread.sleep(600000);
            }
        } catch (InterruptedException e) {
            System.err.println("X - " + e.getMessage());
        } catch (Exception e) {
            System.err.println("X - " + e.getMessage());
        }
    }

    /**
     * Gets through the contents of the file with the name of the domain present
     * in the folder defined in "dynamic-path", a dynamic path if any.
     *
     * @param folder
     * @param domain
     * @return
     */
    public static String dynamicPath(String folder, String domain) {
        try {
            if (folder != null) {
                Path path = Paths.get(folder + "/" + domain);
                if (Files.exists(path)) {
                    return "/" + new String(Files.readAllBytes(path)).replaceAll("[^A-Za-z0-9-_]", "") + "/";
                }
            }
        } catch (IOException ex) {
            System.err.println("X - " + ex.getMessage());
        }
        return "/";
    }

    /**
     * Send certificate files and delete secret to HashiCorp Vault
     *
     * @param vaultAddress
     * @param vaultToken
     * @param vaultPath
     * @param domain
     * @param certificate
     * @param dynamicPath
     * @param create
     */
    public static void sendVault(String vaultAddress, String vaultToken, String vaultPath, String domain, File certificate, String dynamicPath, boolean create) {
        try {
            Vault vault;
            vault = getVault(vaultToken, vaultAddress);
            if (create) {
                String content = FileUtils.readFileToString(certificate, StandardCharsets.UTF_8);
                final Map<String, Object> secrets = new HashMap<>();
                Map<String, String> currentSecrets = vault.logical().read(vaultPath + dynamicPath(dynamicPath, domain) + domain).getData();
                if (currentSecrets != null && !currentSecrets.isEmpty()) {
                    secrets.putAll(currentSecrets);
                }
                secrets.put(certificate.getName(), content);
                // Write operation
                vault.logical().write(vaultPath + dynamicPath(dynamicPath, domain) + domain, secrets);
                System.out.println("V - Send file " + certificate.getName() + " to " + vaultPath + dynamicPath(dynamicPath, domain) + domain + " -> " + vaultAddress);
            } else {
                // Delete operation
                vault.logical().delete(vaultPath + dynamicPath(dynamicPath, domain) + domain);
                System.out.println("V - Delete secret " + vaultPath + dynamicPath(dynamicPath, domain) + domain + " -> " + vaultAddress);
            }

        } catch (VaultException | IOException ex) {
            System.err.println("X - " + ex.getMessage());
        }

    }

    /**
     * Returns access to Vault
     *
     * @param vaultToken
     * @param vaultAddress
     * @return
     * @throws VaultException
     */
    private static Vault getVault(String vaultToken, String vaultAddress) throws VaultException {
        Vault vault;
        if (vaultToken.startsWith("approle")) {
            StringTokenizer approle = new StringTokenizer(vaultToken, ":");
            String path = approle.nextToken();
            String roleId = approle.nextToken();
            String secretId = approle.nextToken();
            String token = new Vault(new VaultConfig().address(vaultAddress).build(), 2).auth().loginByAppRole(path, roleId, secretId).getAuthClientToken();
            VaultConfig config = new VaultConfig()
                    .address(vaultAddress)
                    .token(token)
                    .build();
            vault = new Vault(config, 2);
        } else {
            VaultConfig config = new VaultConfig()
                    .address(vaultAddress)
                    .token(vaultToken)
                    .build();
            vault = new Vault(config, 2);
        }
        return vault;
    }

    /**
     * Get vault list
     *
     * @param vaultAddress
     * @param vaultToken
     * @param vaultPath
     * @return
     */
    private static List<String> getVaultList(String vaultAddress, String vaultToken, String vaultPath) {
        List<String> list = null;
        Vault vault;
        try {
            vault = getVault(vaultToken, vaultAddress);
            list = vault.logical().list(vaultPath).getListData();
        } catch (VaultException ex) {
            System.err.println("X - " + ex.getMessage());
        }
        return list;
    }

    /**
     * Get certificate
     *
     * @param vaultAddress
     * @param vaultToken
     * @param vaultPath
     * @return
     */
    private static void getCertificate(String vaultAddress, String vaultToken, String vaultPath, String domain, String certificatesPath, String certificatesExtension) {
        Vault vault;
        try {
            vault = getVault(vaultToken, vaultAddress);
            Map<String, String> currentSecrets = vault.logical().read(vaultPath + "/" + domain).getData();
            for (Map.Entry<String, String> secret : currentSecrets.entrySet()) {
                if (secret.getKey().endsWith(certificatesExtension)) {
                    File folder = new File(certificatesPath + "/" + domain);
                    if (!folder.exists()) {
                        folder.mkdirs();
                    }
                    if (!readCert(certificatesPath, domain, secret.getKey()).equals(secret.getValue())) {
                        saveCert(secret.getValue(), certificatesPath, domain, secret.getKey());
                    }
                } else {
                    System.out.println("X - File creation skipped: " + secret.getKey());
                }
            }
        } catch (VaultException ex) {
            System.err.println("X - " + ex.getMessage());
        }

    }

    /**
     * Write the certificate to disk
     *
     * @param content
     * @param certificatesPath
     * @param domain
     * @param fileName
     */
    private static void saveCert(String content, String certificatesPath, String domain, String fileName) {
        try (FileWriter writer = new FileWriter(certificatesPath + "/" + domain + "/" + fileName)) {
            writer.write(content);
            System.out.println("V - Saved file: " + fileName + " -> " + certificatesPath + "/" + domain);
        } catch (IOException ex) {
            System.err.println("X - " + ex.getMessage());
        }
    }

    /**
     * Write the certificate to disk
     *
     * @param content
     * @param certificatesPath
     * @param domain
     * @param fileName
     */
    private static String readCert(String certificatesPath, String domain, String fileName) {
        String cert = "";
        try {
            File file = new File(certificatesPath + "/" + domain + "/" + fileName);
            if (file.exists()) {                
                cert = new String(Files.readAllBytes(Paths.get(certificatesPath + "/" + domain + "/" + fileName)));
            }
        } catch (IOException ex) {
            System.err.println("X - " + ex.getMessage());
        }
        return cert;
    }
}
