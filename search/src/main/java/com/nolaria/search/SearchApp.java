/**
 * 
 */
package com.nolaria.search;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
import org.apache.lucene.util.QueryBuilder;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;

//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;

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
		SearchApp.app.index(testFile);
	}

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
}
