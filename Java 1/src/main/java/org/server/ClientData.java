package org.server;

import java.io.Serializable;
import java.security.KeyPair;
import java.security.cert.X509Certificate;

public class ClientData implements Serializable {
    public final KeyPair keys;
    public final X509Certificate certificte;

    ClientData(KeyPair keys, X509Certificate certificte) {
        this.keys = keys;
        this.certificte = certificte;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("Public key:\n").append(keys.getPublic().toString())
                .append("\n\nPrivate key:\n").append(keys.getPrivate().toString())
                .append("\n\nCertificate:\n").append(certificte.toString())
                .append("\n");

        return builder.toString();
    }
}
