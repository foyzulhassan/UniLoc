package com.build.docsim;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.linear.*;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.*;
import org.apache.lucene.util.*;

import com.build.analyzer.config.Config;

public class CosineDocumentSimilarityTFIDF {

	public static final String CONTENT = "text";

	private Set<String> terms = new HashSet<>();
	private RealVector v1 = null;
	private RealVector v2 = null;
	private int passlen;
	private int failen;
	private Map<String,Integer> fileIdMap = new HashMap<String,Integer>();
	int docCounter;
	FSDirectory fsDir = null;
	
	IndexWriter writer = null;
	
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
	
	
	public CosineDocumentSimilarityTFIDF() throws IOException 
	{
		purgeDirectory(new File(Config.luceneDir));	
		this.docCounter=0;
		
		try {
			fsDir = FSDirectory.open(Paths.get(Config.luceneDir));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		CharArraySet stopSet = new CharArraySet(stopWords, false);
		Analyzer analyzer = new StandardAnalyzer(stopSet);

		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		writer = new IndexWriter(fsDir, iwc);	

	}
	
	public void addDocument(String filename,String content)throws IOException
	{	

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


		
		Document doc = new Document();		
		String str1 = null;
		try {
			str1 = content;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		try {
			str1=removeStopWordsAndStem(str1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		doc.add(new Field("text",str1 , Field.Store.NO,
				Field.Index.ANALYZED, Field.TermVector.YES));
		
		fileIdMap.put(filename, docCounter);
		docCounter++;
		writer.addDocument(doc);
	  
	}
	
	public void closeIndexWriter()
	{
		  try {
			writer.commit();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		 
	}
	
	public double getTFIDFCosineSimilarity(int queryindex, int docindex) throws IOException {
		FSDirectory fsDir = FSDirectory.open(Paths.get(Config.luceneDir));

		IndexReader reader = DirectoryReader.open(fsDir);

		Map<String, Integer> f1 = getTermFrequencies(reader, queryindex);
		Map<String, Integer> f2 = getTermFrequencies(reader, docindex);
		
		reader.close();

		if (f1 != null && f2 != null) {
			v1 = toRealVector(f1);
			v2 = toRealVector(f2);
		} else {
			v1 = null;
			v2 = null;
		}
		
		return getCosineSimilarity();
	}
	
	public double getTFIDFCosineSimilarity(String queryfile, String docfile) throws IOException {
		FSDirectory fsDir = FSDirectory.open(Paths.get(Config.luceneDir));

		IndexReader reader = DirectoryReader.open(fsDir);
		
		int queryindex=0;
		int	docindex=1;
		
		if(fileIdMap!=null)
		{
			queryindex=fileIdMap.get(queryfile);
			docindex=fileIdMap.get(docfile);
		}

		Map<String, Integer> f1 = getTermFrequencies(reader, queryindex);
		Map<String, Integer> f2 = getTermFrequencies(reader, docindex);
		
		reader.close();

		if (f1 != null && f2 != null) {
			v1 = toRealVector(f1);
			v2 = toRealVector(f2);
		} else {
			v1 = null;
			v2 = null;
		}
		
		return getCosineSimilarity();
	}
		

	double getCosineSimilarity() {

		if (v1 != null && v2 != null) {
			return (v1.dotProduct(v2)) / (v1.getNorm() * v2.getNorm());

		} else {
			return 0.0;
		}
	}



	Map<String, Integer> getTermFrequencies(IndexReader reader, int docId) throws IOException {
		Terms vector = reader.getTermVector(docId, CONTENT);
		if (vector == null) {
			return null;
		}
		TermsEnum termsEnum = null;
		termsEnum = vector.iterator(termsEnum);
		Map<String, Integer> frequencies = new HashMap<>();
		BytesRef text = null;
		while ((text = termsEnum.next()) != null) {
			String term = text.utf8ToString();
			int freq = (int) termsEnum.totalTermFreq();
			frequencies.put(term, freq);
			terms.add(term);
		}
		return frequencies;
	}

	RealVector toRealVector(Map<String, Integer> map) {
		RealVector vector = new ArrayRealVector(terms.size());
		int i = 0;
		for (String term : terms) {
			int value = map.containsKey(term) ? map.get(term) : 0;
			vector.setEntry(i++, value);
		}
		return (RealVector) vector.mapDivide(vector.getL1Norm());
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
	
	public static String removeStopWordsAndStem(String input) throws IOException 
	{

		String strinput=input.replaceAll("\\.", " \\. ");
		
		String[] stop_word={"auto", "break","case", "char","const","continue","default","do","double","else","enum",
				"extern", "float", "for", "goto", "if", "int", "long", "register", "return", "short", "signed", "sizeof",
				"static", "struct", "switch", "typedef", "union", "unsigned", "void", "volatile", "while","abstract","as" ,"base",
				"bool", "byte", "catch", "checked", "class", "decimal", "delegate", "event", "explicit", "false", "finally",
				"fixed", "foreach", "implicit", "in" , "interface", "internal", "is", "lock", "namespace", "new", "null"
				, "object", "operator", "out", "override", "params", "private", "protected", "public", "readonly", "ref",
				"sbyte", "sealed", "stackalloc", "string", "this", "throw", "true", "try", "typeof", "uint", "ulong", "unchecked"
				, "unsafe", "ushort", "using", "virtual", "gt" };
		ArrayList<String> stopWords = new ArrayList<String>();
		
		for (int k=0;k<stop_word.length;k++)
			stopWords.add(stop_word[k]);
		
		StandardTokenizer tokenStream1 = new StandardTokenizer();
		tokenStream1.setReader(new StringReader(strinput));
		
		TokenStream tokenStream =  tokenStream1;
	    tokenStream = new StopFilter(tokenStream, StandardAnalyzer.STOP_WORDS_SET);
	    tokenStream = new StopFilter(tokenStream, StopFilter.makeStopSet(stopWords));
	    tokenStream = new PorterStemFilter(tokenStream);
	    StringBuilder sb = new StringBuilder();
	    CharTermAttribute token = tokenStream.getAttribute(CharTermAttribute.class);
	   
	    tokenStream.reset();
	    while (tokenStream.incrementToken()) {
	        if (sb.length() > 0) {
	            sb.append(" ");
	        }
	        sb.append(token.toString());
	    }
	    tokenStream.end();
	    tokenStream.close();  
	  
	    
	    return sb.toString();
	}

}