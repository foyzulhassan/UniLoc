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

public class CosineDocumentSimilarity {

	public static final String CONTENT = "text";

	private Set<String> terms = new HashSet<>();
	private RealVector v1 = null;
	private RealVector v2 = null;
	private int passlen;
	private int failen;

	CosineDocumentSimilarity(String s1, String s2) throws IOException {

		RAMDirectory ramDir = new RAMDirectory();

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
		IndexWriter writer = new IndexWriter(ramDir, iwc);
		
		//writer.deleteAll();
		//writer.commit();		

		
		Document doc = new Document();		
		String str1=FileUtils.readFileToString(new File(s1), "UTF-8");	
		str1=removeStopWordsAndStem(str1);	
		if(str1.length()<=0)
		{
			str1="nolog";
		}
		doc.add(new Field("text",str1 , Field.Store.NO,
				Field.Index.ANALYZED, Field.TermVector.YES));
		writer.addDocument(doc);
		
		doc = new Document();
		String str2=FileUtils.readFileToString(new File(s2), "UTF-8");	
		str2=removeStopWordsAndStem(str2);	
		
		if(str2.length()<=0)
		{
			str2="empquery";
		}
		doc.add(new Field("text", str2, Field.Store.NO,
				Field.Index.ANALYZED, Field.TermVector.YES));
		writer.addDocument(doc);
	//	writer.commit();		
		writer.close();
		

		IndexReader reader = DirectoryReader.open(ramDir);

		Map<String, Integer> f1 = getTermFrequencies(reader, 0);
		Map<String, Integer> f2 = getTermFrequencies(reader, 1);
		reader.close();
		
		if(f1!=null)
			v1 = toRealVector(f1);
		if(f2!=null)
			v2 = toRealVector(f2);

	}

	public CosineDocumentSimilarity(List<String> buildpasslines, List<String> buildfaillines) {
		purgeDirectory(new File(Config.luceneDir));
		FSDirectory fsDir = null;
		try {
			fsDir = FSDirectory.open(Paths.get(Config.luceneDir));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		passlen=buildpasslines.size();
		failen=buildfaillines.size();
		
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
			String s1 = null;
			try {
				s1 = removeStopWordsAndStem(buildpasslines.get(index));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			doc.add(new Field("text", convertToUTF8(s1), Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.YES));
			try {
				writer.addDocument(doc);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		for (int index = 0; index < buildfaillines.size(); index++) {
			Document doc = new Document();
			String s1 = null;
			try {
				s1 = removeStopWordsAndStem(buildfaillines.get(index));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			doc.add(new Field("text", convertToUTF8(s1), Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.YES));
			try {
				writer.addDocument(doc);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			writer.commit();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	CosineDocumentSimilarity(String s1, String s2, boolean flag) throws IOException {
		// Directory directory = createIndex(s1, s2);
		// IndexReader reader = DirectoryReader.open(directory);
		// RAMDirectory ramDir = new RAMDirectory();
		// IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		purgeDirectory(new File(Config.luceneDir));
		FSDirectory fsDir = FSDirectory.open(Paths.get(Config.luceneDir));

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
				"unchecked", "unsafe", "ushort", "using", "virtual", "build", "download", "time", "Resolving");

		CharArraySet stopSet = new CharArraySet(stopWords, false);
		Analyzer analyzer = new StandardAnalyzer(stopSet);

		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		// IndexWriter writer = new IndexWriter(ramDir, new
		// StandardAnalyzer(Version.LUCENE_36), true,
		// IndexWriter.MaxFieldLength.UNLIMITED);
		IndexWriter writer = new IndexWriter(fsDir, iwc);
		writer.deleteAll();
		Document doc = new Document();
		doc.add(new Field("text", convertToUTF8(s1), Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.YES));
		writer.addDocument(doc);
		doc = new Document();
		doc.add(new Field("text", convertToUTF8(s2), Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.YES));
		writer.addDocument(doc);
		writer.close();

		IndexReader reader = DirectoryReader.open(fsDir);

		Map<String, Integer> f1 = getTermFrequencies(reader, 0);
		Map<String, Integer> f2 = getTermFrequencies(reader, 1);
		reader.close();

		if (f1 != null && f2 != null) {
			v1 = toRealVector(f1);
			v2 = toRealVector(f2);
		} else {
			v1 = null;
			v2 = null;
		}

	}

	public double getCosineSimilarity(int passindex, int failindex) throws IOException {
		FSDirectory fsDir = FSDirectory.open(Paths.get(Config.luceneDir));

		IndexReader reader = DirectoryReader.open(fsDir);

		Map<String, Integer> f1 = getTermFrequencies(reader, passindex);
		Map<String, Integer> f2 = getTermFrequencies(reader, passlen+failindex);
		
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

	public static double getCosineSimilarity(String s1, String s2) throws IOException {
		return new CosineDocumentSimilarity(s1, s2).getCosineSimilarity();
	}

	public static double getCosineSimilarityFromText(String s1, String s2) throws IOException {
		return new CosineDocumentSimilarity(s1, s2, false).getCosineSimilarity();
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
		//String strinput=input;
		
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
	
	public static void main(String[] args)
	{
		String str1="\\hometest\\file.java\n\n has some issue\ndependency a:c:c missing";
		//str1=str1.replace("\\", "SP");
		
		try {
			String str2=removeStopWordsAndStem(str1);
			System.out.println(str2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}