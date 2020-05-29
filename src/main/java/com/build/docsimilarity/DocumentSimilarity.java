/* This file is part of DocumentCluster, a program for clustering text
   documents based on similarity. To use, specify the number of clusters
   followed by the documents, which must be located in the data subdirectory.
   Stopwords are eliminated by filtering the document contents against
   stopwords.txt in the same directory. Words are stemmed using the Porter
   Stemming algorithm. k-means clustering based on cosine similarity is used
   for the clustering.

    Copyright (C) 2013   Ezra Erb

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License version 3 as published
    by the Free Software Foundation.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    I'd appreciate a note if you find this program useful or make
    updates. Please contact me through LinkedIn or github (my profile also has
    a link to the code depository)
*/

package com.build.docsimilarity;

import java.util.*;

import com.build.analyzer.config.Config;
import com.build.analyzer.dtaccess.DBActionExecutorGradle;
import com.build.analyzer.entity.*;

import java.security.InvalidParameterException;

/* This class takes a set of documents and groups them into clusters by
   similarity, using the classic k-means clustering algorithm. Input is the
   number of clusters followed by the file names of the documents to cluster.
   The number of clusters will be a maximum of half the number of files.
   Files that contain no data will be ignored */
public final class DocumentSimilarity {
	// Files that were clustered
	private ArrayList<String> _files;

	// Analysis data of files
	private ArrayList<DocWordData> _wordMapping;

	/*
	 * Documents with insufficient words in common with other documents, or only
	 * words found in all other documents, which can't be clustered
	 */
	private HashSet<String> _notClustered;
	
	 // Constructor. Takes a file list and creates the initial data list
    public DocumentSimilarity(ArrayList<String> files) throws Exception
    {
        // Create a document copus. The significance level comes from experiment
        // NOTE: Documents that do not exist or have no data are filtered out
        CorpusData data = new CorpusData(files, 0.4);

        /* Extract the data from it. Now filter it for words that appear in too
           FEW documents for clustering. In a regular document distribution,
           they add lots of compute without affecting the results much
           WARNING: In truly disjoint documents, removing these words, as well
           as the elimination of common words done by the CorpusData object,
           may leave no data to cluster on. Extract these documents and report
           them as 'unclusterable'. They should be incredibly rare in
           practice */
        _wordMapping = data.getWordMapping();
        _files = data.getFiles();

        /* The percentage of documents that couts as 'too few' is somewhat
           arbitrary. Different papers use different cutoffs, with around a
           quarter being the median.
           WARNING: This code assumes that documents will rarely be eliminated
           due to having only the most common words, otherwise this cout will
           skew too high accordingly */
        int minDocCount = _files.size() / 4;
        // The similarity measure ignores words in only one document
        if (minDocCount < 1) 
            minDocCount = 1;

        CorpusByWord wordIndex = new CorpusByWord(_wordMapping);
        while (wordIndex.hasNext()) {
            if (wordIndex.nextWord().size() <= minDocCount)
                wordIndex.deleteProcessedWord();
        } // While words in the document to pre-process

        // Any file with no data at this point can't be clustered
        _notClustered = new HashSet<String>();
        int index = 0;
        while (index < _wordMapping.size()) {
            if (_wordMapping.get(index).isEmpty()) {
                // Document with no words to cluster
                _wordMapping.remove(index);
                _notClustered.add(_files.get(index));
                _files.remove(index);
                /* Do not increment the index, the removal moves the next
                   entry into its place */
            } // Document has no words for clustering
            else {
                /* Normalize the vector to have similarity value 1 with itself,
                   so the clustering algorithm works properly */
                _wordMapping.get(index).normalize();
                index++;
            }
        } // While documents to test
    }


