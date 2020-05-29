package com.build.logfilter;
/*
 * This file handles the java search functionality of the retrieval system.
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.tartarus.snowball.ext.PorterStemmer;

import com.ir.assignment.LuceneConstants;

public class Searcher {
	IndexSearcher indexSearcher;
	QueryParser queryParser;
	Query query;
	
	public Searcher(String indexDirectoryPath) 
		      throws IOException{
		//Initialize the directory which has to be searched and attach the Index
		Directory dir = FSDirectory.open(Paths.get(indexDirectoryPath));
	    IndexReader reader = DirectoryReader.open(dir);
	    this.indexSearcher = new IndexSearcher(reader);
	    
	}
	
	
	
	public Document getDocument(ScoreDoc scoreDoc) 
		      throws CorruptIndexException, IOException{
		      return indexSearcher.doc(scoreDoc.doc);	
	}
	
	public TopDocs search(String searchQuery) 
		      throws IOException, ParseException{
		//Query the index for the search result and return the ranked results
		
		// Some constants.
		String LUCENE_ESCAPE_CHARS = "[\\\\+\\-\\!\\(\\)\\:\\^\\]\\{\\}\\~\\*\\?]";
		Pattern LUCENE_PATTERN = Pattern.compile(LUCENE_ESCAPE_CHARS);
		String REPLACEMENT_STRING = "\\\\$0";
		 


//		// ...
//		searchQuery=searchQuery.replaceAll(":", " ");
//		searchQuery=searchQuery.replaceAll("/", "//");
//		
//		Pattern pt = Pattern.compile("[^a-zA-Z0-9]");
//        Matcher match= pt.matcher(searchQuery);
//        while(match.find())
//        {
//            String s= match.group();
//            searchQuery=searchQuery.replaceAll(match.group(), "");
//        }
        
		String escaped = LUCENE_PATTERN.matcher(searchQuery).replaceAll(REPLACEMENT_STRING);
		escaped = QueryParser.escape(escaped);

		queryParser = new QueryParser(LuceneConstants.CONTENTS2, new StandardAnalyzer());
		Query query = queryParser.parse(escaped);
        
	  
	    //query = queryParser.parse(searchQuery);
	    return indexSearcher.search(query, LuceneConstants.MAX_SEARCH);
	}

}
