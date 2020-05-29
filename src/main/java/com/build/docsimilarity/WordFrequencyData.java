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

/* This class represents data about the frequency of some word in some document.
   The value will normally be the TF-IDF value, but this is not a requirement.
   TF-IDF stands for 'term frequency - inverse document frequency', a very
   common measurement of word frequency in text mining */
public final class WordFrequencyData implements Comparable<WordFrequencyData>
{
    private String _word;
    private double _data;
    
    public WordFrequencyData(String word, double data) throws Exception
    {
        // Word must be non-NULL and non-empty
        if (word == null)
            throw new InvalidParameterException("WordFrequencyData invalid, no word specified");
        else if (word.isEmpty())
            throw new InvalidParameterException("WordFrequencyData invalid, no word specified");
        else {
            _word = word;
            _data = data;
        }
    } // Constructor
    
    // Comparitor. Compares the strings only, ignoring the data
    public int compareTo(WordFrequencyData other)
    {
        return _word.compareTo(other._word);
    }
    
    /* Equality operator. This overrides the default in Object, so it is
       MUCH more complicated than it first appears to need. */
    @Override
    public boolean equals(Object other)
    {
        if (other == null)
            return false;
        else if (other == this) // Self always matches
            return true;
        // Guard against class cast exception
        else if (!(other instanceof WordFrequencyData))
            return false; 
        return (compareTo((WordFrequencyData)other) == 0);
    }
    
    public String getWord()
    {
        return _word;
    }
    
    public double getData()
    {
        return _data;
    }
    
    public String toString()
    {
        return _word + "=" + _data;
    }
} 


