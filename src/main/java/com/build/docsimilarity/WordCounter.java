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

/* Primivite types can not be used in generic types in Java. Using wrap classes
   works but is inefficient because they are immutable. The 'mutable wrap'
   pattern (some consider it an anti-pattern) solves the problem. This one is
   specialized to counting something: initialized at zero and can only
   increment it one at at a time */
public final class WordCounter
{
    private int _value;

    public WordCounter()
    {
        _value = 0;
    }

    public void increment()
    {
        _value++;
    }

    public int getCount()
    {
        return _value;
    }

    public String toString()
    {
        return String.valueOf(_value);
    }
}