/**
 * 
 */
package com.nolaria.search;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.IOUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

//import com.nolaria.sv.db.*;
//import org.jsoup.select.*;

/**
 * DEPRECATED - The code in this project was used to explore Lucene, which provided to be too difficult to use 
 * for SiteView indexing.  A simpler approach was added to the register project by adding an indexAll() method.
 */

/**
 * Aug. 1, 2023
 * Originally created as a sandbox to experiment with Lucene, I also played with the
 * Standford NLP Maxent tagger here, leading to full indexing of web pages.
 * Indexing is now supported in the register package (register-2.0.0.jar).
 * 
 * @author Mark J. Norton
 * @author markjnorton@gmail.com
 *
 */
public class SearchApp {
	public static String LUCENE_FIELD_STR = "str";
	public static String LUCENE_FIELD_PID = "pid";
	public static String LUCENE_FIELD_TITLE = "title";
	public static SearchApp app = new SearchApp();
	
	public static String testFileName1 = "D:\\apache-tomcat-9.0.40\\webapps\\planes\\material-planes.html";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println ("Search Indexing Test");
		System.out.println ("--------------------\n");
		File testFile = new File(SearchApp.testFileName1);
		//SearchApp.app.simpleTest(testFile);
		//SearchApp.app.parseFile(testFile);
		//SearchApp.app.complextTest();
		SearchApp.app.taggedTest(testFile);
	}

	/**
	 * Index the text in the file passed and add it to the search engine.
	 * 
	 * @param f
	 */
	public void simpleTest(File f) {
		System.out.println("File to index: "+f.getPath());
		try {
			//	This code comes from:  https://lucene.apache.org/core/9_1_0/core/index.html
			//	Index a piece of text.
			Analyzer analyzer = new StandardAnalyzer();
		    Path indexPath = Files.createTempDirectory("tempIndex");
		    Directory directory = FSDirectory.open(indexPath);
		    IndexWriterConfig config = new IndexWriterConfig(analyzer);
		    IndexWriter iwriter = new IndexWriter(directory, config);
		    
		    Document doc = new Document();
		    String text = "This is the text to be indexed.";
		    doc.add(new Field(LUCENE_FIELD_STR, text, TextField.TYPE_STORED));
		    doc.add(new Field(LUCENE_FIELD_PID, "0406cef4-ef24-477f-9299-92de97d6192e", TextField.TYPE_STORED));
		    doc.add(new Field(LUCENE_FIELD_TITLE, "Test File Title", TextField.TYPE_STORED));
		    
		    iwriter.addDocument(doc);
		    iwriter.close();
		    
		    //  Search for the text.
		    // Now search the index:
		    DirectoryReader ireader = DirectoryReader.open(directory);
		    IndexSearcher isearcher = new IndexSearcher(ireader);
		    
		    // Parse a simple query that searches for "text":
		    QueryParser parser = new QueryParser(LUCENE_FIELD_STR, analyzer);
		    Query query = parser.parse("text");
		    ScoreDoc[] hits = isearcher.search(query, 10).scoreDocs;
		    //assertEquals(1, hits.length);
		    
		    // Iterate through the results:
		    for (int i = 0; i < hits.length; i++) {
			    Document hitDoc = isearcher.doc(hits[i].doc);
			    String str = hitDoc.get(LUCENE_FIELD_STR);
			    String pid = hitDoc.get("pid");
			    String title = hitDoc.get("title");
			    System.out.println("\t"+pid+" - "+title+": "+str);
		    }
		    ireader.close();
		    directory.close();
		    IOUtils.rm(indexPath);
		}
		catch (IOException io) {
			System.out.println("IO Error: "+io.getCause());
		}
		catch (ParseException pe) {
			System.out.println("Query Parser Error: "+pe.getCause());			
		}
	}
	
	/**
	 * Set up Lucene to index a set of files.
	 */
	public void complextTest() {
		try {
			//	This code comes from:  https://lucene.apache.org/core/9_1_0/core/index.html
			//	Index a piece of text.
			Analyzer analyzer = new StandardAnalyzer();
		    Path indexPath = Files.createTempDirectory("tempIndex");
		    Directory directory = FSDirectory.open(indexPath);
		    IndexWriterConfig config = new IndexWriterConfig(analyzer);
		    IndexWriter iwriter = new IndexWriter(directory, config);
		    
		    //	Index all of the files in a file system.
		    indexAllFiles(iwriter);
		    
		    iwriter.close();
		    
		    //  Search for the text.

		    //	Close directory.
		    directory.close();
		    //IOUtils.rm(indexPath);
		}
		catch (IOException io) {
			System.out.println("IO Error: "+io.getCause());
		}
		/*
		catch (ParseException pe) {
			System.out.println("Query Parser Error: "+pe.getCause());			
		}
		*/
	}
	
	/**
	 * Parse a page using JSoup, then tag words found using the Stanford NLP Maxent tagger.
	 */
	public void taggedTest(File f) {
		//	Extract strings from a parsed HTML page.
		ParsedFile pf = this.parseFile(f);
		//System.out.println("Strings found: "+pf.strings.size()+"\n");

		//	Merge strings and create a list of tagged words.
		pf.findKeywords();
		System.out.println("Keywords found: "+pf.getKeywordsCount());
		System.out.println(pf.toKeywordString());
		
	}
	
	/**
	 * Walk over a file system and index any HTML pages found using an Index Writer.
	 * 
	 * @param iwriter
	 */
	public void indexAllFiles(IndexWriter iwriter) {
		//	The following file stubs out scanning a file system.
		File testFile = new File(SearchApp.testFileName1);
		
		this.indexFile(testFile, iwriter);		
	}
	
	/**
	 * 
	 * @param f
	 * @param iwriter
	 */
	public void indexFile(File f, IndexWriter iwriter) {
		//	Parse the HTML file passed.
		ParsedFile pf = parseFile(f);
		
		//	Index the strings here.
		Set<String> keys = pf.strings.keySet();
		for (String str : keys) {
		    Document doc = new Document();
		    doc.add(new Field(LUCENE_FIELD_STR, str, TextField.TYPE_STORED));
		    doc.add(new Field(LUCENE_FIELD_PID, pf.pid, TextField.TYPE_STORED));
		    doc.add(new Field(LUCENE_FIELD_TITLE, pf.title, TextField.TYPE_STORED));			
		}
		
		//	Print out the strings found.
		System.out.println(pf.toString());
	}
	
	/**
	 * Parse an HTML file and show text fragments.
	 * Note:  The Document class used here is a JSoup document.
	 * 
	 * @param file to be parsed
	 * @return parsed file object
	 */
	public ParsedFile parseFile(File file) {
		ParsedFile pf = new ParsedFile();

		try {
			org.jsoup.nodes.Document doc = Jsoup.parse(file, "UTF-8", "http://example.com/");
			List<Node> nodes = doc.childNodes();
			Element root = doc.root();				//	Get the page root.
			
			//	Collect indexable strings from this HTML document.
			//Map<String,IndexableString> strings = new HashMap<String,IndexableString>();
			for (Node node : nodes) {
				htmlWalker(0, node, pf.strings);
			}
			
			//	Extract meta data
			Elements mdElements = root.getElementsByTag("meta");
			if (mdElements.size() > 0) {
				for (Element element : mdElements) {
					Node node = (Node)element;
					String name = node.attr("name");
					if (name.compareTo("pid") == 0)
						pf.pid = node.attr("content");
					if (name.compareTo("title") == 0)
						pf.title = node.attr("content");
				}
			}
			else
				System.out.println("No metadata elements were found");
			
			System.out.println();			
		}
		catch (IOException io) {
			System.out.println("IO Error: "+io.getCause());
			io.printStackTrace();
		}
		catch (Exception ex) {
			System.out.println("Exception: "+ex.getCause());
			ex.printStackTrace();			
		}
		
		return pf;
	}
	
	/**
	 * Recursive HTML document scanner.
	 * 
	 * @param node
	 * @throws Exception 
	 */
	public void htmlWalker(int level, Node node, Map<String,IndexableString> strings) throws Exception {
		
		//	If this is an element node, process it.
		if (node instanceof Element) {
			Element element = (Element) node;
			String tagName = element.tagName().toLowerCase();
			if (isIndexable(tagName) && element.hasText()) {
				String text = element.text();
				IndexableString str = new IndexableString(level, tagName, text);
				strings.put(text,str);
				//System.out.println(level+"| "+Util.tabber(level)+tagName+": "+text);
			}
		}
		
		//	Get the child notes and recurse.
		List<Node> cNodes = node.childNodes();
		if (cNodes.size() > 0) {
			for (Node cNode : cNodes)
				htmlWalker(level++, cNode, strings);
		}		
	}
	
	/**
	 * Only text in these HTML tags are considered indexable.
	 * @param tagName
	 * @return
	 */
	public boolean isIndexable(String tagName) {
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
	
}
