package com.build.lucene.score;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

/**
 * Class to generate Document Vectors from Lucene Index
 * @author Mubin Shrestha
 */
public class VectorGenerator {
    DocVector[] docVector;
    private Map allterms;
    Integer totalNoOfDocumentInIndex;
    IndexReader indexReader;
    
    public VectorGenerator() throws IOException
    {
        allterms = new HashMap<>();
        indexReader = IndexOpener.GetIndexReader();
        totalNoOfDocumentInIndex = IndexOpener.TotalDocumentInIndex();
        docVector = new DocVector[totalNoOfDocumentInIndex];
    }
    
    public void GetAllTerms() throws IOException
    {
        AllTerms allTerms = new AllTerms();
        allTerms.initAllTerms();
        allterms = allTerms.getAllTerms();
    }
    
    public DocVector[] GetDocumentVectors() throws IOException {
        for (int docId = 0; docId < totalNoOfDocumentInIndex; docId++) {
            Terms vector = indexReader.getTermVector(docId, LuceneConstants.CONTENTS);
            TermsEnum termsEnum = null;
            termsEnum = vector.iterator(termsEnum);
            BytesRef text = null;            
            docVector[docId] = new DocVector(allterms);            
            while ((text = termsEnum.next()) != null) {
                String term = text.utf8ToString();
                int freq = (int) termsEnum.totalTermFreq();
                docVector[docId].setEntry(term, freq);
            }
            docVector[docId].normalize();
        }        
        indexReader.close();
        return docVector;
    }
}