package org.jpos.q2.nserver;

public interface NServerSSLMBean extends NServerMBean
{
    public String getKeystore();
    public void setKeystore(String keystore);
    public String getKeystorePassword();
    public void setKeystorePassword(String keystorePassword);
    public String getKeyPassword();
    public void setKeyPassword(String keystorePassword);
}
