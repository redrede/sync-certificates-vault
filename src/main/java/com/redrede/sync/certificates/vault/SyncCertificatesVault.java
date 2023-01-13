package com.redrede.sync.certificates.vault;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

/**
 * Sync certificates to Vault
 * 
 * @author marcelo
 */
public class SyncCertificatesVault {
    
    public static void main(String[] args) {
        try {
            if (args.length == 5) {
                System.out.println("Parameters");
                System.out.println("certificates-path: " + args[0]);
                System.out.println("certificates-extension: " + args[1]);
                System.out.println("vault-address " + args[2]);
                System.out.println("vault-token: " + args[3]);
                System.out.println("vault-path: " + args[4]);
                
                syncFiles(args[0], args[1], args[2], args[3], args[4]);
            } else {
                System.err.println("Error:");
                System.err.println("Enter the parameters");
                System.out.println("java -jar sync-certificates-vault.jar <certificates-path> <certificates-extension> <vault-address> <vault-token>  <vault-path>");
                System.out.println("Example: ");
                System.out.println("java -jar sync-certificates-vault.jar \"/etc/letsencrypt/live\" \"pem\" \"http://127.0.0.1:1234\" \"myroot\" \"secrets/certificates\" ");
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public static void syncFiles(String certificatesPath, String certificatesExtension, String vaultAddress, String vaultToken, String vaultPath) throws IOException {
        //get the file object

        List<String> extensionsList = Arrays.asList(certificatesExtension.split(","));
        
        File fromDir = FileUtils.getFile(certificatesPath);
        
        FileAlterationObserver observer = new FileAlterationObserver(fromDir);
        
        observer.addListener(new FileAlterationListenerAdaptor() {
            
            @Override
            public void onDirectoryCreate(File file) {
                
            }
            
            @Override
            public void onDirectoryDelete(File file) {
                
            }
            
            @Override
            public void onFileCreate(File srcFile) {
                if (extensionsList.contains(FilenameUtils.getExtension(srcFile.getName()))) {
                    System.out.println("File create: " + srcFile);
                    sendVault(vaultAddress, vaultToken, vaultPath, srcFile.getParent().replace(certificatesPath, "").replace("/", ""), srcFile);
                }
            }
            
            @Override
            public void onFileDelete(File file) {
                
            }
            
            @Override
            public void onFileChange(File srcFile) {
                if (extensionsList.contains(FilenameUtils.getExtension(srcFile.getName()))) {
                    System.out.println("File change: " + srcFile);
                    sendVault(vaultAddress, vaultToken, vaultPath, srcFile.getParent().replace(certificatesPath, "").replace("/", ""), srcFile);
                }
            }
            
        });
        
        FileAlterationMonitor monitor = new FileAlterationMonitor(1000, observer);
        
        try {
            monitor.start();
            System.out.println("Start file sync");
            
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    
    public static void sendVault(String vaultAddress, String vaultToken, String vaultPath, String domain, File certificate) {
        try {
            final VaultConfig config = new VaultConfig()
                    .address(vaultAddress)
                    .token(vaultToken)
                    .build();
            
            String content = FileUtils.readFileToString(certificate, StandardCharsets.UTF_8);
            
            final Vault vault = new Vault(config, 2);
            final Map<String, Object> secrets = new HashMap<>();
            
            Map<String, String> currentSecrets = vault.logical().read(vaultPath + "/" + domain).getData();
            if (currentSecrets != null && !currentSecrets.isEmpty()) {
                secrets.putAll(currentSecrets);
            }
            
            secrets.put(certificate.getName(), content);
            // Write operation
            vault.logical().write(vaultPath + "/" + domain, secrets);
            System.out.println("Send file " + certificate.getName() + " to " + vaultPath + "/" + domain +" -> " + vaultAddress);
            
        } catch (VaultException | IOException ex) {
            Logger.getLogger(SyncCertificatesVault.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