	// Constructor. Takes a file list and creates the initial data list
	public DocumentSimilarity(ArrayList<String> files, List<Gradlepatch> projects) throws Exception {
		// Create a document copus. The significance level comes from experiment
		// NOTE: Documents that do not exist or have no data are filtered out
		CorpusData data = new CorpusData(files, projects, 0.4);

		/*
		 * Extract the data from it. Now filter it for words that appear in too
		 * FEW documents for clustering. In a regular document distribution,
		 * they add lots of compute without affecting the results much WARNING:
		 * In truly disjoint documents, removing these words, as well as the
		 * elimination of common words done by the CorpusData object, may leave
		 * no data to cluster on. Extract these documents and report them as
		 * 'unclusterable'. They should be incredibly rare in practice
		 */
		_wordMapping = data.getWordMapping();
		_files = data.getFiles();

		/*
		 * The percentage of documents that couts as 'too few' is somewhat
		 * arbitrary. Different papers use different cutoffs, with around a
		 * quarter being the median. WARNING: This code assumes that documents
		 * will rarely be eliminated due to having only the most common words,
		 * otherwise this cout will skew too high accordingly
		 */
		int minDocCount = 2;/// _files.size() / 4;
		// The similarity measure ignores words in only one document
		if (minDocCount < 1)
			minDocCount = 1;

		CorpusByWord wordIndex = new CorpusByWord(_wordMapping);
		// while (wordIndex.hasNext()) {
		// if (wordIndex.nextWord().size() <= minDocCount)
		// wordIndex.deleteProcessedWord();
		// } // While words in the document to pre-process

		// Any file with no data at this point can't be clustered
		_notClustered = new HashSet<String>();
		int index = 0;
		while (index < _wordMapping.size()) {
			if (_wordMapping.get(index).isEmpty()) {
				// Document with no words to cluster
				_wordMapping.remove(index);
				_notClustered.add(_files.get(index));
				_files.remove(index);
				/*
				 * Do not increment the index, the removal moves the next entry
				 * into its place
				 */
			} // Document has no words for clustering
			else {
				/*
				 * Normalize the vector to have similarity value 1 with itself,
				 * so the clustering algorithm works properly
				 */
				_wordMapping.get(index).normalize();
				index++;
			}
		} // While documents to test
	}

	// Cluster files in the object
	public ArrayList<HashSet<String>> cluster(int clusterCount, boolean traceOutput) throws Exception {
		/*
		 * This method uses the classic simple k-means algorithm for clustering.
		 * Its an iterative algorithm based on centroids. Since they are
		 * normalized, each document represents a vector within an N-dimensional
		 * sphere. Pick a number of vectors equal to the number of clusters, and
		 * sort the remainder into clusters based on which vector they are most
		 * similiar to. These form the initial partition into clusters.
		 * Averaging the vectors within each cluster gives the centroid of each
		 * cluster. Now, the vectors are repartitioned based on the centroids,
		 * again grouping them by similarity. This will give different cluster
		 * contents, more similiar than last time. Find new centroids and repeat
		 * again. The algorithm terminates when either no vector moves to
		 * another cluster, or a certain number of iterations is reached
		 * 
		 * DATA DESIGN NOTE: This code tracks the cluster number per document,
		 * because its faster then creating explicit clusters, and constantly
		 * inserting and deleting members. The actual clustes are only reported
		 * at the end
		 */

		/*
		 * Max cluster count is half the nummber of documents that can be
		 * clustered
		 */
		if (clusterCount > _files.size() / 2)
			clusterCount = _files.size() / 2;
		if (traceOutput) {
			System.out.println("Files:");
			System.out.println(_wordMapping);
			System.out.println("Clusters wanted: " + clusterCount);
		}

		ArrayList<HashSet<String>> result = new ArrayList<HashSet<String>>();
		if (clusterCount < 2)
			// Can't cluster. Put everything in one cluster and return
			result.add(new HashSet<String>(_files));
		else {
			int[] clusterMapping = new int[_files.size()];
			ArrayList<DocWordData> currClusters = new ArrayList<DocWordData>();
			/*
			 * Seed the list with documents. This assumes relatively random
			 * distribution of words between documents, otherwise the results
			 * will be skewed
			 */
			int index;
			int min = 0;
			int max = _files.size() - 1;
			Random random = new Random();

			for (index = 0; index < clusterCount; index++) {

				/////////////////// Foyzul Change///////////////////////////

				int randomNumber = random.nextInt(max - min) + min;
				currClusters.add(_wordMapping.get(randomNumber));
			}

			int iteration = 0;
			boolean docMoved = true;
			while (docMoved && (iteration < 10)) {
				docMoved = false;
				/*
				 * For each document, find the closest cluster. If not equal to
				 * the current location, move it there
				 */
				for (index = 0; index < _wordMapping.size(); index++) {
					int bestCluster = findClosestCluster(_wordMapping.get(index), currClusters);
					if (bestCluster != clusterMapping[index]) {
						clusterMapping[index] = bestCluster;
						docMoved = true;
					} // Document belongs in a different cluster
				} // For each document

				if (docMoved) {
					// Find centroids of the resulting clusters
					for (index = 0; index < currClusters.size(); index++) {
						// Assemble a vector of all documents in the cluster
						ArrayList<DocWordData> clusterDocs = new ArrayList<DocWordData>();
						int index2;
						for (index2 = 0; index2 < clusterMapping.length; index2++)
							if (clusterMapping[index2] == index)
								clusterDocs.add(_wordMapping.get(index2));
						/*
						 * If cluster has no documents, leave its centroid where
						 * it was; documents may move back to it after the
						 * centroid of their new clusters move
						 */
						if (!clusterDocs.isEmpty())
							currClusters.set(index, findCentroid(clusterDocs));
					} // For each cluster
				} // At least one document changed clusters
				if (traceOutput) {
					System.out.println("Current cluster assignments:");
					System.out.println(Arrays.toString(clusterMapping));
					System.out.println("Current cluster centroids:");
					System.out.println(currClusters);
				} // Trace output
				iteration++;
			} // While clustering can be improved and reason to do so

			// Build actual clusters from the final data
			/*
			 * NOTE: This is harder than it appears. The final number of
			 * clusters may be smaller than the number asked for, so the cluster
			 * numbers in the final array may not be consecutive. Most
			 * performant solution is another array to translate the original
			 * cluster numbers into positions in the final array set
			 */
			int[] clusterPositions = new int[clusterCount];
			Arrays.fill(clusterPositions, -1); // 0 is a valid index
			for (index = 0; index < clusterMapping.length; index++) {
				/*
				 * If given cluster number does not already exist in final
				 * results, create it and add to the mapping array
				 */
				if (clusterPositions[clusterMapping[index]] == -1) {
					clusterPositions[clusterMapping[index]] = result.size();
					result.add(new HashSet<String>());
				}
				result.get(clusterPositions[clusterMapping[index]]).add(_files.get(index));
			} // For each document in final results
		} // Files can be clustered
		return result;
	}

