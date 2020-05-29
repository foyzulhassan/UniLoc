package com.build.lucene.score;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import com.build.analyzer.config.Config;

public class LuceneSearcing {
	String indexDir;
	String dataDir;

	SourceIndexer indexer;
	Searcher searcher;

	public LuceneSearcing() {
		this.indexDir = Config.luceneDir;
		purgeDirectory(new File(Config.luceneDir));
		try {
			indexer = new SourceIndexer();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public Map<String, Double> searchAndGetScore(List<String> searchQuery, List<String> repofiles)
			throws IOException, ParseException {
		// Map<String, Double> srcranking = new HashMap<String, Double>();
		searcher = new Searcher(indexDir);

		Map<String, Double> masterSim = new HashMap<>();
		// Query the index for the search result and return the ranked results
		QueryParser qp = new QueryParser(LuceneConstants.CONTENTS, new StandardAnalyzer());

		StringBuilder strbuilder = new StringBuilder();
		for (String strline : searchQuery) {
			String searchline = strline.replaceAll(":", " ");
			searchline = searchline.replaceAll(" and ", " ");
			searchline = searchline.replaceAll(" or ", " ");
			searchline = searchline.replaceAll(" not ", " ");
			searchline = searchline.replaceAll(" AND ", " ");
			searchline = searchline.replaceAll(" OR ", " ");
			searchline = searchline.replaceAll(" NOT ", " ");
			searchline = searchline.replaceAll("-", " ");
			searchline = searchline.replaceAll("[^a-zA-Z0-9]", " ");
			searchline = qp.escape(searchline);
			searchline = searchline.trim();
			strbuilder.append(searchline);
			strbuilder.append(" ");
		}

		if (strbuilder.toString().length() > 0) {

			TopDocs hits = searcher.search(strbuilder.toString());

			for (ScoreDoc scoreDoc : hits.scoreDocs) {
				Document doc = searcher.getDocument(scoreDoc);

				if (masterSim.containsKey(doc.get(LuceneConstants.FILE_ALIASPATH))) {
					try {
						// System.out.println(masterSim.get(LuceneConstants.FILE_ALIASPATH));
						double sim = masterSim.get(doc.get(LuceneConstants.FILE_ALIASPATH));
						double d2 = (double) scoreDoc.score;
						sim = sim + d2;

						masterSim.put(doc.get(LuceneConstants.FILE_ALIASPATH), sim);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {

					masterSim.put(doc.get(LuceneConstants.FILE_ALIASPATH), (double) scoreDoc.score);
				}
			}
		}

		for (String strfile : repofiles) {
			if (!masterSim.containsKey(strfile)) {
				masterSim.put(strfile, 0.0);
			}
		}
		
		List<Double> values = new ArrayList(masterSim.values());
		Double min=Collections.min(values);
		Double max=Collections.max(values);
		
		for(String file:masterSim.keySet())
		{
			Double value=masterSim.get(file);
			Double newval=1.0-((value - min) / (max - min));
			masterSim.put(file, newval);
		}
		

		return masterSim;
	}

	public void addToIndex(String strfile, String aliasfile) {
		try {
			indexer.addToIndex(strfile, aliasfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void closeIndexer() {
		try {
			indexer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
}
