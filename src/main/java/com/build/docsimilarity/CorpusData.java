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

import com.build.analyzer.entity.*;

import java.security.InvalidParameterException;

/* This class takes a list of documents, the corpus to analyze, and returns the
   document-word matrix of TF-IDF values for each significant word. The matrix
   is sparse, so it is represented as an array of linked lists. The lists are
   sorted to ensure the words are in the same order for each document

   TF-IDF stands for 'term frequency - inverse document frequency', a very
   common measurement value in text analysis. It takes the normalized frequency
   of a worde in a document and divides it by a measure of how many documents it
   appears in. The higher the value, the more significant a word is for
   processing purposes */
/* Data design overview: The data structures for this class are complicated.
   Downstream code needs to know values about the significant words in each
   document, so the logical structure is a matrix with documents as the rows
   and the words as the columns.
       Very few words appear in all documents, so the resulting matrix is
   sparse. Sparse matrixes implemented with regular matrix data structures are
   both inefficient and memory hogs. The solution is to collapse one dimension
   or the other to a list of index value - cell value pairs. Different designs
   for doing this exist, each of which has diffferent tradeoffs for different
   matrix operations. This class uses a data structure with collapsed columns,
   so it is indexed by document. Accessing it by word is complicated enough to
   have a seperate class for it.
       The other decision to make is whether to extract data from the files
   sorted in word order, or to sort it afterward. Sorting it afterward is
   significantly cheaper because many words appear in each document multiple
   times, and unsorted words can be accessed with a hashtable. */
public final class CorpusData
{
    // List of files analyzed. The results are indexed to this
    private ArrayList<String> _files;

    // The TF-IDF values of significant words, indexed by document
    private ArrayList<DocWordData> _wordMapping;

    /* Constructor. Needs the files and the minimum TF-IDF value to be
       considered significant. It varies with how documents were written, so no
       single cutoff applies to all sets */
    public CorpusData(ArrayList<String> files,List<Gradlepatch> projects, double minSignificantValue) throws Exception
    {
        if (files.size() < 2)
            throw new InvalidParameterException("Files to analyze word frequencies invalid, " + files + ". At least two must be specified");

        _files = files;
        _wordMapping = new ArrayList<DocWordData>(_files.size());

        int index = 0;
        DocumentReader reader = new DocumentReader();
        while (index < _files.size()) {
            DocWordData results = null;            
            // Get the raw word counts for the file. Skip on any exception
            try {
                Set<Map.Entry<String, WordCounter>> wordCounts = reader.getWordFrequenciesFromDB(_files.get(index),projects).entrySet();
                if (!wordCounts.isEmpty()) { // Found something
                    /* The raw counts are normalized to augmented term
                       frequencies so a long document does not dominate the
                       results. The augmented frequency is roughly the word
                       count divided by the maximum word count in the document.

                       Put the results in an array and sort it before doing
                       the insert. Sorting an array is NlogN, while doing direct
                       inserts of of unsorted data is N^2, because DocWordData
                       does a linear search for inserts */
                    ArrayList<WordFrequencyData> tempResults = new ArrayList<WordFrequencyData>();
                    
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
                        double termFrequency = 0.5 + (((double)rawValue.getValue().getCount() * 0.5) / (double)highestCount);
                        tempResults.add(new WordFrequencyData(rawValue.getKey(),
                                                              termFrequency));
                    } // While more entries to go through
                    Collections.sort(tempResults);

                    // Now insert into the actual results
                    results = new DocWordData();
                    Iterator<WordFrequencyData> tempIndex = tempResults.iterator();
                    while (tempIndex.hasNext())
                        results.addData(tempIndex.next());
                } // Word counts found processing file
            } // Try block
            catch (Exception e) {
                // Any exception means the file is ignored. 
                /* WARNING: The get() call can issue its own exception. The
                   call worked above, so this code assumes it will work here
                   also */
                System.out.println("File " + _files.get(index) + " ignored due to error: " + e);
                // Ensure consistent results
                results = null;
            }

            /* If have results, add them to the overall preliminary results,
               otherwise, delete the file from the file list */
            if (results != null) {
                _wordMapping.add(results);
                index++; // Move to next entry
            }
            else
                _files.remove(index);
            /* NOTE: Deleting the entry moves the next one into its place, so
               the index should NOT be incremented here */
        } // While files to process

