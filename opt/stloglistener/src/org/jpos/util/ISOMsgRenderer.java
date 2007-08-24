/*
 * Copyright (c) 2007 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.util;

import java.util.Date;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import org.antlr.stringtemplate.AttributeRenderer;

import org.jpos.iso.ISOMsg;

public class ISOMsgRenderer implements AttributeRenderer {
    public String toString (Object o) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream p = new PrintStream (baos);
        ((ISOMsg)o).dump (p, "");
        return baos.toString();
    }
    public String toString (Object o, String format) {
        if ("short".equals (format))
            return o.toString();
        else
            return toString(o);
    }
}