	// Given a document, finds the most similiar cluster
	private int findClosestCluster(DocWordData testDoc, ArrayList<DocWordData> clusters) throws Exception {
		int index;
		int bestCluster = 0;
		double bestSimilarity = testDoc.findSimilarity(clusters.get(0));
		for (index = 1; index < clusters.size(); index++) {
			double newSimilarity = testDoc.findSimilarity(clusters.get(index));
			if (newSimilarity > bestSimilarity) {
				bestSimilarity = newSimilarity;
				bestCluster = index;
			} // New cluster better than those tested so far
		} // Loop through clusters
		return bestCluster;
	}

	/*
	 * Given a collection of documents, finds their centroid. This is defined as
	 * treating each document as a vector within word space, and finding their
	 * average.
	 */
	private DocWordData findCentroid(ArrayList<DocWordData> documents) throws Exception {
		/*
		 * The documents are vectors within a space of words. Iterate through
		 * each word dimension, and average the values. If the word does not
		 * appear in a particular document, its value for that document is zero
		 */

		// If documents is empty, this fails
		CorpusByWord index = new CorpusByWord(documents);
		DocWordData result = new DocWordData();
		double docCount = (double) documents.size();
		while (index.hasNext()) {
			ArrayList<FrequencyByDoc> wordData = index.nextWord();
			int tempIndex;
			double sum = 0.0;
			for (tempIndex = 0; tempIndex < wordData.size(); tempIndex++)
				sum += wordData.get(tempIndex).getFrequency();
			result.addData(index.getProcessedWord(), sum / docCount);
		} // While word dimensions to process
		return result;
	}