        /* Iterate through the mapping word by word. The number of documents
           each word appears becomes the main input into the IDF portion of
           the frequency value, which is used to scale the TF values already
           in the results. If the average value falls below the threshold, the
           word is removed from the corpus results; it adds compute without
           affecting the final results very much */
        CorpusByWord wordIndex = new CorpusByWord(_wordMapping);
        while (wordIndex.hasNext()) {
            ArrayList<FrequencyByDoc> wordData = wordIndex.nextWord();
            // Calculate IDF from document count for word
            double invDocFreq = Math.log(((double)_wordMapping.size()) /
                                         ((double)wordData.size()));
            /* Find the average TF-IDF value for the word. The TF values are
               in the current word data, giving an average TF value, which
               multiplied by the IDF gives the average TF-IDF.
               NOTE: In therory, it makes no difference whether the average TF
               value is multiplied by the IDF value, or the individual TF-IDF
               values are found and averaged. In practice, it could make a
               difference due to rounding. This code assumes that the rounding
               error is small enough compared to the double precision to not
               worry about it */
            double avgTFIDF = 0.0;
            Iterator<FrequencyByDoc> wordTF = wordData.iterator();
            while (wordTF.hasNext())
                avgTFIDF += wordTF.next().getFrequency();
            avgTFIDF /= (double)wordData.size();
            avgTFIDF *= invDocFreq;
            
            /* Eliminate the word if below the limit, otherwise scale the values
               by the IDF value to get the final results */
            if (avgTFIDF < minSignificantValue)
                wordIndex.deleteProcessedWord();
            else
                wordIndex.scaleProcessedWord(invDocFreq);
        } // While words in the document to pre-process
    }
    
    
    
    public CorpusData(ArrayList<String> files, double minSignificantValue) throws Exception
    {
        if (files.size() < 2)
            throw new InvalidParameterException("Files to analyze word frequencies invalid, " + files + ". At least two must be specified");

        _files = files;
        _wordMapping = new ArrayList<DocWordData>(_files.size());

        int index = 0;
        DocumentReader reader = new DocumentReader();
        while (index < _files.size()) {
            DocWordData results = null;            
            // Get the raw word counts for the file. Skip on any exception
            try {
                Set<Map.Entry<String, WordCounter>> wordCounts = reader.getWordFrequencies(_files.get(index)).entrySet();
                if (!wordCounts.isEmpty()) { // Found something
                    /* The raw counts are normalized to augmented term
                       frequencies so a long document does not dominate the
                       results. The augmented frequency is roughly the word
                       count divided by the maximum word count in the document.

                       Put the results in an array and sort it before doing
                       the insert. Sorting an array is NlogN, while doing direct
                       inserts of of unsorted data is N^2, because DocWordData
                       does a linear search for inserts */
                    ArrayList<WordFrequencyData> tempResults = new ArrayList<WordFrequencyData>();
                    
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
                        double termFrequency = 0.5 + (((double)rawValue.getValue().getCount() * 0.5) / (double)highestCount);
                        tempResults.add(new WordFrequencyData(rawValue.getKey(),
                                                              termFrequency));
                    } // While more entries to go through
                    Collections.sort(tempResults);

                    // Now insert into the actual results
                    results = new DocWordData();
                    Iterator<WordFrequencyData> tempIndex = tempResults.iterator();
                    while (tempIndex.hasNext())
                        results.addData(tempIndex.next());
                } // Word counts found processing file
            } // Try block
            catch (Exception e) {
                // Any exception means the file is ignored. 
                /* WARNING: The get() call can issue its own exception. The
                   call worked above, so this code assumes it will work here
                   also */
                System.out.println("File " + _files.get(index) + " ignored due to error: " + e);
                // Ensure consistent results
                results = null;
            }

            /* If have results, add them to the overall preliminary results,
               otherwise, delete the file from the file list */
            if (results != null) {
                _wordMapping.add(results);
                index++; // Move to next entry
            }
            else
                _files.remove(index);
            /* NOTE: Deleting the entry moves the next one into its place, so
               the index should NOT be incremented here */
        } // While files to process

        /* Iterate through the mapping word by word. The number of documents
           each word appears becomes the main input into the IDF portion of
           the frequency value, which is used to scale the TF values already
           in the results. If the average value falls below the threshold, the
           word is removed from the corpus results; it adds compute without
           affecting the final results very much */
        CorpusByWord wordIndex = new CorpusByWord(_wordMapping);
        while (wordIndex.hasNext()) {
            ArrayList<FrequencyByDoc> wordData = wordIndex.nextWord();
            // Calculate IDF from document count for word
            double invDocFreq = Math.log(((double)_wordMapping.size()) /
                                         ((double)wordData.size()));
            /* Find the average TF-IDF value for the word. The TF values are
               in the current word data, giving an average TF value, which
               multiplied by the IDF gives the average TF-IDF.
               NOTE: In therory, it makes no difference whether the average TF
               value is multiplied by the IDF value, or the individual TF-IDF
               values are found and averaged. In practice, it could make a
               difference due to rounding. This code assumes that the rounding
               error is small enough compared to the double precision to not
               worry about it */
            double avgTFIDF = 0.0;
            Iterator<FrequencyByDoc> wordTF = wordData.iterator();
            while (wordTF.hasNext())
                avgTFIDF += wordTF.next().getFrequency();
            avgTFIDF /= (double)wordData.size();
            avgTFIDF *= invDocFreq;
            
            /* Eliminate the word if below the limit, otherwise scale the values
               by the IDF value to get the final results */
            if (avgTFIDF < minSignificantValue)
                wordIndex.deleteProcessedWord();
            else
                wordIndex.scaleProcessedWord(invDocFreq);
        } // While words in the document to pre-process
    }
    
    
    // Get the data in the object
    public ArrayList<String> getFiles()
    {
        return _files;
    }

    public ArrayList<DocWordData> getWordMapping()
    {
        return _wordMapping;
    }

    public String toString()
    {
        StringBuilder result = new StringBuilder();
        int index;
        for (index = 0; index < _files.size(); index++) {
            try {
                result.append(_files.get(index));
            }
            catch (Exception e) {
                result.append("ERROR, FILE UNDEFINED");
            }
            result.append(": ");
            try {
                result.append(_wordMapping.get(index));
            }
            catch (Exception e) {
                result.append("ERROR, UNDEFINED");
            }
            result.append("\n");
        } // for loop
        return result.toString();
    }

    // Test program. Read in the specified input files and output the results
    public static void main(String[] args) throws Exception
    {
        try {
            if (args.length < 2)
                System.out.println("At least two files must be specified to test");
            else {
                //CorpusData test = new CorpusData(new ArrayList<String>(Arrays.asList(args)), 0.6);
                System.out.println("test");
            }
        }
        catch (Exception e) {
            System.out.println("Exception " + e + " caught");
            throw e; // Rethrow so improper temination is obvious
        }
    }
}