package es.gimix.net;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import android.content.Context;

// http://www.thoughtcrime.org/blog/authenticity-is-broken-in-ssl-but-your-app-ha/
public class PinningCertManager {
	/** Installs a certificate pinning as default SSL socket
	 * 
	 * @param appContext application context to load resources from
	 * @param resourceId resource id of a BKS truststore
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws CertificateException
	 * @throws IOException
	 */
    public static void install(Context appContext, int resourceId) throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
    	// load certificates
		// use Portecle [http://portecle.sourceforge.net/] to manipulate the keystore

		// load keystore from file in Android resources. Resources can be included in Android library projects
    	KeyStore truststore = KeyStore.getInstance("BKS");
    	InputStream in = appContext.getResources().openRawResource(resourceId);
    	try {
            truststore.load(in, null /* no password */);
        } finally {
            in.close();
        }

    	// create a trust manager
        TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        factory.init(truststore);

        // Install trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, factory.getTrustManagers(), new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
