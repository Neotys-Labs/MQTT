/**
 * Utility class to read encrypted PEM files and generate a
 * SSL Socket Factory based on the provided certificates.
 * The original code is by Sharon Asher (link below). I have modified
 * it to use a newer version of the BouncyCastle Library (v1.52)
 *
 * Reference - https://gist.github.com/sharonbn/4104301"
 */
package com.neotys.action.mqtt.connect;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;

public class SSLUtil {

    public static SSLSocketFactory getSocketFactory(final String caCrtFile, final String crtFile, final String keyFile,
                                                    final String password) {
        try {
            String Crtpath, CaCertpath,Keypath;

            /**
             * Add BouncyCastle as a Security Provider
             */
            Security.addProvider(new BouncyCastleProvider());

            // load CA certificate
            X509Certificate caCert = null;
            CaCertpath = caCrtFile.replace("\\", "/");
// or
            CaCertpath = CaCertpath.replaceAll("\\\\", "/");

            FileInputStream fis = new FileInputStream(CaCertpath);
            BufferedInputStream bis = new BufferedInputStream(fis);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            while (bis.available() > 0) {
                caCert = (X509Certificate) cf.generateCertificate(bis);
                // System.out.println(caCert.toString());
            }

            Crtpath = crtFile.replace("\\", "/");

            Crtpath = Crtpath.replaceAll("\\\\", "/");

            // load client certificate
            bis = new BufferedInputStream(new FileInputStream(Crtpath));
            X509Certificate cert = null;
            while (bis.available() > 0) {
                cert = (X509Certificate) cf.generateCertificate(bis);
                // System.out.println(caCert.toString());
            }
            Keypath = keyFile.replace("\\", "/");
// or
            Keypath = Keypath.replaceAll("\\\\", "/");
            // load client private key
            PEMParser pemParser = new PEMParser(new FileReader(Keypath));
            Object object = pemParser.readObject();
            PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder()
                    .build(password.toCharArray());
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter()
                    .setProvider("BC");
            KeyPair key;
            if (object instanceof PEMEncryptedKeyPair) {
                System.out.println("Encrypted key - we will use provided password");
                key = converter.getKeyPair(((PEMEncryptedKeyPair) object)
                        .decryptKeyPair(decProv));
            } else {
                System.out.println("Unencrypted key - no password needed");
                key = converter.getKeyPair((PEMKeyPair) object);
            }
            pemParser.close();

            // CA certificate is used to authenticate server
            KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
            caKs.load(null, null);
            caKs.setCertificateEntry("ca-certificate", caCert);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
            tmf.init(caKs);

            // client key and certificates are sent to server so it can authenticate
            // us
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, null);
            ks.setCertificateEntry("certificate", cert);
            ks.setKeyEntry("private-key", key.getPrivate(), password.toCharArray(),
                    new java.security.cert.Certificate[] { cert });
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory
                    .getDefaultAlgorithm());
            kmf.init(ks, password.toCharArray());

            // finally, create SSL socket factory
            SSLContext context = SSLContext.getInstance("TLSv1.2");
            context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            return context.getSocketFactory();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}