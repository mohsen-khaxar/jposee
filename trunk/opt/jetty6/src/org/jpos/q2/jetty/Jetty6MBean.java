/*
 * Jetty6MBean.java
 *
 * Created on February 4, 2007, 11:18 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.jpos.q2.jetty;

import org.jpos.q2.QBeanSupportMBean;

/**
 *
 * @author msc
 */
public interface Jetty6MBean extends QBeanSupportMBean{
    
   
    public void setConfig (String config);
    public String getConfig ();
            
}
