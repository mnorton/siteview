/**
 * 
 */
package com.nolaria.search;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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
//import org.jsoup.select.*;

/**
 * @author Mark J. Norton
 * @author markjnorton@gmail.com
 *
 */
public class SearchApp {
	public static SearchApp app = new SearchApp();
	
	public static String testFileName = "D:\\apache-tomcat-9.0.40\\webapps\\test-site\\special-planes\\faery.html";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println ("Lucene Search Engine Test");
		File testFile = new File(SearchApp.testFileName);
		//SearchApp.app.index(testFile);
		SearchApp.app.parseFile(testFile);
	}

	/**
	 * Index the text in the file passed and add it to the search engine.
	 * 
	 * @param f
	 */
	public void index(File f) {
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
		    doc.add(new Field("fieldname", text, TextField.TYPE_STORED));
		    iwriter.addDocument(doc);
		    iwriter.close();
		    
		    //  Search for the text.
		    // Now search the index:
		    DirectoryReader ireader = DirectoryReader.open(directory);
		    IndexSearcher isearcher = new IndexSearcher(ireader);
		    // Parse a simple query that searches for "text":
		    QueryParser parser = new QueryParser("fieldname", analyzer);
		    Query query = parser.parse("text");
		    ScoreDoc[] hits = isearcher.search(query, 10).scoreDocs;
		    //assertEquals(1, hits.length);
		    // Iterate through the results:
		    for (int i = 0; i < hits.length; i++) {
			    Document hitDoc = isearcher.doc(hits[i].doc);
			    //assertEquals("This is the text to be indexed.", hitDoc.get("fieldname"));
			    System.out.println("\t"+hitDoc.get("fieldname"));
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
	 * Parse an HTML file and show text fragments.
	 * @param file
	 */
	public void parseFile(File file) {
		try {
			org.jsoup.nodes.Document doc = Jsoup.parse(file, "UTF-8", "http://example.com/");
			List<Node> nodes = doc.childNodes();
			for (Node node : nodes) {
				htmlWalker(node);
			}			
		}
		catch (IOException io) {
			System.out.println("IO Error: "+io.getCause());			
		}
	}
	
	/**
	 * Recursive HTML document scanner.
	 * 
	 * @param node
	 */
	public void htmlWalker(Node node) {
		
		//	If this is an element node, process it.
		if (node instanceof Element) {
			Element element = (Element) node;
			String tagName = element.tagName().toLowerCase();
			if (isIndexable(tagName)) {
				String text = element.text();
				System.out.println(tagName+": "+text);
			}
		}
		
		//	Get the child notes and recurse.
		List<Node> cNodes = node.childNodes();
		if (cNodes.size() > 0) {
			for (Node cNode : cNodes)
				htmlWalker(cNode);
		}		
	}
	
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
