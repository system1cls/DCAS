package org.server;


import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;


import java.io.FileReader;
import java.security.PrivateKey;

public class Main {
    public static void main(String[] args) {

        PrivateKey privateKey = null;
        try {
            FileReader fileReader = new FileReader("C:\\Users\\syste\\IdeaProjects\\DPAS1\\keyForCert");
            PEMParser parser = new PEMParser(fileReader);

            Object object = parser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();

            if (object instanceof PEMKeyPair) {
                privateKey = converter.getPrivateKey(((PEMKeyPair) object).getPrivateKeyInfo());
                System.out.println(privateKey.getAlgorithm());
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }

        Parser parser = new Parser();
        int cntThreads = parser.getCntThreads(args);
        String issuer = parser.getIssuer(args);

        Server server = new Server(2000, 1, cntThreads, privateKey, issuer);
        server.startServer();

    }

}