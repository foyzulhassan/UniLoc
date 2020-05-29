package com.build.docsimilarity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.build.analyzer.entity.Gradlebuildfixdata;

public class DocWordFreqGenerator {

	// List of files analyzed. The results are indexed to this
	private ArrayList<String> _files;

	// The TF-IDF values of significant words, indexed by document
	private ArrayList<DocWordData> _wordMapping;

	public DocWordFreqGenerator() {
		_files = new ArrayList<String>();
		_wordMapping = new ArrayList<DocWordData>();
	}

	public ArrayList<WordFrequencyData> getTFData(Gradlebuildfixdata buildfix, boolean ispass) {

		int index = 0;
		DocumentReader reader = null;
		ArrayList<WordFrequencyData> tempResults = new ArrayList<WordFrequencyData>();

		try {
			reader = new DocumentReader();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		DocWordData results = null;
		// Get the raw word counts for the file. Skip on any exception
		try {
			Set<Map.Entry<String, WordCounter>> wordCounts = reader.getWordFrequenciesFromDB(buildfix, ispass)
					.entrySet();
			if (!wordCounts.isEmpty()) { // Found something
				/*
				 * The raw counts are normalized to augmented term frequencies
				 * so a long document does not dominate the results. The
				 * augmented frequency is roughly the word count divided by the
				 * maximum word count in the document.
				 * 
				 * Put the results in an array and sort it before doing the
				 * insert. Sorting an array is NlogN, while doing direct inserts
				 * of of unsorted data is N^2, because DocWordData does a linear
				 * search for inserts
				 */

				// Find highest count in the document
				int highestCount = 0;
				Iterator<Map.Entry<String, WordCounter>> count = wordCounts.iterator();
				while (count.hasNext()) {
					int currCount = count.next().getValue().getCount();
					if (currCount > highestCount)
						highestCount = currCount;
				} // While more entries to go through

				// Now, convert and insert
				count = wordCounts.iterator();
				while (count.hasNext()) {
					Map.Entry<String, WordCounter> rawValue = count.next();
					double termFrequency = 0.5
							+ (((double) rawValue.getValue().getCount() * 0.5) / (double) highestCount);
					tempResults.add(new WordFrequencyData(rawValue.getKey(), termFrequency));
				} // While more entries to go through
				Collections.sort(tempResults);

			} // Word counts found processing file
		} // Try block
		catch (Exception e) {
			// Any exception means the file is ignored.
			/*
			 * WARNING: The get() call can issue its own exception. The call
			 * worked above, so this code assumes it will work here also
			 */
			System.out.println("File " + _files.get(index) + " ignored due to error: " + e);
			// Ensure consistent results
			results = null;
		}

		return tempResults;
	} // While files to process
}
