/**
 * 
 */
package com.nolaria.sv.db;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Vector;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.PennTreebankTokenizer;


/**
 * Data for a web page in the Page Id Model.  In this model, references
 * to a page is done using it's identifier, for example:
 * 
 * http://localhost:8080/sv?site=nolaria&id=961d30bb-c47b-4908-9762-d5918d477319
 * 
 * Jul. 31, 2023:  Support added for extracting indexable keywords from a page.
 *  
 * @author markjnorton
 *
 */
public class PageId {
	//	TODO:  This is a bit of a hack, but including the model as a resource into the JAR would make it HUGE.
	public static final String TAGGER_MODEL = "D:/Personal/SiteView/stanford-tagger-4.2.0/stanford-postagger-full-2020-11-17/models/english-left3words-distsim.tagger";

	public String id;		//	A UUID that uniquely identifies this page
	public String site;		//	Web site name
	public String title;	//	Title of this page
	public String file;		//	File name of this page
	public String path;		//	File pat to this page
	
	public List<String> keywords = new Vector<String>();	// List defaults to empty.
	
	/**
	 * Constructor given all values.
	 * 
	 * @param id
	 * @param site
	 * @param name
	 * @param file
	 * @param path
	 */
	public PageId (String id, String site, String name, String file, String path) {
		this.id = id;		//	A UUID for the page.
		this.site = site;	//	Web site associated with this page.
		this.title = name;	//	Web page title.
		this.file = file;	//	File name.
		this.path = path;	//	Path to file name relative to Tomcat webapps dir.
	}

	/**
	 * Get the unique identifier of this page.
	 * @return page identifier string
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Get the site name of this page.
	 * @return site name string
	 */
	public String getSite() {
		return this.site;
	}

	/**
	 * Get the name (title) of this page.
	 * @return name string
	 */
	public String getTitle() {
		return this.title;
	}

	/**
	 * Get the file name of this page.
	 * @return file name string
	 */
	public String getFile() {
		return this.file;
	}

	/**
	 * returns the directory this file is contained in.
	 * @return directory path string
	 */
	public String getPath() {
		return this.path;
	}
	
	/**
	 * Returns the file name of the page.
	 * @return file name string
	 */
	public String getFullFileName() {
		StringBuffer sb = new StringBuffer();
		
		String tomcat = System.getenv("CATALINA_HOME");
		sb.append(tomcat+File.separator);
		sb.append("webapps"+File.separator);
		sb.append(this.site);
		if (this.path.compareTo("") == 0)
			sb.append(File.separator);
		else
			sb.append(File.separator+this.getPath()+File.separator);
		sb.append(this.file);
		
		return sb.toString();
	}
	
	/**
	 * Return the URL for this page using the Page Id model.  Such as:
	 * http://localhost:8080/sv?site=nolaria&id=961d30bb-c47b-4908-9762-d5918d477319
	 * @return URL string
	 */
	public String getUrl() {
		return "http://localhost:8080/sv?site="+this.getSite()+"&id="+this.getId();
	}
	
	/**
	 * Return the direct URL for this page.  This reference points to an HTML file
	 * managed by TomCat, such as:
	 * http://localhost:8080/nolaria/home.html
	 * 
	 * @return URL string
	 */
	public String getDirectUrl() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("http://localhost:8080/");
		sb.append(this.site+"/");
		if (this.path.length() != 0) {
			sb.append(this.getPath());
			sb.append("/");
		}
		sb.append(this.getFile());
		
		//System.out.println("Direct URL - Path: "+this.getPath()+" - File: "+this.getFile());
		
