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

public class CosineSimilarityGenerator {
	public Map<String, Double> getCosineSimilarityMap(List<String> addedfiles,List<String> repofiles) {
		
		Map<String, Double> masterSim = new HashMap<>();
		
		VectorGenerator vectorGenerator = null;
		try {
			vectorGenerator = new VectorGenerator();
			vectorGenerator.GetAllTerms();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		DocVector[] docVector = null;
		try {
			docVector = vectorGenerator.GetDocumentVectors();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // getting
																		// document
																		// vectors

		int lastdoc=docVector.length-1;
		for (int i = 0; i < (docVector.length-1); i++) {
			double cosineSimilarity = CosineSimilarity.CosineSimilarity(docVector[i], docVector[lastdoc]);
			masterSim.put(addedfiles.get(i), cosineSimilarity);
		}
		
		for (String strfile : repofiles) {
			if (!masterSim.containsKey(strfile)) {
				masterSim.put(strfile, 0.0);
			}
		}
		
		return masterSim;
	}

}
