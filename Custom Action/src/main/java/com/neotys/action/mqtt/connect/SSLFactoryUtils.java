package com.neotys.action.mqtt.connect;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;

import javax.net.ssl.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class SSLFactoryUtils {
	private TrustManager[] trustManagers;
	private KeyManager[] keyManagers;

	public void trustAll() {
		trustManagers = new TrustManager[]{
				new X509TrustManager() {
					@Override
					public void checkClientTrusted(final X509Certificate[] x509Certificates, final String s) {

					}

					@Override
					public void checkServerTrusted(final X509Certificate[] x509Certificates, final String s) {

					}

					@Override
					public X509Certificate[] getAcceptedIssuers() {
						return new X509Certificate[0];
					}
				}
		};
	}


	public void trustCa(final String caFile) throws Exception {
		try(FileInputStream fis = new FileInputStream(new File(caFile))) {
			X509Certificate ca = (X509Certificate) CertificateFactory.getInstance("X.509")
					.generateCertificate(new BufferedInputStream(fis));

			KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
			ks.load(null, null);
			ks.setCertificateEntry(Integer.toString(1), ca);

			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(ks);
			this.trustManagers = tmf.getTrustManagers();
		}
	}

	public void clientCert(final String crtFile, final String keyFile, final String password) throws Exception {
		Security.addProvider(new BouncyCastleProvider());
		final Certificate cert;
		final CertificateFactory cf = CertificateFactory.getInstance("X.509");
		try(final BufferedInputStream bis = new BufferedInputStream(new FileInputStream(crtFile))) {
			cert = cf.generateCertificate(bis);
		}
		final KeyPair key;
		try (PEMParser pemParser = new PEMParser(new FileReader(new File(keyFile)))) {
			Object object = pemParser.readObject();
			JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");

			if (object instanceof PEMEncryptedKeyPair) {
				PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder().build(password.toCharArray());
				key = converter.getKeyPair(((PEMEncryptedKeyPair) object).decryptKeyPair(decProv));
			} else {
				key = converter.getKeyPair((PEMKeyPair) object);
			}
		}
		final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(null, null);
		ks.setCertificateEntry("certificate", cert);
		ks.setKeyEntry("private-key", key.getPrivate(), password.toCharArray(), new java.security.cert.Certificate[]{cert});
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(ks, password.toCharArray());
		this.keyManagers = kmf.getKeyManagers();

	}

	public SSLSocketFactory build() throws NoSuchAlgorithmException, KeyManagementException {
		final SSLContext context = SSLContext.getInstance("TLSv1.2");
		context.init(keyManagers, trustManagers, null);
		return context.getSocketFactory();
	}
}
