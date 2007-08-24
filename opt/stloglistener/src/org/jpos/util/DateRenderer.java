/*
 * Copyright (c) 2007 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.util;

import java.util.Date;
import java.text.SimpleDateFormat;
import org.antlr.stringtemplate.AttributeRenderer;


public class DateRenderer implements AttributeRenderer {
    public String toString (Object o) {
        return toString (o, "yyyy.MM.dd");
    }
    public String toString (Object o, String format) {
        if (o instanceof Date) {
            SimpleDateFormat df = new SimpleDateFormat(format);
            return df.format ((Date)o);
        } else 
            return o.toString();
    }
}