	public ArrayList<HashSet<String>> cluster(int clusterCount) throws Exception {
		return cluster(clusterCount, false);
	}

	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("Files to cluster:\n");
		int index;
		for (index = 0; index < _files.size(); index++) {
			try {
				result.append(_files.get(index));
			} catch (Exception e) {
				result.append("ERROR, FILE UNDEFINED");
			}
			result.append(": ");
			try {
				result.append(_wordMapping.get(index));
			} catch (Exception e) {
				result.append("ERROR, UNDEFINED");
			}
			result.append("\n");
		} // for loop
		result.append("\n Not clusterable fles:");
		result.append(_notClustered.toString());
		return result.toString();
	}

	// Main program. Read in the specified input files and cluster them
	public static void main(String[] args) throws Exception {
		try {
			if (args.length < 3)
				System.out.println("A cluster count followed by at least two files must be specified");
			else {
				// First argument is the number of clusters
				int clusterCount = Integer.parseInt(args[0]);
				// Assemble the file list from the rest
				/*
				 * NOTE: All methods that assemble an ArrayList from an array
				 * iterate through the array, so this really is the most
				 * efficient method available
				 */
				ArrayList<String> files = new ArrayList<String>();
				int index;
				for (index = 1; index < args.length; index++)
					files.add(args[index]);
				// DocumentCluster clusterMaker = new DocumentCluster(files);
				// ArrayList<HashSet<String>> result =
				// clusterMaker.cluster(clusterCount, false);
				// System.out.println(result);
			}
		} catch (Exception e) {
			System.out.println("Exception " + e + " caught");
			throw e; // Rethrow so improper temination is obvious
		}
	}

	public static ArrayList<HashSet<String>> getSimilarity(List<Gradlepatch> projects, int clusterCount) {
		ArrayList<String> files = new ArrayList<String>();
		ArrayList<HashSet<String>> result = null;

		int index;
		for (index = 0; index < projects.size(); index++)
			files.add(Integer.toString(index));

		DocumentSimilarity clusterMaker;
		try {
			clusterMaker = new DocumentSimilarity(files, projects);
			result = clusterMaker.cluster(clusterCount, false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(result);

		return result;

	}
	
	
	// get similarity value
	public double findSimilarityVal(ArrayList<String> files) throws Exception {

		double newSimilarit = 0.0;

		DocWordData testDoc = _wordMapping.get(_files.size() - 1);		

		for (int index = 0; index < _files.size() - 1; index++) {
			newSimilarit = testDoc.findSimilarity(_wordMapping.get(index));

		} // Loop through clusters

		return newSimilarit;
	}
	
	// Given a document, finds the most similiar cluster
	public Map<String, Double> findSimilarityMap(ArrayList<String> files) throws Exception {		

		DocWordData testDoc = _wordMapping.get(_files.size() - 1);
		Map<String, Double> simmap = new HashMap<String, Double>();

		for (int index = 0; index < _files.size() - 1; index++) {
			double newSimilarity = testDoc.findSimilarity(_wordMapping.get(index));

			simmap.put(files.get(index), newSimilarity);

		} // Loop through clusters


		Map<String, Double> sortedsimmap = sortByValue(simmap);

		
		return sortedsimmap;
	}
	
	
	

	// Given a document, finds the most similiar cluster
	private String findNSimilarCluster(List<Gradlepatch> trainprojects) throws Exception {
		String simtext = "";

		DocWordData testDoc = _wordMapping.get(_files.size() - 1);
		Map<Integer, Double> simmap = new HashMap<Integer, Double>();

		for (int index = 0; index < _files.size() - 1; index++) {
			double newSimilarity = testDoc.findSimilarity(_wordMapping.get(index));

			simmap.put(index, newSimilarity);

		} // Loop through clusters

		System.out.println(_files.size());
		System.out.println(_wordMapping.size());
		Map<Integer, Double> sortedsimmap = sortByValue(simmap);

		ArrayList<Integer> keys = new ArrayList<Integer>(sortedsimmap.keySet());
		int counter = 0;
		int i = keys.size() - 1;
		boolean flag=false;
		
		while (i >= 0) {

			if (flag==false)
			{
				simtext = simtext + trainprojects.get(keys.get(i)).getRow();
				flag=true;
			}
			else
			{
				simtext = simtext + "#" + trainprojects.get(keys.get(i)).getRow();
			}

			counter++;
			i--;

			if (counter >= Config.topNSimilarity)
				break;
		}

		return simtext;
	}

	public static void genrateSimilarity() {
		DBActionExecutorGradle dbexec = new DBActionExecutorGradle();

		List<Gradlepatch> projects = dbexec.getRowsWithPatch();

		List<Gradlepatch> trainprojects = new ArrayList<Gradlepatch>();
		List<Gradlepatch> testprojects = new ArrayList<Gradlepatch>();

		for (int index = 0; index < projects.size(); index++) {
			if (index < Config.trainCount) {
				trainprojects.add(projects.get(index));
			} else {
				testprojects.add(projects.get(index));
			}
		}

		for (int index = 0; index < testprojects.size(); index++) {

			List<Gradlepatch> temptrainprojects = new ArrayList<Gradlepatch>();
			temptrainprojects.addAll(trainprojects);
			temptrainprojects.add(testprojects.get(index));

			ArrayList<String> files = new ArrayList<String>();

			for (int findex = 0; findex < temptrainprojects.size(); findex++)
				files.add(Integer.toString(findex));

			DocumentSimilarity clusterMaker;
			try {
				clusterMaker = new DocumentSimilarity(files, temptrainprojects);
				String result = clusterMaker.findNSimilarCluster(temptrainprojects);

				testprojects.get(index).setPatchParent(result);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// remove the test row;
			//trainprojects.remove(trainprojects.size() - 1);
			temptrainprojects.clear();
		}
		
		dbexec.updateBatchExistingRecord(testprojects);

	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> unsortMap) {

		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(unsortMap.entrySet());

		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return -(o1.getValue()).compareTo(o2.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}

		return result;

	}

}