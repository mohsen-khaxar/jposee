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

package org.jpos.util;

import java.io.IOException;
import java.io.PrintStream;
import java.io.File;
import java.util.Date;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Document;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

/**
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 */
public class IndexLogListener implements LogListener {
    StandardAnalyzer analyzer;
    public static final String PATH = "log/index";

    public IndexLogListener () {
        super();
        analyzer = new StandardAnalyzer();
    }
    public synchronized LogEvent log (LogEvent ev) {
        try {
            IndexWriter indexWriter = null;
            try {
                indexWriter = new IndexWriter (PATH, analyzer, false);
            } catch (IOException e) {
                indexWriter = new IndexWriter (PATH, analyzer, true);
            }
            Document doc = new Document();
            doc.add (Field.Keyword ("date", new Date()));
            doc.add (Field.Keyword ("realm", ev.getRealm()));
            doc.add (Field.Text ("event", ev.toString()));
            indexWriter.addDocument (doc);
            indexWriter.close();
        } catch (IOException e) {
            ev.addMessage (e);
        }
        return ev;
    }
}

