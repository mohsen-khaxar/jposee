/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2010 Alejandro P. Revilla
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.q2.nserver;

import org.jpos.core.ConfigurationException;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.apache.mina.filter.ssl.KeyStoreFactory;
import org.apache.mina.filter.ssl.SslContextFactory;
import org.apache.mina.filter.ssl.SslFilter;

import java.io.File;

public class NServerSSL extends NServer implements NServerSSLMBean
{
    String keystore;
    String keystorePassword;
    String keyPassword;

    public String getKeystore()
    {
        return keystore;
    }

    public void setKeystore(String keystore)
    {
        this.keystore = keystore;
    }

    public String getKeystorePassword()
    {
        return keystorePassword;
    }

    public void setKeystorePassword(String keystorePassword)
    {
        this.keystorePassword = keystorePassword;
    }

    public String getKeyPassword()
    {
        return keyPassword;
    }

    public void setKeyPassword(String keyPassword)
    {
        this.keyPassword = keyPassword;
    }


    @Override
    protected void startService() throws Exception
    {
        if(keystore==null || keystore.trim().length()==0) throw new ConfigurationException("keystore is a required parameter");
        if(keystorePassword == null || keystorePassword.trim().length()==0) throw new ConfigurationException("keystore-password is a required parameter");
        if(keyPassword == null || keyPassword.trim().length()==0) throw new ConfigurationException("key-password is a required parameter");
        super.startService();
    }

    @Override
    protected void preBindAcceptorHook(NioSocketAcceptor acceptor) throws Exception
    {
        final KeyStoreFactory keyStoreFactory = new KeyStoreFactory();
        final SslContextFactory f=new SslContextFactory();

        keyStoreFactory.setDataFile(new File(keystore));
        keyStoreFactory.setPassword(keystorePassword);

        f.setProtocol("TLS");
        f.setKeyManagerFactoryAlgorithm("SunX509");
        f.setKeyManagerFactoryKeyStore(keyStoreFactory.newInstance());
        f.setKeyManagerFactoryKeyStorePassword(keyPassword);
        SslFilter ssl = new SslFilter(f.newInstance());
        acceptor.getFilterChain().addFirst("sslFilter",ssl);
    }
}
