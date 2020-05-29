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
import java.util.regex.*;
import java.lang.IllegalArgumentException;

/* This class implements a stemmer, which converts words into their roots. Very
   important in text processing, it allows code to handle different variants of
   a word as though they were the same, leading to cleaner results. It also
   reduces compute, by reducing the number of dimension of the word-space
   spanned by documents.

   Many different stemmer algorithms exist. All have different tradeoffs
   between speed, false positives, and false negatives. This code implements
   the Porter algorithm, the most widely used. Its quite fast and resonably
   accurate.

   The Porter algorithm works based on pattern recognition. It looks for words
   matching certain patterns, and then manipulates the word based on the
   results. The matching is done in multiple phases, so a word may be
   manipulated multiple times. The goal is to remove all suffuxes, leaving just
   the root. The root may not be an actual English word, which does not affect
   subsequent clustering as long as it is consistent.

   The original paper is available at:
   http://tartarus.org/martin/PorterStemmer/def.txt

   This class contains lots of pre-compiled regular expressions, so its
   implemented as a singleton */
public final class PorterStemmer
{
    // The singleton
    private static PorterStemmer _stemmer = null;

    // Pre-compiled regular expressions
    Pattern _syllableStart = null;
    Pattern _ySyllableStart = null;
    Pattern _sses = null;
    Pattern _ies = null;
    Pattern _s = null;
    Pattern _eed = null;
    Pattern _ed = null;
    Pattern _yed = null;
    Pattern _ing = null;
    Pattern _ying = null;
    Pattern _at = null;
    Pattern _bl = null;
    Pattern _iz = null;
    Pattern _cvc = null;
    Pattern _cyc = null;
    Pattern _double = null;
    Pattern _y = null;
    Pattern _yy = null;
    Pattern _ational = null;
    Pattern _tional = null;
    Pattern _enci = null;
    Pattern _anci = null;
    Pattern _izer = null;
    Pattern _abli = null;
    Pattern _alli = null;
    Pattern _entli = null;
    Pattern _eli = null;
    Pattern _ousli = null;
    Pattern _ization = null;
    Pattern _ation = null;
    Pattern _ator = null;
    Pattern _alism = null;
    Pattern _iveness = null;
    Pattern _fulness = null;
    Pattern _ousness = null;
    Pattern _aliti = null;
    Pattern _iviti = null;
    Pattern _biliti = null;
    Pattern _icate = null;
    Pattern _ative = null;
    Pattern _alize = null;
    Pattern _iciti = null;
    Pattern _ical = null;
    Pattern _ful = null;
    Pattern _ness = null;
    Pattern _al = null;
    Pattern _ance = null;
    Pattern _ence = null;
    Pattern _er = null;
    Pattern _ic = null;
    Pattern _able = null;
    Pattern _ible = null;
    Pattern _ant = null;
    Pattern _ement = null;
    Pattern _ment = null;
    Pattern _ent = null;
    Pattern _sion = null;
    Pattern _ou = null;
    Pattern _ism = null;
    Pattern _ate = null;
    Pattern _iti = null;
    Pattern _ous = null;
    Pattern _ive = null;
    Pattern _ize = null;
    Pattern _e = null;
    Pattern _ll = null;
    Pattern _cvce = null;
    Pattern _cyce = null;
    
    // Getter. If not created, create it, then return it
    // WARNING: NOT THREADSAFE!
    public static PorterStemmer getStemmer() throws Exception
    {
        if (_stemmer == null) {
            try {
                _stemmer = new PorterStemmer();
            }
            catch (Exception e) {
                // Delete partially formed object to ensure consistency
                _stemmer = null;
                throw e;
            }
        } // Singleton not already created
        return _stemmer;
    }

