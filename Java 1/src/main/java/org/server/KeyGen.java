package org.server;

import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Queue;
import java.util.Set;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

public class KeyGen implements Runnable {

    KeyPairGenerator generator;
    String issuer;
    long days;
    final ContentSigner contentSigner;
    final Queue<String> queue;
    final HashMap<String, ClientData> clientDataHashMap;

    KeyGen(String hashAlgorithm, PrivateKey privateKey, Queue<String> queue,
           HashMap<String, ClientData> clientDataHashMap, String issuer) {
        this.clientDataHashMap = clientDataHashMap;
        this.queue = queue;
        this.issuer = issuer;

        try {

            contentSigner = new JcaContentSignerBuilder(hashAlgorithm).build(privateKey);

            generator = KeyPairGenerator.getInstance("RSA", "SunRsaSign");
            generator.initialize(8192);


        } catch (OperatorCreationException | NoSuchProviderException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


    ClientData generate(String name) {
        try {
            KeyPair pair = generator.generateKeyPair();


            final Instant now = Instant.now();
            final Date notBefore = Date.from(now);
            final Date notAfter = Date.from(now.plus(Duration.ofDays(days)));

            X500Name issuerX500Name = new X500NameBuilder().addRDN(BCStyle.NAME, issuer).build();
            X500Name clientX500Name = new X500NameBuilder().addRDN(BCStyle.NAME, name).build();

            X509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder(
                    issuerX500Name,
                    BigInteger.valueOf(now.toEpochMilli()),
                    notBefore,
                    notAfter,
                    clientX500Name,
                    pair.getPublic()
            );

            var certificate = new JcaX509CertificateConverter()
                    .setProvider(new BouncyCastleProvider()).getCertificate(certificateBuilder.build(contentSigner));

            return new ClientData(pair, certificate);

        } catch (CertificateException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }


    @Override
    public void run() {
        String name;

        while(true) {
            try {
                synchronized (queue) {
                    if (queue.isEmpty()) {
                        queue.wait();
                    }

                    name = queue.poll();
                }

                ClientData clientData = generate(name);

                synchronized (clientDataHashMap) {
                    System.out.println("putting data for client: " + name);
                    clientDataHashMap.put(name, clientData);
                }

            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
                throw new RuntimeException(e);
            }


        }

    }
}
