package com.build.keyword;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.standard.ClassicFilter;
import org.apache.lucene.analysis.standard.ClassicTokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

public class TermExtractor {

	public static String stem(String term) throws IOException {

		TokenStream tokenStream = null;
		try {

			// tokenize
			 tokenStream = new ClassicTokenizer();
			// stem
			tokenStream = new PorterStemFilter(tokenStream);

			// add each token in a set, so that duplicates are removed
			Set<String> stems = new HashSet<String>();
			CharTermAttribute token = tokenStream.getAttribute(CharTermAttribute.class);
			tokenStream.reset();
			while (tokenStream.incrementToken()) {
				stems.add(token.toString());
			}

			// if no stem or 2+ stems have been found, return null
			if (stems.size() != 1) {
				return null;
			}
			String stem = stems.iterator().next();
			// if the stem has non-alphanumerical chars, return null
			if (!stem.matches("[a-zA-Z0-9-]+")) {
				return null;
			}

			return stem;

		} finally {
			if (tokenStream != null) {
				tokenStream.close();
			}
		}

	}

	public static <T> T find(Collection<T> collection, T example) {
		for (T element : collection) {
			if (element.equals(example)) {
				return element;
			}
		}
		collection.add(example);
		return example;
	}

	public static List<Keyword> guessFromString(String input) throws IOException {

		TokenStream tokenStream = null;
		try {

			// hack to keep dashed words (e.g. "non-specific" rather than "non"
			// and "specific")
			input = input.replaceAll("-+", "-0");
			// replace any punctuation char but apostrophes and dashes by a
			// space
			input = input.replaceAll("[\\p{Punct}&&[^'-]]+", " ");
			// replace most common english contractions
			input = input.replaceAll("(?:'(?:[tdsm]|[vr]e|ll))+\\b", "");

			// tokenize input
			tokenStream = new ClassicTokenizer();
			// to lowercase
			tokenStream = new LowerCaseFilter(tokenStream);
			// remove dots from acronyms (and "'s" but already done manually
			// above)
			tokenStream = new ClassicFilter(tokenStream);
			// convert any char to ASCII
			tokenStream = new ASCIIFoldingFilter(tokenStream);
			
			List<String> stopwords=new ArrayList<String>();
			
			stopwords.add("a");
			stopwords.add("the");
			// remove english stop words
			tokenStream = new StopFilter(tokenStream,StopFilter.makeStopSet(stopwords, false));
			//new StopFileter()

			//tokenStream = new StopFilter()
			List<Keyword> keywords = new LinkedList<Keyword>();
			CharTermAttribute token = tokenStream.getAttribute(CharTermAttribute.class);
			tokenStream.reset();
			while (tokenStream.incrementToken()) {
				String term = token.toString();
				// stem each term
				String stem = stem(term);
				if (stem != null) {
					// create the keyword or get the existing one if any
					Keyword keyword = find(keywords, new Keyword(stem.replaceAll("-0", "-")));
					// add its corresponding initial token
					keyword.add(term.replaceAll("-0", "-"));
				}
			}

			// reverse sort by frequency
			Collections.sort(keywords);

			return keywords;

		} finally {
			if (tokenStream != null) {
				tokenStream.close();
			}
		}

	}
	
	public static String getAllContent(List<Keyword> keywords)
	{
		
		StringBuilder strbuilder=new StringBuilder();
		
		for(int in=0;in<keywords.size();in++)
		{
			strbuilder.append(keywords.get(in).getStem());
			strbuilder.append(" ");			
		}
		
		return strbuilder.toString();
		
	}

}
