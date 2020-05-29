package com.build.logfilter;

/*
 * This program has been written as part of solution for Information Retrieval course 
 * programming assignment 1. Code has been developed by group of following 4 person
 * Rai Jitendra
 * Shivam Maurya
 * Gaurav Sharma
 * Mukul Salhotra
 * This is the main program where it first creates the indexes by running through
 * the directory and all text and html files present in it. After that it creates 
 * index on the content of the file. 
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream.GetField;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.print.Doc;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.commons.io.FileUtils;
import org.tartarus.snowball.ext.PorterStemmer;

import com.build.analyzer.config.Config;

public class LogLineSimGenerator {

	public LogLineSimGenerator(List<String> buildpasslines) {
		generateDataIndex(buildpasslines);
	}

	private void generateDataIndex(List<String> buildpasslines) {

		purgeDirectory(new File(Config.luceneDir));
		FSDirectory fsDir = null;
		try {
			fsDir = FSDirectory.open(Paths.get(Config.luceneDir));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Index the full text of both documents
		// CharArraySet stopword=
		List<String> stopWords = Arrays.asList("a", "an", "and", "are", "as", "at", "be", "but", "by", "for", "if",
				"in", "into", "is", "it", "no", "not", "of", "on", "or", "such", "that", "the", "their", "then",
				"there", "these", "they", "this", "to", "was", "will", "with", "java", "build", "auto", "break", "case",
				"char", "const", "continue", "default", "do", "double", "else", "enum", "extern", "float", "for",
				"goto", "if", "int", "long", "register", "return", "short", "signed", "sizeof", "static", "struct",
				"switch", "typedef", "union", "unsigned", "void", "volatile", "while", "abstract", "as", "base", "bool",
				"byte", "catch", "checked", "class", "decimal", "delegate", "event", "explicit", "false", "finally",
				"fixed", "foreach", "implicit", "in", "interface", "internal", "is", "lock", "namespace", "new", "null",
				"object", "operator", "out", "override", "params", "private", "protected", "public", "readonly", "ref",
				"sbyte", "sealed", "stackalloc", "string", "this", "throw", "true", "try", "typeof", "uint", "ulong",
				"unchecked", "unsafe", "ushort", "using", "virtual", "build", "download", "time", "Resolving",
				"dependencies");

		CharArraySet stopSet = new CharArraySet(stopWords, false);
		Analyzer analyzer = new StandardAnalyzer(stopSet);

		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		// IndexWriter writer = new IndexWriter(ramDir, new
		// StandardAnalyzer(Version.LUCENE_36), true,
		// IndexWriter.MaxFieldLength.UNLIMITED);
		IndexWriter writer = null;
		try {
			writer = new IndexWriter(fsDir, iwc);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	

		for (int index = 0; index < buildpasslines.size(); index++) {
			Document doc = new Document();
			String s1 = buildpasslines.get(index);
			doc.add(new Field("text", convertToUTF8(s1), Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.YES));
			try {
				writer.addDocument(doc);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//writer

	}
	
	
	public double getTopSimValue(String searchQuery) throws IOException, ParseException {
		Searcher searcher = new Searcher(Config.luceneDir);
		double topsim=0.0;
		TopDocs hits = searcher.search(searchQuery);
		
		if(hits.scoreDocs.length>0)
		{
			ScoreDoc scoreDoc=hits.scoreDocs[0];
			topsim=scoreDoc.score;
		}
	
		return topsim;
	}

	static void purgeDirectory(File dir) {
		try {
			for (File file : dir.listFiles()) {
				if (file.isDirectory())
					purgeDirectory(file);
				file.delete();
			}
		} catch (NullPointerException e) {
			System.out.println("Index directory empty. Continue");
		}

	}

	static public String QueryPorterStemmer(String QueryString) {
		PorterStemmer stemmer = new PorterStemmer();

		StringBuffer stemedLine = new StringBuffer();
		String[] words = QueryString.split(" ");
		for (int i = 0; i < words.length; i++) {
			stemmer.setCurrent(words[i]);
			stemmer.stem();
			stemedLine.append(stemmer.getCurrent() + " ");
		}
		return stemedLine.toString();
	}



	// convert from internal Java String format -> UTF-8
	public static String convertToUTF8(String s) {
		String out = null;
		try {
			out = new String(s.getBytes("UTF-8"), "ISO-8859-1");
		} catch (java.io.UnsupportedEncodingException e) {
			return null;
		}
		return out;
	}
}
