package org.client;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;

import org.server.ClientData;

public class Main {

    public static void main(String []args) {
        if (args.length < 4) {
            throw new RuntimeException("Not enough args");
        }

        String name = args[0];
        String ip = args[1];
        int port = parseInt(args[2]);
        String fileNameToSave = args[3];
        int cntWait = 0;
        if (args.length > 4) {
            cntWait = parseInt(args[4]);
        }



        boolean ungetted = true;
        ClientData data = null;

        while (ungetted) {
            try (SocketChannel channel = SocketChannel.open(new InetSocketAddress(ip, port));) {
                ByteBuffer nameBuffer = ByteBuffer.allocate(name.length() + 4);
                nameBuffer.putInt(name.length());
                nameBuffer.put(name.getBytes(StandardCharsets.UTF_8));

                nameBuffer.flip();

                while (nameBuffer.hasRemaining()) {
                    channel.write(nameBuffer);
                }

                if (cntWait != 0) Thread.sleep(cntWait);

                ByteBuffer lengthOfAns = ByteBuffer.allocate(4);

                boolean isReady = true;
                while(lengthOfAns.hasRemaining()) {
                    if (channel.read(lengthOfAns) == -1) {
                        isReady = false;
                        break;
                    }
                }

                if (!isReady) {
                    System.out.println("isn`t ready");
                    Thread.sleep(10000);
                    continue;
                }

                lengthOfAns.flip();
                int length = lengthOfAns.getInt();

                ByteBuffer dataBuffer = ByteBuffer.allocate(length);


                while (dataBuffer.hasRemaining()) {
                    channel.read(dataBuffer);
                }

                dataBuffer.flip();

                InputStream is = new ByteArrayInputStream(dataBuffer.array());
                ObjectInputStream ois = new ObjectInputStream(is);

                data = (ClientData) ois.readObject();
                ungetted = false;
                ois.close();
                is.close();

            } catch (IOException e) {
                System.out.println(e.getMessage());
            } catch (InterruptedException | ClassNotFoundException e) {
                System.out.println(e.getMessage());
                throw new RuntimeException(e);
            }
        }

        saveClientData(data, fileNameToSave);
    }

    private static int parseInt(String portStr) {
        try {
            return Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static void saveClientData(ClientData data, String fileNameToSave) {
        savePrivate(data.keys.getPrivate(), fileNameToSave + "-private.key");
        savePublic(data.keys.getPublic(), fileNameToSave + "-public.key");
        saveCert(data.certificte, fileNameToSave + "-cert.crt");
    }

    private static void savePublic(PublicKey key, String fileName) {
        String encoded = Base64.getEncoder().encodeToString(key.getEncoded());

        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("-----BEGIN PUBLIC KEY-----\n");
            for (int i = 0; i < encoded.length(); i += 64) {
                writer.write(encoded.substring(i, Math.min(i + 64, encoded.length())) + "\n");
            }
            writer.write("-----END PUBLIC KEY-----\n");
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static void savePrivate(PrivateKey key, String fileName) {
        String encoded = Base64.getEncoder().encodeToString(key.getEncoded());

        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("-----BEGIN PRIVATE KEY-----\n");
            for (int i = 0; i < encoded.length(); i += 64) {
                writer.write(encoded.substring(i, Math.min(i + 64, encoded.length())) + "\n");
            }
            writer.write("-----END PRIVATE KEY-----\n");
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static void saveCert(X509Certificate cert, String fileName) {
        try {
            String encoded = Base64.getEncoder().encodeToString(cert.getEncoded());

            FileWriter writer = new FileWriter(fileName);
            writer.write("-----BEGIN CERTIFICATE-----\n");

            for (int i = 0; i < encoded.length(); i += 64) {
                writer.write(encoded.substring(i, Math.min(i + 64, encoded.length())) + "\n");
            }
            writer.write("-----END CERTIFICATE-----\n");

            writer.close();
        } catch (CertificateEncodingException | IOException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
