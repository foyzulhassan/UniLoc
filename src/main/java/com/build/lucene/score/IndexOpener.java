package com.build.lucene.score;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import com.build.analyzer.config.Config;

public class IndexOpener {
    
    public static IndexReader GetIndexReader() throws IOException {
        IndexReader indexReader = DirectoryReader.open(FSDirectory.open((Paths.get(Config.luceneDir))));
        return indexReader;
    }

    /**
     * Returns the total number of documents in the index
     * @return
     * @throws IOException 
     */
    public static Integer TotalDocumentInIndex() throws IOException
    {
        Integer maxDoc = GetIndexReader().maxDoc();
        GetIndexReader().close();
        return maxDoc;
    }
}