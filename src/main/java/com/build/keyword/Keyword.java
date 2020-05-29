package com.build.keyword;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Keyword implements Comparable<Keyword> {

	  private final String stem;
	  private final Set<String> terms = new HashSet<String>();
	  private int frequency = 0;

	  public Keyword(String stem) {
	    this.stem = stem;
	  }

	  public void add(String term) {
	    terms.add(term);
	    frequency++;
	  }

	  @Override
	  public int compareTo(Keyword o) {
	    // descending order
	    return Integer.valueOf(o.frequency).compareTo(frequency);
	  }

	  @Override
	  public boolean equals(Object obj) {
	    if (this == obj) {
	      return true;
	    } else if (!(obj instanceof Keyword)) {
	      return false;
	    } else {
	      return stem.equals(((Keyword) obj).stem);
	    }
	  }

	  @Override
	  public int hashCode() {
	    return Arrays.hashCode(new Object[] { stem });
	  }

	  public String getStem() {
	    return stem;
	  }

	  public Set<String> getTerms() {
	    return terms;
	  }

	  public int getFrequency() {
	    return frequency;
	  }

	}