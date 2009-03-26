/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2009 Alejandro P. Revilla
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

package org.jpos.util;

import java.io.*;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import org.jpos.iso.ISOUtil;
import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;

/**
 * send e-mail with selected LogEvents to operator account
 * <b>Configuration properties</b>
 * <pre>
 *    jpos.operator.from=jpos
 *    jpos.operator.to=operator@foo.bar
 *    jpos.operator.subject.prefix=[jPOS]
 *    jpos.operator.tags="Operator ISORequest SystemMonitor"
 *    jpos.operator.delay=10000
 *    jpos.mail.smtp.host=localhost
 * </pre>
 *
 * @author apr@cs.com.uy
 * @version $Id: OperatorLogListener.java 2130 2005-04-11 15:58:24Z apr $
 */
public class OperatorLogListener 
    implements LogListener, Configurable, Runnable
{
    Configuration cfg;
    BlockingQueue queue;
    boolean logExceptions;

    public OperatorLogListener () {
        super();
        queue = new BlockingQueue();
    }
    public OperatorLogListener (Configuration cfg) {
        super();
        this.cfg = cfg;
        queue = new BlockingQueue();
        new Thread(this).start();
    }
    public void setConfiguration (Configuration cfg) 
        throws ConfigurationException
    {
        this.cfg = cfg;
        assertProperty ("jpos.operator.to");
        assertProperty ("jpos.operator.subject.prefix");
        assertProperty ("jpos.operator.tags");
        assertProperty ("jpos.operator.delay");
        assertProperty ("jpos.mail.smtp.host");

        logExceptions = cfg.get ("jpos.operator.tags").
            indexOf ("exception") >= 0;

        new Thread(this).start();
    }
    public void run() {
        Thread.currentThread().setName ("OperatorLogListener");
        int delay = cfg.getInt ("jpos.operator.delay");
        try {
            ISOUtil.sleep (2500);   // initial delay
            for (;;) {
                try {
                    LogEvent ev[] = new LogEvent[1];
                    if (queue.pending() > 0) {
                        ev = new LogEvent [queue.pending()];
                        for (int i=0; i < ev.length; i++)
                            ev[i] = (LogEvent) queue.dequeue();
                    } else 
                        ev[0] = (LogEvent) queue.dequeue();
                    sendMail (ev);
                    if (delay > 0)
                        Thread.sleep (delay);
                } catch (InterruptedException e) { }
            }
        } catch (BlockingQueue.Closed e) { }
    }
    private void sendMail (LogEvent[] ev) {
        String from    = cfg.get ("jpos.operator.from", "jpos-logger");
        String[] to    = cfg.getAll ("jpos.operator.to");
        String[] cc    = cfg.getAll ("jpos.operator.cc");
        String[] bcc   = cfg.getAll ("jpos.operator.bcc");
        String subject = cfg.get ("jpos.operator.subject.prefix");
        if (ev.length > 1) 
            subject = subject + ev.length + " events";
        else
            subject = subject + ev[0].getRealm() + " - " +ev[0].tag;

        // create some properties and get the default Session
        Properties props = System.getProperties();
        props.put("mail.smtp.host", cfg.get ("jpos.mail.smtp.host", 
                "localhost"));
        
        Session session = Session.getDefaultInstance(props, null);
        session.setDebug(false);
        
        try {
            // create a message
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom (new InternetAddress(from));

            InternetAddress[] address = new InternetAddress[to.length];
            for (int i=0; i<to.length; i++) 
                address[i] = new InternetAddress (to[i]);
            msg.setRecipients (Message.RecipientType.TO, getAddress (to));
            msg.setRecipients (Message.RecipientType.CC, getAddress (cc));
            msg.setRecipients (Message.RecipientType.BCC, getAddress (bcc));
            msg.setSubject(subject);
            Multipart mp = new MimeMultipart();

            for(int i=0; i<ev.length; i++) {
                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                PrintStream p = new PrintStream (buf);
                ev[i].dump (p, "");
                p.close();
        
                // create and fill the first message part
                MimeBodyPart mbp = new MimeBodyPart();
                mbp.setText(buf.toString());
                mbp.setFileName (ev[i].tag + "_" + i + ".txt");
                mp.addBodyPart(mbp);
            }
            msg.setContent(mp);
            msg.setSentDate(new Date());
            Transport.send(msg);
        } catch (MessagingException mex) {
            mex.printStackTrace();
            Exception ex = null;
            if ((ex = mex.getNextException()) != null) {
                ex.printStackTrace();
            }
        }
    }
    private boolean checkOperatorTag(LogEvent ev) {
        String tags = cfg.get ("jpos.operator.tags");

        return (tags.indexOf (ev.tag) >= 0) || (logExceptions && hasException (ev));
    }
    private boolean hasException (LogEvent evt) {
        Iterator iter = evt.getPayLoad().iterator();
        while (iter.hasNext()) {
            if (iter.next() instanceof Throwable)
                return true;
        }
        return false;
    }
    private InternetAddress[] getAddress (String[] s) throws AddressException {
        InternetAddress[] address = new InternetAddress[s.length];
        for (int i=0; i<s.length; i++) 
            address[i] = new InternetAddress (s[i]);
        return address;
    }
    public synchronized LogEvent log (LogEvent ev) {
        if (checkOperatorTag(ev))
            queue.enqueue (ev);
        return ev;
    }
    private void assertProperty (String propName) throws ConfigurationException
    {
        if (cfg.get (propName) == null)
            throw new ConfigurationException 
                (propName + " property not present");
    }
}
