/*
 * 
 */
package dubna.walt.util;
    import javax.net.ssl.X509TrustManager;

/**
 *
 * @author serg
 */
public class RelaxedX509TrustManager implements X509TrustManager {
    public boolean isClientTrusted(java.security.cert.X509Certificate[] chain){ return true; }
    public boolean isServerTrusted(java.security.cert.X509Certificate[] chain){ return true; }
    @Override
    public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
    @Override
    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String input) {}
    @Override
    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String input) {}
    
}
