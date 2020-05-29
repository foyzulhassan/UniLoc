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
import java.security.InvalidParameterException;
import java.lang.IndexOutOfBoundsException;

/* This file represents data about words within a single document. Logically,
   it represents one row within a sparse matrix. Operations must be able to
   manipulate it and combine it with data for other documents efficiently. Its
   implemented as a linked list, a standard design for this data structure.
      The structure can be accessed one of two ways: by indexes and by word
   lookup. The former is significantly faster thanks to the need for the later
   to perform a linear search. This means that the fastest way to manipulate
   two of these objects is to scan both of them simultaneously. The class
   enforces a sort order on the words to ensure simuntaneous scans, which depend
   on matching word order, work properly */
public final class DocWordData
{
    private LinkedList<WordFrequencyData> _data;

    // Constructor
    public DocWordData()
    {
        _data = new LinkedList<WordFrequencyData>();
    }

    /* Add a new word and its data. If it already exists, the data is
       overwritten. Entries are sorted so order is consistent across objects */
    public void addData(String word, double data) throws Exception
    {
        addData(new WordFrequencyData(word, data));
    }

    public void addData(WordFrequencyData newData)
    {
        // If the list is empty, just insert
        if (_data.isEmpty())
            _data.add(newData);
        // Otimization: Often, words will be inserted in order
        else if (_data.getLast().compareTo(newData) < 0)
            _data.addLast(newData);
        else {
            // Hunt for where the new entry belongs
            int index = 0;
            while ((index < _data.size()) &&
                   (_data.get(index).compareTo(newData) < 0))
                index++;
            if (index >= _data.size()) // Scanned the entire list
                _data.addLast(newData);
            else if (_data.get(index).compareTo(newData) == 0)
                // Overwrite
                _data.set(index, newData);
            else
                // Have data just beyond wanted position. Insert it here
                _data.add(index, newData);
        }
    }

    // Gets the value for a given word. If not present, the value is zero
    public double getData(String entry) throws Exception
    {
        /* Convert the string to a data entry and search for it. Remember that
           entries match on the string only */
        int index = _data.indexOf(new WordFrequencyData(entry, 0.0));
        if (index < 0)
            return 0.0;
        else
            return _data.get(index).getData();
    }

    /* Gets the value for a given index. Main use is scanning the list like an
       iterator */
    public double getData(int index) throws Exception
    {
        if ((index < 0) || (index >= _data.size()))
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds (" + 0 + " - " + (_data.size() - 1) + ")");
        return _data.get(index).getData();
    }

    // Gets the word represented by a given index
    public String getWordForIndex(int index) throws Exception
    {
        if ((index < 0) || (index >= _data.size()))
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds (" + 0 + " - " + (_data.size() - 1) + ")");
        return _data.get(index).getWord();
    }
    
    // Scales the given item by the specified amount. 
    public void scaleData(int index, double scaleAmt) throws Exception
    {
        if ((index < 0) || (index >= _data.size()))
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds (" + 0 + " - " + (_data.size() - 1) + ")");
        WordFrequencyData oldData = _data.get(index);
        _data.set(index, new WordFrequencyData(oldData.getWord(),
                                               oldData.getData() * scaleAmt));
    }

    // Remove data at a given index
    public void removeData(int index) throws Exception
    {
        if ((index < 0) || (index >= _data.size()))
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds (" + 0 + " - " + (_data.size() - 1) + ")");
        _data.remove(index);
    }

    // Remove data for a given word, if present
    public void removeData(String word) throws Exception
    {
        /* Convert the string to a data entry and search for it. Remember that
           entries match on the string only */
        int index = _data.indexOf(new WordFrequencyData(word, 0.0));
        if (index >= 0)
            _data.remove(index);
    }

    // Finds the similarity between this document and some other document
    /* WARNING: This method assumes that both document values have been
       normalized, so the similiarity of a document with itself equals 1 */
    public double findSimilarity(DocWordData other)
    {
        /* Many methods of calculating similarity exist. The code below uses
           cosine similarity, a widely used measure that can be calculated
           quickly. It treats the two documents as vectors within a space of
           word dimensions, and finds the cosine of the angle between them.
           The result falls between 0 and 1, with higher values being better.

           The cosine can be calcuated with a dot product:
           cos (angle) = AB /(|A||B|)
           Both vectors are normalized, so the second term equals 1.

           Design note: One way to calculating the dot product is to create a
           corpus with the two documents, initialize a CorpusByWord object on
           it, and then use it to iterate through the combined words. That's
           a lot of compute for two documents, so this code does the iteration
           directly instead */
        int index1 = 0;
        int index2 = 0;
        double result = 0.0;
        while ((index1 < _data.size()) && (index2 < other._data.size())) {
            /* Comparison function between words. If one less than the other,
               advance that index, otherwise find product of frequencies, add
               to total, increment both */
            int compare = _data.get(index1).compareTo(other._data.get(index2));
            if (compare < 0) // This word not in other, sorts before it
                index1++;
            else if (compare > 0) // Other word not in this, sorts before it
                index2++;
            else { // Match
                result += (_data.get(index1).getData() *
                           other._data.get(index2).getData());
                index1++;
                index2++;
            } // Current word for both documents match
        } // While words in both documents to process
        return result;
    }

    /* Normalizes a set of document frequencies, defined as the similarity of
       the document with itself equals one */
    public void normalize() throws Exception
    {
        // First, find the current similarity value
        double similarity = findSimilarity(this);
        /* The similarity calculations multiples each term with itself, so to
           normalize, scale each term by the inverse of the square root */
        similarity = Math.sqrt(similarity);
        similarity = 1.0 / similarity;
        int index;
        for (index = 0; index < size(); index++)
            scaleData(index, similarity);
    }

    // Returns the number of strings with values defined
    public int size()
    {
        return _data.size();
    }
    
    // Returns true if no strings have values defined
    public boolean isEmpty()
    {
        return _data.size() == 0;
    }

    public String toString()
    {
        return _data.toString();
    }

    // Test program. Create an object, manipulate it, and output it
    public static void main(String[] args) throws Exception
    {
        DocWordData test = new DocWordData();
        test.addData("test1", 3.5);
        test.addData("test2", 2.6);
        test.addData("test4", 1.1);
        System.out.println(test);
        // Out of order insert
        test.addData("test3", 5.1);
        System.out.println(test);

        System.out.println("Value for test3:" + test.getData("test3"));
        System.out.println("Value for foobar:" + test.getData("foobar"));
        System.out.println("Word for position 1:" + test.getWordForIndex(1));
        System.out.println("Value for position 0:" + test.getData(0));
        test.scaleData(1, -1.1);
        System.out.println(test);

        test.removeData("test2");
        test.removeData("foobar");
        test.removeData(0);
        System.out.println(test);

        /* Create two documents and find their similarity. Remember they need to
           be normalized */
        DocWordData test2 = new DocWordData();
        test2.addData("test1", 0.6);
        test2.addData("test3", 0.8);

        DocWordData test3 = new DocWordData();
        test3.addData("test2", 0.92307692);
        test3.addData("test3", 0.38461538);

        System.out.println("Similarity is: " + test2.findSimilarity(test3));

        // Create a test data and normalize it. End result matches test2 above
        DocWordData test4 = new DocWordData();
        test4.addData("test6", 3.0);
        test4.addData("test8", 4.0);
        test4.normalize();
        System.out.println("Normalized doc data: " + test4);
    }
}