		return sb.toString();

	}
	
	/**
	 * Get HTML mark-up to embed this page in an iFrame.
	 * @return iFrame mark-up
	 */
	public String getIFrame() {
		//	This is the mark-up that puts an iFrame into the content pane.
		return "\t<iframe src='"+this.getDirectUrl()+"' title='"+this.getTitle()+"'></iframe>\n";
	}
	
	/**
	 * Build a list of keywords from the text associated with this page.
	 * JSoup is used to parse the HTML file.
	 * The Penn Treebank Tokenizer is used to split text into words.
	 * The Stanford NLP Maxent Tagger is used to add part of speech to words.
	 * 
	 * @throws PageException
	 */
	public List<String> findKeywords() throws PageException {	
		
		try {
			//	1.  Open the file.
			String fileName = this.getFullFileName();
			System.out.println ("File to parse: "+fileName);
			File f = new File(fileName);
			
			//	2.  Parse the HTML
			org.jsoup.nodes.Document doc = Jsoup.parse(f, "UTF-8", "http://example.com/");
			List<Node> nodes = doc.childNodes();
			//Element root = doc.root();				//	Get the page root.

			//	3.  Collect indexable strings from this HTML document.
			//Map<String,IndexableString> strings = new HashMap<String,IndexableString>();
			List<String> content = new Vector<String>();
			for (Node node : nodes) {;
				htmlWalker(0, node, content);
			}
			//System.out.println("Content strings found: "+content.size());
			
			//	4.  Clean up punctuation
			//this.cleanPunctuation(content);
			
			//	5.  Merge all strings extracted from the HTML.
			StringBuffer allText = new StringBuffer();
			for (String s : content) {
				/*	Attempts to fix the &nbsp; issue which JSoup doesn't handle well.
				char firstChar = s.charAt(0);
				if (firstChar == '?') System.out.println("Found a question mark.");
				if (!Character.isDigit(firstChar) && !Character.isLetter(firstChar)) {
					System.out.println("*** Non-alphabetic character found in: "+s);
					s = s.substring(1, s.length());		//	Removes leading strange character such as &nbsp;  -- not working.
				}
				*/
				allText.append(s.toLowerCase()+" ");
			}
			String mergedContent = allText.toString();
			//System.out.println("Merged content size: "+mergedContent.length());
			
			//	6.  Create a list of tagged words.
			MaxentTagger tagger = new MaxentTagger(PageId.TAGGER_MODEL);		
			PennTreebankTokenizer tokenizer = new PennTreebankTokenizer(new StringReader(mergedContent));
			List<String> uncleanTokens  = tokenizer.tokenize();
			List<String>tokens = new Vector<String>();
			for (String token : uncleanTokens) {
				if (token.length() > 3)
					tokens.add(this.cleanPunctuation(token).trim());
			}
			for (int i=0; i<10000; i++) {}
			List<Word> words = SentenceUtils.toUntaggedList(tokens);
			List<TaggedWord> taggedWords = tagger.apply(words);
			System.out.println("Tagged words found: "+taggedWords.size());
			
			//	7.  Create a list of keywords.
			this.keywords = new Vector<String>();
			for (TaggedWord w : taggedWords) {
				String twWord = w.word();
				String twTag = w.tag();
				if (this.isIndexableTag(twTag)) {
					//	Add the word to the keyword list if not already included.
					if (this.keywords.indexOf(twWord) == -1)
						this.keywords.add(twWord);
				}
			}
			System.out.println("Keywords found: "+this.keywords.size());
			
			return this.keywords;

		}
		catch (IOException io) {
			throw new PageException(io.getCause().getMessage());
		}	
	}

	/**
	 * Create a description string for this page and return it.
	 * returns description string.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append(title + ": ");
		sb.append("is identified by: "+id+", ");
		sb.append("in site: "+site);
		//sb.append("located in: "+this.getFullFileName());
		sb.append(" with path: "+this.getPath());
		
		return sb.toString();
	}
	
	/***********************************************
	 *              Private Methods                *
	 **********************************************/

	/**
	 * Recursive HTML document scanner.
	 * This walks over a DOM tree, extracts text, adds it to content, which is a list of strings.
	 * 
	 * @param node
	 */
	public void htmlWalker(int level, Node node, List<String> content) {		
		//	If this is an element node, process it.
		if (node instanceof Element) {
			Element element = (Element) node;
			String tagName = element.tagName().toLowerCase();
			if (isIndexable(tagName) && element.hasText()) {
				String text = element.text();
				content.add(text);
			}
		}
		
		//	Get the child notes and recurse.
		List<Node> cNodes = node.childNodes();
		if (cNodes.size() > 0) {
			for (Node cNode : cNodes)
				htmlWalker(level++, cNode, content);
		}		
	}
	
	/**
	 * Only text in these HTML tags are considered indexable.
	 * This is used to find content strings in a web page.
	 * 
	 * @param tagName
	 * @return
	 */
	private boolean isIndexable(String tagName) {
		switch (tagName) {
			case "div":
			case "span":
			case "p":
			case "h1":
			case "h2":
			case "h3":
			case "h4":
			case "li":
			return true;
		}
		return false;
	}

	/**
	 * Remove all punctuation marks in the list of strings passed.
	 * Replacing a punctuation character with a space avoids the regular expression problems that String.replaceAll()
	 * brings in.  As long as the character is at the start or end, spaces get trimmed off elsewhere.
	 * 
	 * @returns	String with punctuation removed
	 */
	private String cleanPunctuation(String str) {
		str = str.replace('.', ' ');	//	Remove periods.
		str = str.replace('?', ' ');	//	Remove question.
		str = str.replace('!', ' ');	//	Remove exclamation.
		str = str.replace(',', ' ');	//	Remove commas.
		str = str.replace(';', ' ');	//	Remove semi-colons.
		str = str.replace(':', ' ');	//	Remove colons.
		//str = str.replace('-', ' ');	//	Remove hyphens.
		//str = str.replaceAll("-", "");	//	Remove dashes.		
		//str = str.replace('/', ' ');	//	Remove slash.
		str = str.replaceAll("/", "");	//	Remove slashes.		
		str = str.replace('+', ' ');	//	Remove plus.
		str = str.replace('&', ' ');	//	Remove ampersand.
		str = str.replace('*', ' ');	//	Remove asterisk.
		str = str.replace('=', ' ');	//	Remove equal sign.
		str = str.replace('"', ' ');	//	Remove double quote.
		str = str.replace('(', ' ');	//	Remove open paren.
		str = str.replace(')', ' ');	//	Remove close paren.
		str = str.replace('{', ' ');	//	Remove open brace.
		str = str.replace('}', ' ');	//	Remove close brace.
		str = str.replace('[', ' ');	//	Remove open bracket.
		str = str.replace(']', ' ');	//	Remove close bracket.
		str = str.replaceAll("'", "");	//	Remove apostrophes.

		return str;
	}
	
	/**
	 * This is an attempt at a more clever way to remove punctuation characters.
	 * It's not as nuanced as cleanPunctuation, however.  For example, embedded
	 * hyphens would be removed, which is not desired.
	 * 
	 * @param str
	 * @return string with punctuation filtered out.
	 */
	@SuppressWarnings("unused")
	private String filterLetters(String str) {
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<str.length(); i++) {
			char c = str.charAt(i);
			if (Character.isLetter(c))
				sb.append(c);
		}
		return sb.toString();
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

}