    public PorterStemmer()
    {
        /* The regular expression patterns used by the stemmer are built from
           shorter building blocks. Define those, then define the patterns
           and compile them */
        String letter = new String("[a-z]");
        String vowel = new String("aeiou"); 
        String anyVowel = new String("[" + vowel + "]"); // Match any vowel
        /* What this means: any letter except a vowel. The '&&[^' means
           "subtract all items from the inner set from the current set"
           (technically, it means "create a set of everything but those letters
           and find the intersection", which is equivalent to a set
           difference) */
        String consanant = new String("[" + letter + "&&[^" + vowel + "]]");

        /* Special handling for Y: when surrounded by different consenants, it
           acts as a vowel */
        String yConsanant = new String("[" + consanant + "&&[^y]]"); // Consanants without Y

        /* These next four are complex. They match anything containing at least
           one vowel. Can't just search for the vowel because the entire string
           except for the ending needs to be captured for processing. The '*'
           after the two letter variables means "match any number of times that
           makes the pattern work". */
        String containsVowel = letter + "*" + anyVowel + letter + "*";
        String containsYvowel = letter + "*" + yConsanant + "y" + letter + "*";

        // Pattern blocks used to define the start of syllables
        _syllableStart = Pattern.compile(anyVowel + consanant);
        _ySyllableStart = Pattern.compile(yConsanant + "y" + yConsanant);

        /* Suffixes. The parantheses around the first part of some of the
           definitions capture that part, so it can be inserted into the final
           result */

        // Plurals
        _sses = Pattern.compile("sses$");
        _ies = Pattern.compile("ies$");
        _s = Pattern.compile("([" + letter + "&&[^s]])s$"); // s preceeded by anything but s

        // Verb tenses
        _eed = Pattern.compile("eed$");
        _ed = Pattern.compile("(" + containsVowel + ")ed$");
        _yed = Pattern.compile("(" + containsYvowel + ")ed$");
        _ing = Pattern.compile("(" + containsVowel + ")ing$");
        _ying = Pattern.compile("(" + containsYvowel + ")ing$");
        _y = Pattern.compile("(" + containsVowel + ")y$");
        _yy = Pattern.compile("(" + containsYvowel + ")y$");
        _at = Pattern.compile("at$");
        _bl = Pattern.compile("bl$");
        _iz = Pattern.compile("iz$");
        // Any consanant, any vowel, any consanant except w, x, y
        _cvc = Pattern.compile(consanant + anyVowel + "[" + consanant + "&&[^wxy]]");
        _cyc = Pattern.compile(yConsanant + "y[" + consanant + "&&[^wxy]]");
        
        /* A tricky one: any double consanant except l, s, z. The parenthesis
           capture each letter, so one can be inserted back into the output */
        _double = Pattern.compile("([" + consanant + "&&[^lsz]]){2,}");
       
        // Adjectives and adverbs
        _ational = Pattern.compile("ational$");
        _tional = Pattern.compile("tional$");
        _enci = Pattern.compile("enci$");
        _anci = Pattern.compile("anci$");
        _izer = Pattern.compile("izer$");
        _abli = Pattern.compile("abli$");
        _alli = Pattern.compile("alli$");
        _entli = Pattern.compile("entli$");
        _eli = Pattern.compile("eli$");
        _ousli = Pattern.compile("ousli$");
        _ization = Pattern.compile("ization$");
        _ation = Pattern.compile("ation$");
        _ator = Pattern.compile("ator$");
        _alism = Pattern.compile("alism$");
        _iveness = Pattern.compile("iveness$");
        _fulness = Pattern.compile("fulness$");
        _ousness = Pattern.compile("ousness$");
        _aliti = Pattern.compile("aliti$");
        _iviti = Pattern.compile("iviti$");
        _biliti = Pattern.compile("biliti$");
        _icate = Pattern.compile("icate$");
        _ative = Pattern.compile("ative$");
        _alize = Pattern.compile("alize$");
        _iciti = Pattern.compile("iciti$");
        _ical = Pattern.compile("ical$");
        _ful = Pattern.compile("ful$");
        _ness = Pattern.compile("ness$");
        _al = Pattern.compile("al$");
        _ance = Pattern.compile("ance$");
        _ence = Pattern.compile("ence$");
        _er = Pattern.compile("er$");
        _ic = Pattern.compile("ic$");
        _able = Pattern.compile("able$");
        _ible = Pattern.compile("ible$");
        _ant = Pattern.compile("ant$");
        _ement = Pattern.compile("ement$");
        _ment = Pattern.compile("ment$");
        _ent = Pattern.compile("ent$");
        _sion = Pattern.compile("([st])ion$");
        _ou = Pattern.compile("ou$");
        _ism = Pattern.compile("ism$");
        _ate = Pattern.compile("ate$");
        _iti = Pattern.compile("iti$");
        _ous = Pattern.compile("ous$");
        _ive = Pattern.compile("ive$");
        _ize = Pattern.compile("ize$");

        // Stem cleanup after suffuxes
        _e = Pattern.compile("e$");
        _ll = Pattern.compile("ll$");
        // Any consanant, any vowel, any consanant except w, x, y, then e
        _cvce = Pattern.compile(consanant + anyVowel + "[" + consanant + "&&[^wxy]]e$");
        _cyce = Pattern.compile(yConsanant + "y[" + consanant + "&&[^wxy]]e$");
    }

