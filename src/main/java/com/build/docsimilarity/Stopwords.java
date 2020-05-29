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
import java.io.*;
import java.util.*;

import com.build.analyzer.config.Config;


/* This file manages a singleton containing the list of stop words. These are
   words that are so common in documents that they are useless for English
   test analysis. No agreed on list exists; it gets tweaked by each
   application. This code deals with the issue by reading the list from a
   configuration file */
public final class Stopwords
{
    // The singleton
    private static Stopwords _words = null;

    // The actual words
    private Set<String> _wordList = null;

    /* File used to load the words. Inside the class to ensure it is always
       closed and deallocated */
    private BufferedReader _file;

    // Getter. If not created, create it, then return it
    // WARNING: NOT THREADSAFE!
    public static Stopwords getStopWords() throws Exception
    {
        if (_words == null) {
            try {
                _words = new Stopwords();
            }
            catch (Exception e) {
                // Delete partially formed object to ensure consistency
                _words = null;
                throw e;
            }
        } // Singleton not already created
        return _words;
    }

    public Stopwords() throws Exception
    {
        _wordList = new HashSet<String>();

        /* Open the data file. If not found, assume no stop words are defined.
           This is likely a misconfiguration, so log it */
        try {
            /* TRICKY NOTE: Notice the double backslash below. Java uses
               '\' as an escape character. The first is the esacpe
               character needed to insert a litteral '\' in the string! */
        	String filename=Config.workDir+"stopwords.txt";
            String fullName = new String(filename);
            _file = new BufferedReader(new FileReader(fullName));
            if (_file == null) {
                // Failed to open. 
                System.out.println("No stop words file " + fullName + " found. Assuming none defined");
            }
            else {
                // Words are specified on one line split by commas
                String [] newData = _file.readLine().split(",");
                if (newData.length <= 0)
                    // Found no data
                    System.out.println("Stop words file " + fullName + " corrupt; no data");
                else {
                    int index;
                    for (index = 0; index < newData.length; index++)
                        _wordList.add(newData[index]);
                }
                _file.close();
                _file = null;
            }
        }
        catch (Exception e) {
            // Ensure consistent state
            _wordList.clear();

            // Clean up file
            _file.close();
            _file = null;
            throw e;
        }
    }

    /** This method ensures the file is always closed before the object dies.
        In general, if the file gets to here, something has gone wrong and
        resources have been held far longer than needed. A warning is issued
        to handle this case */
    public void finailize()
    {
        if (_file != null) {
            System.out.println("WARNING: File not properly closed");
            try {
                _file.close();
            }
            /* Catch and dispose of any IO exception, since the object is going
               away soon anyway. This is normally an anti-pattern, but needed
               in this case */
            catch (IOException e) {}
            _file = null;
        }
    }

    // Output the stopwords list
    public String toString()
    {
        if (_wordList == null)
            return "NONE";
        else
            return Arrays.toString(_wordList.toArray());
    }

    public boolean isStopWord(String word)
    {
        if (_wordList == null)
            return false;
        else
            return (_wordList.contains(word));
    }

    public static void main(String[] args) throws Exception
    {
        try {
            Stopwords test = getStopWords();
            System.out.println(test);
            System.out.println("Test is a stopword:" + test.isStopWord("test"));
            System.out.println("The is a stopword:" + test.isStopWord("the"));
        }
        catch (Exception e) {
            System.out.println("Exception " + e + " caught");
            throw e; // Rethrow so improper temination is obvious
        }
    }
}