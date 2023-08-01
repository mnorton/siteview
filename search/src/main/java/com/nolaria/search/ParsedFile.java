/**
 * 
 */
package com.nolaria.search;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.PennTreebankTokenizer;


/**
 * This simple class holds information from a parsed HTML web page.
 * This follows the conventions of a Java Pea, so it doesn't have getters and setters.
 * 
 * @author markjnorton@gmail.com
 */
public class ParsedFile {
	public static final String taggerModel = "D:/Personal/SiteView/stanford-tagger-4.2.0/stanford-postagger-full-2020-11-17/models/english-left3words-distsim.tagger";

	public String pid = null;
	public String title = null;
	public Map<String,IndexableString> strings = new HashMap<String,IndexableString>();
	public List<TaggedWord> taggedWords = null;		//	All words with POS tags
	public List<String> keywords = null;			//	Words filtered by accepted POS tags.
	
	/**
	 * Process the extracted strings to find keywords.
	 * 
	 * 1. merge all strings.
	 * 2. tag all words to create the taggedWords list.
	 * 3. filter alltaggedWords by POS to create the keywords list.
	 */
	public void findKeywords() {
		//	Clean up the strings.
		this.cleanPunctuation();
		
		//	Merge all strings extracted from the HTML.
		StringBuffer allText = new StringBuffer();
		for (IndexableString is : this.strings.values()) {
			String s = is.str;
			char firstChar = s.charAt(0);
			if (firstChar == '?') System.out.println("Found a question mark.");
			/*
			if (!Character.isDigit(firstChar) && !Character.isLetter(firstChar)) {
				System.out.println("*** Non-alphabetic character found in: "+s);
				s = s.substring(1, s.length());		//	Removes leading strange character such as &nbsp;  -- not working.
			}
			*/
			allText.append(s.toLowerCase()+" ");
		}
		
		//	Create a list of tagged words.
		MaxentTagger tagger = new MaxentTagger(taggerModel);		
		PennTreebankTokenizer tokenizer = new PennTreebankTokenizer(new StringReader(allText.toString()));
		List<String> tokens  = tokenizer.tokenize();
		for (int i=0; i<10000; i++) {}
		List<Word> words = SentenceUtils.toUntaggedList(tokens);
		this.taggedWords = tagger.apply(words);
		
		//	Create a list of keywords.
		this.keywords = new Vector<String>();
		for (TaggedWord w : this.taggedWords) {
			String twWord = w.word();
			String twTag = w.tag();
			if (this.isIndexableTag(twTag)) {
				//	Add the word to the keyword list if not already included.
				if (this.keywords.indexOf(twWord) == -1)
					this.keywords.add(twWord);
			}
		}		

	}
	
	/**
	 * Return the number of tagged words found.
	 * @return number of tagged words.
	 */
	public int getTaggedWordsCount() {
		if (this.taggedWords != null)
			return this.taggedWords.size();
		return 0;
	}
	
	/**
	 * Return the number of keywords found.
	 * @return number of keywords.
	 */
	public int getKeywordsCount() {
		if (this.keywords != null)
			return this.keywords.size();
		return 0;
	}

	/**
	 * Remove punctuation in Indexable Strings in this Parsed File.
	 * TODO:  There is probably a more clever way to remove all these characters.
	 */
	private void cleanPunctuation() {
		for (IndexableString is : this.strings.values()) {
			String str = is.getStr();
			str = str.replace('.', ' ');	//	Remove periods.
			str = str.replace('?', ' ');	//	Remove question.
			str = str.replace('!', ' ');	//	Remove exclamation.
			str = str.replace(',', ' ');	//	Remove commas.
			str = str.replace(';', ' ');	//	Remove semi-colons.
			str = str.replace(':', ' ');	//	Remove colons.
			str = str.replace('-', ' ');	//	Remove hyphens.
			str = str.replace('/', ' ');	//	Remove slash.
			str = str.replace('+', ' ');	//	Remove plus.
			str = str.replace('&', ' ');	//	Remove ampersand.
			str = str.replace('*', ' ');	//	Remove asterisk.
			str = str.replace('=', ' ');	//	Remove equal sign.
			str = str.replace('"', ' ');	//	Remove double quote.
			//str = str.replace('\'', ' ');	//	Remove apostrophe. - Removing this might screw up finding possessives.
			str = str.replace('(', ' ');	//	Remove open paren.
			str = str.replace(')', ' ');	//	Remove close paren.
			str = str.replace('{', ' ');	//	Remove open brace.
			str = str.replace('}', ' ');	//	Remove close brace.
			str = str.replace('[', ' ');	//	Remove open bracket.
			str = str.replace(']', ' ');	//	Remove close bracket.
			is.setStr(str);
		}

	}
	
	/**
	 * Return true if the tag is included in the list of indexable tags, otherwise false.
	 * @param tag
	 * @return true if indexable tag
	 */
	private boolean isIndexableTag(String tag) {
		switch (tag) {
		case "JJ":		//	Adjective
		case "JJR":		//	Adjective, comparative
		case "JJS":		//	Adjective, superlative
		case "CD":		//	Cardinal number
		case "FW":		//	Foreign word
		case "NN":		//	Noun, common, singular
		case "NNS":		//	Noun, common, plural
		case "NNP":		//	Noun, proper, singular
		case "NNPS":	//	Noun, proper, plural
			return true;
		default:
			return false;
		}
	}
	
	/**
	 * Create a pretty string of the strings data.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("PID: "+this.pid+"\n");
		sb.append("Title: "+this.title+"\n");
		sb.append("\n");
		
		Set<String> keys = this.strings.keySet();
		for (String key : keys) {
			IndexableString str = this.strings.get(key);
			int level = str.getLevel();
			sb.append(level+"| "+str.getTag()+": "+str.getStr()+"\n");
		}
		sb.append("\nIndexable strings found: "+this.strings.size());
		
		return sb.toString();
	}
	
	/**
	 * Create a pretty string of tagged words.
	 * 
	 * @return string of tagged words.
	 */
	public String toTaggedString() {
		if (this.taggedWords == null)
			return "Tagged words list is null.\n";
		StringBuffer sb = new StringBuffer();
		//sb.append("Tagged words:\n");
		for (TaggedWord w : this.taggedWords) {
			System.out.println("\t"+w.word()+" - "+w.tag());
		}		
		return sb.toString();
	}
	
	/**
	 * Create a pretty string of keywords found.
	 * @return string of keywords
	 */
	public String toKeywordString() {
		if (this.keywords == null)
			return "Keywords list is null.\n";
		StringBuffer sb = new StringBuffer();
		for (String keyword : this.keywords)
			System.out.println("\t"+keyword);			
		return sb.toString();
	}
}