    // Returns the location of the second through fourth syllables in a word
    private int[] getSyllables(String word) throws Exception
    {
        /* Certain stemming operations depend on the number of syllables a word
           will have after the stemming operation. The locations are calculated
           so they only need to be done once; comparing the wanted syllable to
           the overall length will show whether it would survive the stemming.
           Only four are extracted because the stemmer only cares about that
           many at the most. It also doesn't include the first syllable, but its
           location should be obvious.

           A syllable here is defined as a consanant proceeded by a vowel. This
           does not match up with the linguistic defintion but works well enough
           for the stemmer. The letter 'y' can be either a constant or a vowel,
           depending on context. This code searches treating it as BOTH, and
           needs to match up the results of the two searches

           NOTE: End of matched chars is given as the first char NOT matched;
           need to subtract one to get the actual last char */
        Matcher syllable = _syllableStart.matcher(word);
        Matcher ySyllable = _ySyllableStart.matcher(word);

        int[] result = new int[3];
        int resultCount = 0;
        boolean foundSyllable = syllable.find();
        boolean foundYsyllable = ySyllable.find();
        while ((resultCount < result.length) &&
               (foundSyllable || foundYsyllable)) {
            if (!foundYsyllable) { // Found regular syllable only, most likely
                result[resultCount] = syllable.end() - 1;
                resultCount++;
                foundSyllable = syllable.find();
            }
            else if (!foundSyllable) { // Found syllable with Y vowel only
                result[resultCount] = ySyllable.end() - 1;
                resultCount++;
                foundYsyllable = ySyllable.find();
            }
            // Have both, take earlier
            else if (syllable.end() < ySyllable.end()) {
                result[resultCount] = syllable.end() - 1;
                resultCount++;
                foundSyllable = syllable.find();
            }
            else {
                result[resultCount] = ySyllable.end() - 1;
                resultCount++;
                foundYsyllable = ySyllable.find();
            }
        } // While still string to search and reason to do so
        return result;
    }

    /* Tests a word for a series of suffixes. The first one that matches beyond
       the specified position is replaced as given in the second list. If none
       match, the word is returned unedited */
    private String replaceSuffix(String word, int reqStemLength, 
                                 ArrayList<Pattern> suffixes,
                                 ArrayList<String> replacements) throws Exception
    {
        /* If no data is specified, or the length of the two lists does not
           match, have a serious problem */
        if ((word == null) || (suffixes == null) || (replacements == null))
            throw new IllegalArgumentException();
        if (suffixes.size() != replacements.size())
            throw new IllegalArgumentException();

        Matcher matcher = suffixes.get(0).matcher(word);
        int index = 0;
        boolean haveMatch = false;
        while ((!haveMatch) && (index < suffixes.size())) {
            if (matcher.find())
                haveMatch = (matcher.start() > reqStemLength);
            if (!haveMatch) {
                index++;
                // Set up for next pass
                if (index < suffixes.size())
                    matcher.usePattern(suffixes.get(index));
            } // No match so far
        } // No match and still suffixes to check
        if (haveMatch) {
            StringBuffer result = new StringBuffer();
            matcher.appendReplacement(result, replacements.get(index));
            word = result.toString();
        } // Found a suffix match
        return word;
    }
            
