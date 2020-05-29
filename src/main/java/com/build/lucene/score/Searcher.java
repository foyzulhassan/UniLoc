package com.build.lucene.score;

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

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.tartarus.snowball.ext.PorterStemmer;

public class Searcher {
	IndexSearcher indexSearcher;
	QueryParser queryParser;
	Query query;

	public Searcher(String indexDirectoryPath) throws IOException {
		// Initialize the directory which has to be searched and attach the
		// Index
		Directory dir = FSDirectory.open(Paths.get(indexDirectoryPath));
		IndexReader reader = DirectoryReader.open(dir);
		this.indexSearcher = new IndexSearcher(reader);
		//indexSearcher.setSimilarity();

	}

	public Document getDocument(ScoreDoc scoreDoc) throws CorruptIndexException, IOException {
		return indexSearcher.doc(scoreDoc.doc);
	}

	public TopDocs search(String searchQuery) throws IOException, ParseException {
		BooleanQuery.setMaxClauseCount(80000);
		// Query the index for the search result and return the ranked results
		queryParser = new QueryParser(LuceneConstants.CONTENTS, new StandardAnalyzer());
		// queryParser.setm
		String strquery = QueryParser.escape(searchQuery);
		query = queryParser.parse(strquery);
		return indexSearcher.search(query, LuceneConstants.MAX_SEARCH);
	}

//	public TopDocs search(String searchQuery) throws IOException, ParseException {
//		// Query the index for the search result and return the ranked results
//
//		// try
//		// {
//		// queryParser = new QueryParser(LuceneConstants.CONTENTS, new
//		// StandardAnalyzer());
//		// query = queryParser.parse(searchQuery);
//		// }catch(Exception e)
//		// {
//		// e.printStackTrace();
//		// }
//		// return indexSearcher.search(query, LuceneConstants.MAX_SEARCH);
//
//		//String queryString = searchQuery;
//		BooleanQuery luceneQuery = null;
//		BooleanQuery.setMaxClauseCount(80000);
//		queryParser = new QueryParser(LuceneConstants.CONTENTS, new StandardAnalyzer());
//
//		try {
//			luceneQuery = new BooleanQuery();
//
//			// create a default match-all-questions query and add it
//			MatchAllDocsQuery madQuery = new MatchAllDocsQuery();
//			luceneQuery.add(madQuery, Occur.SHOULD);
//
//			// prepare a parser to parse the exceptions clauses
//			Query filterQuery = queryParser.parse(searchQuery);
//
//			if (filterQuery instanceof BooleanQuery) {
//				// if the parsed query contains multiple clauses, we add each
//				// one of these to our overall query individually
//				// (without specifying the Occur property - therefore
//				// keeping whatever was specified in the query string)
//				BooleanQuery parsedBooleanQuery = (BooleanQuery) filterQuery;
//				for (BooleanClause clause : parsedBooleanQuery.getClauses()) {
//					luceneQuery.add(clause);
//				}
//			} else {
//				// if the parsed query contains just a single clause, we add
//				// it to our overall query
//				luceneQuery.add(filterQuery, Occur.MUST);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		return indexSearcher.search(luceneQuery, LuceneConstants.MAX_SEARCH);
//	}

}
