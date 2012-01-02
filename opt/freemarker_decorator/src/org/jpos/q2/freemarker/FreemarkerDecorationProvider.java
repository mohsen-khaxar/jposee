/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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

package org.jpos.q2.freemarker;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.cache.FileTemplateLoader;
import org.jpos.q2.ConfigDecorationProvider;

import java.io.File;
import java.io.StringWriter;
import java.io.IOException;
import java.util.HashMap;

/**
 * @author <a href="mailto:vsalaman@vmantek.com">Victor Salaman</a>
 * @version $Revision$ $Date$
 */
public class FreemarkerDecorationProvider implements ConfigDecorationProvider
{
    private Configuration config;
    private File deployDir;
    private final static String DEPLOY_PROPERTIES_TEMPLATE="deploy-properties.ftl";

    public void initialize(File deployDir) throws Exception
    {
        config=new Configuration();
        config.setTemplateLoader(new Q2ConfigFileLoader(deployDir));
        config.setTemplateUpdateDelay(0);
        config.setTagSyntax(Configuration.SQUARE_BRACKET_TAG_SYNTAX);
        if(new File(deployDir,DEPLOY_PROPERTIES_TEMPLATE).exists())
        {
            config.addAutoInclude(DEPLOY_PROPERTIES_TEMPLATE);
        }
        this.deployDir=deployDir;
    }

    public String decorateFile(File f) throws Exception
    {
        String ddPath=deployDir.getAbsolutePath();
        String filePath=f.getAbsolutePath();
        final int index = filePath.indexOf(ddPath);
        if(index<0)
        {
            throw new Exception("File "+filePath+" is not under deploy directory");
        }
        String nPath=filePath.substring(ddPath.length()+1);
        Template t=config.getTemplate(nPath);
        StringWriter sw=new StringWriter();
        t.process(new HashMap(),sw);
        return sw.toString();
    }

    public void uninitialize()
    {
        // Do nothing
    }

    public static class Q2ConfigFileLoader extends FileTemplateLoader
    {
        public Q2ConfigFileLoader(File file) throws IOException
        {
            super(file);
        }

        public long getLastModified(Object templateSource)
        {
            return System.currentTimeMillis();
        }
    }
}