    // Returns the stem of the passed word
    public String getStem(String word) throws Exception
    {
        /* This code implements the classic Porter stemmer. For each step,
           apply patterns in order until one is matched, then replace as
           needed.
           OPTIMIZATION: Since this code deals with suffixes only, words that
           could potentially need one type of manipulation will not qualify for
           any other type of manipulation for the same step. This allows
           very convenient branching based on the final chars of a word.
           NOTE: This code uses LOTS of tests on the output buffer to check
           whether a result already exists from the previous tests. I chose
           this over nested if...then..else statements because its easier
           to edit and looks cleaner */           

        int [] syllables = getSyllables(word);
        
        StringBuffer result = new StringBuffer();
        Matcher matcher = _sses.matcher(word);

        /* Convert plural to singular. WARNING: Not all words that end in 's'
           are plural */
        if (word.charAt(word.length() - 1) == 's') {
            if (matcher.find())
                matcher.appendReplacement(result, "ss");
            if (result.length() == 0) { // Not matched
                matcher.usePattern(_ies);
                if (matcher.find())
                    matcher.appendReplacement(result, "i");
            } // Nothing matched so far
            if (result.length() == 0) {
                matcher.usePattern(_s);
                if (matcher.find())
                    /* This one is a little tricky. The pararenthesis in the
                       match pattern define a group within the match. The '$1'
                       below outputs the group into the results. The net effect
                       is that the 's' is lost while the char before it is
                       kept */
                    matcher.appendReplacement(result, "$1");
            } // Nothing matched so far
        } // String ends with 's'
        else {
            // Convert verbs to the present tense. 
            matcher.usePattern(_eed);
            if (matcher.find()) {
                /* If multiple syllables will exist after the suffix removal,
                   convert, else ignore */
                if ((syllables[0] > 0) &&
                    (syllables[0] < matcher.start()))
                    matcher.appendReplacement(result, "ee");
            }
            else {
                /* Test for verb tense conversion. If ANY succeed, then have
                   additional processing afterward */
                boolean haveTense = false;
                matcher.usePattern(_ed);
                if (matcher.find())
                    haveTense = true;
                else {
                    /* patterns that match on any number of chars but fail to
                       find anything leave the matcher at the end of the string,
                       and the next search normally starts from there. Reset
                       the matcher state before changing the pattern to deal
                       with this behavior */
                    matcher.reset();
                    matcher.usePattern(_yed);
                    if (matcher.find())
                        haveTense = true;
                    else {
                        matcher.reset();
                        matcher.usePattern(_ing);
                        if (matcher.find())
                            haveTense = true;
                        else {
                            matcher.reset();
                            matcher.usePattern(_ying);
                            if (matcher.find())
                                haveTense = true;
                        } // Not matched on 'ing'
                    } // Not matched on 'ed'
                } // Not matched on 'ed'
                if (haveTense) {
                    // Extract stem and process further
                    matcher.appendReplacement(result, "$1");
                    word = result.toString();
                    result.setLength(0);

                    /* Converting verb tense may change the stem before adding
                       the suffix. These tests reverse those changes.
                       NOTE: Word changed above, so need to recreate the
                       matcher

                       If an 'e' was dropped before adding the suffix, add
                       it back. */
                    matcher = _at.matcher(word);
                    boolean needE = false;
                    if (matcher.find())
                        needE = true;
                    else {
                        matcher.usePattern(_bl);
                        if (matcher.find())
                            needE = true;
                        else {
                            matcher.usePattern(_iz);
                            if (matcher.find())
                                needE = true;
                            /* These next two tests are tricky. The word must
                               end with the pattern 'consant-vowel-consanant'
                               and have exactly two syllables. The last letter
                               of the pattern defined the start of a syllable
                               before the suffix was removed, so the second
                               syllable location must match that spot for any
                               of these tests to pass. The third syllable must
                               fall after the end (implying it was removed with
                               the suffix) or be undefind
                               NOTE: Remember the first entry of the syllable
                               list is the SECOND syllable (the location of the
                               first is obvious) */
                            else if ((syllables[0] == (word.length() - 1)) &&
                                     ((syllables[1] == 0) ||
                                      (syllables[1] >= word.length()))) {
                                matcher.usePattern(_cvc);
                                if (matcher.find())
                                    needE = true;
                                else {
                                    matcher.usePattern(_cyc);
                                    needE = matcher.find();
                                } // Not cvc pattern
                            } // Two syllables
                        } // No match on 'bl'
                    } // No match on 'at
                    if (needE) {
                        // Insert the entire word into the buffer, add ending
                        result.append(word);
                        result.append("e");
                    } // Word needs an 'e' appended
                    else {
                        /* Check for constant doubling before the suffix was
                           added. If it exists, remove it
                           NOTE: Keep in mind that some stems have double
                           letter endings, and don't qualify here */
                        matcher.usePattern(_double);
                        if (matcher.find())
                            matcher.appendReplacement(result, "$1");
                    } // Not word with 'e' dropped before adding suffix
                } // Verb conversion to present tense
            } // Does not end in 'eed'
        } // Not a potential plural    
        
        if (result.length() > 0) // Something matched for this step
            word = result.toString();

        /* If a multisyllable word ends in a 'y', convert it to a 'i' so it
           matches the stem from the plural change above
           NOTE: Word may have changed above, so recreate the matcher */
        matcher = _y.matcher(word);
        boolean haveY = false;
        if (matcher.find())
            haveY = true;
        else {
            matcher.reset();
            matcher.usePattern(_yy);
            haveY = matcher.find();
        }
        if (haveY) {
            result.setLength(0);
            matcher.appendReplacement(result, "$1");
            result.append("i");
            word = result.toString();
        } // Multi-syllable word ending in a 'y'

        /* Remove suffixes that create adjectives and adverbs. In order to
           have a suffix, the string must have at least two syllables after
           it is removed
           OPTIMIZATION: The suffixes sort beautifully based on their second
           to last letter. Test this in the word to find the appropriate ones */
        if ((word.length() > 3) && (syllables[0] != 0)) {
            /* Create a list of patterns to check, in order, and what to
               replace them with */
            ArrayList<Pattern> suffixes = new ArrayList<Pattern>();
            ArrayList<String> replacements = new ArrayList<String>();
            
            switch (word.charAt(word.length() - 2)) {
            case 'a':
                suffixes.add(_ational);
                replacements.add(new String("ate"));
                suffixes.add(_tional);
                replacements.add(new String("tion"));
                break;
            case 'c':
                suffixes.add(_enci);
                replacements.add(new String("ence"));
                suffixes.add(_anci);
                replacements.add(new String("ance"));
                break;
            case 'e':
                suffixes.add(_izer);
                replacements.add(new String("ize"));
                break;
            case 'l':
                suffixes.add(_abli);
                replacements.add(new String("able"));
                suffixes.add(_alli);
                replacements.add(new String("al"));
                suffixes.add(_entli);
                replacements.add(new String("ent"));
                suffixes.add(_eli);
                replacements.add(new String("e"));
                suffixes.add(_ousli);
                replacements.add(new String("ous"));
                break;
            case 'o':
                suffixes.add(_ization);
                replacements.add(new String("ize"));
                suffixes.add(_ation);
                replacements.add(new String("ate"));
                suffixes.add(_ator);
                replacements.add(new String("ate"));
                break;
            case 's':
                suffixes.add(_alism);
                replacements.add(new String("al"));
                suffixes.add(_iveness);
                replacements.add(new String("ive"));
                suffixes.add(_fulness);
                replacements.add(new String("ful"));
                suffixes.add(_ousness);
                replacements.add(new String("ous"));
                break;

            case 't':
                suffixes.add(_aliti);
                replacements.add(new String("al"));
                suffixes.add(_iviti);
                replacements.add(new String("ive"));
                suffixes.add(_biliti);
                replacements.add(new String("ble"));
                break;

            default:
                // Do nothing
                break;
            } // Switch on second to last letter

            if (!suffixes.isEmpty()) // Apply the list
                word = replaceSuffix(word, syllables[0], suffixes,
                                     replacements);
        } // Word may have a suffix to remove
        
        /* More adjective and adverb suffixes, some of which may be removed
           from the stems found above
           OPTIMIZATION: Split on the last letter this time */
        if ((word.length() > 2) && (syllables[0] != 0)) {
            ArrayList<Pattern> suffixes = new ArrayList<Pattern>();
            ArrayList<String> replacements = new ArrayList<String>();

            switch (word.charAt(word.length() - 1)) {
            case 'e':
                suffixes.add(_icate);
                replacements.add(new String("ic"));
                suffixes.add(_ative);
                replacements.add(new String(""));
                suffixes.add(_alize);
                replacements.add(new String("al"));
                break;
            case 'i':
                suffixes.add(_iciti);
                replacements.add(new String("ic"));
                break;
            case 'l':
                suffixes.add(_ical);
                replacements.add(new String("ic"));
                suffixes.add(_ful);
                replacements.add(new String(""));
                break;
            case 's':
                suffixes.add(_ness);
                replacements.add(new String(""));
                break;
            default:
                // Do nothing
                break;
            } // Switch on second to last letter

            if (!suffixes.isEmpty()) // Apply the list
                word = replaceSuffix(word, syllables[0], suffixes,
                                     replacements);
        } // Word may have suffix to remove

        /* Yet more adjective and adverb suffixes, some of which may be removed
           from the stems found above. At least two syllables must remain after
           removal of the suffix
           OPTIMIZATION: Split on the second to last letter */
        if ((word.length() > 3) && (syllables[1] != 0)) {
            /* If the second to last letter is 'o', one ending needs
               special processing. Test it first; if it fails process the
               rest */
            boolean ionMatch = false;
            if (word.charAt(word.length() - 2) == 'o') {
                matcher = _sion.matcher(word);
                if (matcher.find())
                    /* First letter of match is preserved, so allow syllable
                       to start on it */
                    if (syllables[1] <= matcher.start()) {
                        result.setLength(0);
                        matcher.appendReplacement(result, "$1");
                        word = result.toString();
                        ionMatch = true;
                    } // First match char on or before second syllable
            } // Second to last letter is 'o'
            if (!ionMatch) {
                ArrayList<Pattern> suffixes = new ArrayList<Pattern>();
                ArrayList<String> replacements = new ArrayList<String>();
                switch (word.charAt(word.length() - 2)) {
                case 'a':
                    suffixes.add(_al);
                    replacements.add(new String(""));
                    break;
                case 'c':
                    suffixes.add(_ance);
                    replacements.add(new String(""));
                    suffixes.add(_ence);
                    replacements.add(new String(""));
                    break;
                case 'e':
                    suffixes.add(_er);
                    replacements.add(new String(""));
                    break;
                case 'i':
                    suffixes.add(_ic);
                    replacements.add(new String(""));
                    break;
                case 'l':
                    suffixes.add(_able);
                    replacements.add(new String(""));
                    suffixes.add(_ible);
                    replacements.add(new String(""));
                    break;
                case 'n':
                    suffixes.add(_ant);
                    replacements.add(new String(""));
                    suffixes.add(_ement);
                    replacements.add(new String(""));
                    suffixes.add(_ment);
                    replacements.add(new String(""));
                    suffixes.add(_ent);
                    replacements.add(new String(""));
                    break;
                case 'o':
                    suffixes.add(_ou);
                    replacements.add(new String(""));
                    break;
                case 's':
                    suffixes.add(_ism);
                    replacements.add(new String(""));
                    break;
                case 't':
                    suffixes.add(_ate);
                    replacements.add(new String(""));
                    suffixes.add(_iti);
                    replacements.add(new String(""));
                    break;
                case 'u':
                    suffixes.add(_ous);
                    replacements.add(new String(""));
                    break;
                case 'v':
                    suffixes.add(_ive);
                    replacements.add(new String(""));
                    break;
                case 'z':
                    suffixes.add(_ize);
                    replacements.add(new String(""));
                    break;
                default:
                    // Do nothing
                    break;
                } // Switch on second to last letter

                if (!suffixes.isEmpty()) // Apply the list
                    word = replaceSuffix(word, syllables[1], suffixes,
                                         replacements);
            } // Not a word ending with 'ion'
        } // More than two syllables in word
        
        // Clean up stems after suffix removal
        result.setLength(0);
        if ((syllables[1] > 0) && (syllables[1] < word.length())) {
            // Stem has at least three syllables
            matcher = _e.matcher(word);
            if (matcher.find())
                matcher.appendReplacement(result, "");
            else {
                matcher.usePattern(_ll);
                if (matcher.find())
                    matcher.appendReplacement(result, "l");
            } // Doesn't end with 'e'
        } // At least three syllables
        else if ((syllables[0] > 0) && (syllables[1] < word.length()) &&
                 (syllables[1] == 0)) {
            // Exactly two syllables

            /* Remove trailing 'e, unless the ending is
               consanant-vowel-consanant-e */
            matcher = _e.matcher(word);
            if (matcher.find()) { // Word ends with 'e'
                Matcher exlMatcher = _cvce.matcher(word);
                if (!exlMatcher.find()) {
                    exlMatcher.usePattern(_cyce);
                    if (!exlMatcher.find())
                        // Ending is NOT constant-vowel-consanant-e
                        /* NOTE: This is the matcher from the first test, not
                           those that came afterward! */
                        matcher.appendReplacement(result, "");
                } 
            } // Ends with 'e'
        } // Exactly two syllables
        
        if (result.length() > 0) // Something matched for this step
            word = result.toString();
        return word;
    }

    public static void main(String[] args) throws Exception
    {
        try {
            PorterStemmer test = getStemmer();

            /* Create a long list of words to test. Nearly all of them come
               from the original paper. Note that for some the word should NOT
               change. Compare the stems to the expected results, and log the
               mismatches */
            ArrayList<String> testWords = new ArrayList<String>();
            ArrayList<String> stemWords = new ArrayList<String>();
            testWords.add(new String("caresses"));
            stemWords.add(new String("caress"));
            testWords.add(new String("ponies"));
            stemWords.add(new String("poni"));
            testWords.add(new String("ties"));
            stemWords.add(new String("ti"));
            testWords.add(new String("caress"));
            stemWords.add(new String("caress"));
            testWords.add(new String("cats"));
            stemWords.add(new String("cat"));
            
            testWords.add(new String("syllables"));
            stemWords.add(new String("syllabl"));
            testWords.add(new String("feed"));
            stemWords.add(new String("feed"));
            testWords.add(new String("agreed"));
            stemWords.add(new String("agree"));
            testWords.add(new String("plastered"));
            stemWords.add(new String("plaster"));
            testWords.add(new String("bled"));
            stemWords.add(new String("bled"));
            
            testWords.add(new String("sing"));
            stemWords.add(new String("sing"));
            testWords.add(new String("flying"));
            stemWords.add(new String("fly"));
            testWords.add(new String("conflated"));
            stemWords.add(new String("conflat"));
            testWords.add(new String("troubled"));
            stemWords.add(new String("trouble"));
            testWords.add(new String("sized"));
            stemWords.add(new String("size"));

            testWords.add(new String("hopping"));
            stemWords.add(new String("hop"));
            testWords.add(new String("falling"));
            stemWords.add(new String("fall"));
            testWords.add(new String("hissing"));
            stemWords.add(new String("hiss"));
            testWords.add(new String("failing"));
            stemWords.add(new String("fail"));
            testWords.add(new String("filing"));
            stemWords.add(new String("file"));

            testWords.add(new String("sky"));
            stemWords.add(new String("sky"));
            testWords.add(new String("relational"));
            stemWords.add(new String("relat"));
            testWords.add(new String("conditional"));
            stemWords.add(new String("condit"));
            testWords.add(new String("rational"));
            stemWords.add(new String("ration"));
            testWords.add(new String("valency"));
            stemWords.add(new String("valenc"));

            testWords.add(new String("digitizer"));
            stemWords.add(new String("digit"));
            testWords.add(new String("conformably"));
            stemWords.add(new String("conform"));
            testWords.add(new String("differently"));
            stemWords.add(new String("differ"));
            testWords.add(new String("analogously"));
            stemWords.add(new String("analog"));
            testWords.add(new String("authorization"));
            stemWords.add(new String("author"));

            testWords.add(new String("predication"));
            stemWords.add(new String("predic"));
            testWords.add(new String("operator"));
            stemWords.add(new String("oper"));
            testWords.add(new String("feudalism"));
            stemWords.add(new String("feudal"));
            testWords.add(new String("decisiveness"));
            stemWords.add(new String("decis"));
            testWords.add(new String("hopefulness"));
            stemWords.add(new String("hope"));

            testWords.add(new String("callousness"));
            stemWords.add(new String("callous"));
            testWords.add(new String("formality"));
            stemWords.add(new String("formal"));
            testWords.add(new String("sensitivity"));
            stemWords.add(new String("sensit"));
            testWords.add(new String("sensibility"));
            stemWords.add(new String("sensibl"));
            testWords.add(new String("ability"));
            stemWords.add(new String("abil"));

            testWords.add(new String("triplicate"));
            stemWords.add(new String("triplic"));
            testWords.add(new String("formative"));
            stemWords.add(new String("form"));
            testWords.add(new String("formalize"));
            stemWords.add(new String("formal"));
            testWords.add(new String("electricity"));
            stemWords.add(new String("electr"));
            testWords.add(new String("electrical"));
            stemWords.add(new String("electr"));

            testWords.add(new String("revival"));
            stemWords.add(new String("reviv"));
            testWords.add(new String("allowance"));
            stemWords.add(new String("allow"));
            testWords.add(new String("inference"));
            stemWords.add(new String("infer"));
            testWords.add(new String("airliner"));
            stemWords.add(new String("airlin"));
            testWords.add(new String("adjustable"));
            stemWords.add(new String("adjust"));

            testWords.add(new String("defensible"));
            stemWords.add(new String("defens"));
            testWords.add(new String("replacement"));
            stemWords.add(new String("replac"));
            testWords.add(new String("element"));
            stemWords.add(new String("element"));
            testWords.add(new String("dependent"));
            stemWords.add(new String("depend"));
            testWords.add(new String("activate"));
            stemWords.add(new String("activ"));

            testWords.add(new String("effective"));
            stemWords.add(new String("effect"));
            testWords.add(new String("rate"));
            stemWords.add(new String("rate"));
            testWords.add(new String("cease"));
            stemWords.add(new String("ceas"));
            testWords.add(new String("controller"));
            stemWords.add(new String("control"));
            testWords.add(new String("roll"));
            stemWords.add(new String("roll"));

            int index;
            String result;
            boolean valid = true;
            for (index = 0; index < testWords.size(); index++) {
                result = test.getStem(testWords.get(index));
                if (stemWords.get(index).compareTo(result) != 0) {
                    valid = false;
                    System.out.println("Stem test failed. Word: " +
                                       testWords.get(index) +
                                       " Expected stem:" +
                                       stemWords.get(index) +
                                       " Actual stem: " + result);
                } // Mismatch to expected results
            } // Loop through test words

            if (valid)
                System.out.println("All tests passed successfully");
        }
        catch (Exception e) {
            System.out.println("Exception " + e + " caught");
            throw e; // Rethrow so improper temination is obvious
        }
    }
}