package com.nolaria.tools;

import java.io.*;
import java.util.UUID;

import com.nolaria.sv.db.*;

public class GhostMaker {

	public static void main(String[] args) throws Exception {
		//GhostMaker.filter();
		//GhostMaker.make();
		GhostMaker.fix();
	}
	
	/**
	 * Reads nolaria.csv and expects the following fields on each line:
	 * site, path, file, title, id
	 */
	private static void filter() {
		BufferedReader reader;
		
		PageRegistry pr = new PageRegistry();

		try {
			reader = new BufferedReader(new FileReader("d:/nolaria.csv"));
			String line = reader.readLine();

			int ct = 1;
			while (line != null) {
				//System.out.println(number++ +": "+line);
				
				String[]parts = line.split(",");
				String siteName = parts[0];
				String path = parts[1];
				String fileName = parts[2];
				String title = parts[3];
				String id = parts[4];
				
				String fullPathName = null;
				if (path.length() == 0)
					fullPathName = SiteRegistry.FILE_ROOT+"/"+siteName+"/"+fileName;
				else
					fullPathName = SiteRegistry.FILE_ROOT+"/"+siteName+"/"+path+"/"+fileName;

				
				//	We want to filter out plane scape files.
				Boolean isPlanar = (path.indexOf("zee-planes") != -1) || fileName.compareTo("zee-planes.html") == 0;
				
				//	Also filter out the oka-03.html file.
				Boolean isOka = fileName.compareTo("oka-03.html") == 0;
				
				//	Filter out old-petronia.
				Boolean isOldP = path.indexOf("old-petronia") != -1;
				
				//	Filter out kalyx narrative
				Boolean isKalyx = path.indexOf("kalyx-narrative") != -1;
				
				File f = new File(fullPathName);
				if (!isPlanar && !f.exists() && !isOka && !isOldP && !isKalyx) {
					//System.out.println(fullPathName);
					System.out.println(line);					
				}

				ct++;
				
				// read next line
				line = reader.readLine();
				if (line == null)
					break;

			}
			System.out.println("Files found: "+ct);


			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * Reads nolaria.csv and expects the following fields on each line:
	 * site, path, file, title, id
	 * 
	 * Builds the ghost file, registers it, and writes it out.
	 * @throws SiteException 
	 */
	private static void make() throws Exception {
		PageRegistry pr = new PageRegistry();
		SiteRegistry sr = new SiteRegistry();
		
		BufferedReader reader;

		try {
			reader = new BufferedReader(new FileReader("d:/nolaria-edited.csv"));
			
			String line = reader.readLine();

			int ct = 1;
			while (line != null) {
				System.out.println(ct++ +": "+line);
				
				//if (ct ==2)
				//	break;
				
				//	Extract the page record fields.
				String[]parts = line.split(",");
				String siteName = parts[0];
				String path = parts[1];
				String fileName = parts[2];
				String title = parts[3];
				//String id = parts[4];
				String id = UUID.randomUUID().toString(); 
				
				//	Create the full path file name to be created.
				String fullPathName = null;
				if (path.length() == 0)
					fullPathName = SiteRegistry.FILE_ROOT+"/"+siteName+"/"+fileName;
				else
					fullPathName = SiteRegistry.FILE_ROOT+"/"+siteName+"/"+path+"/"+fileName;

				//	Files in this list are assumed to not exist but check anyways.			
				File f = new File(fullPathName);
				if (!f.exists()) {
					
					System.out.println("File to make: "+fullPathName);
					
					if (path.length() > 0) {
						System.out.println("Checking path nodes exist:");
						
						String[]nodes = path.split("/");
						String folderPath = SiteRegistry.FILE_ROOT+"/nolaria";
						for (String node : nodes) {
							folderPath += "/"+node;
							System.out.println("\t"+folderPath);
							File folder = new File(folderPath);
							if (folder.exists()) {
								//System.out.println("\tFolder node "+node+" exists.  Skip it.");
								continue;
							}
							else {
								System.out.println("\tMaking "+node);
								folder.mkdir();
							}
								
						}
						//	TODO:  make folders.
					}
					
					//	Get the site info.
					Site site = sr.getSiteByName("nolaria");
					String cssFileName = site.getCss();
					
					//	Holds the page meta data.
					PageInfo info = new PageInfo(title, fileName, id);
					
					//	Assemble the content to update.
					StringBuffer sb = new StringBuffer();
					sb.append("<html>\n");
					sb.append("<head>\n");
					sb.append("</head>\n");
					sb.append("<body>\n");
					sb.append("\t<p>Ghost File</p>\n");
					sb.append("</body>\n");
					sb.append("</html>\n");
					String content = sb.toString();
					
					//	Update the content with a real head section.
					String pageContent = Util.updateHeaderInfo( content, cssFileName, info);
	
					//System.out.println("\n"+pageContent);
					
					//	Save the new ghost file.
					Util.saveFile(pageContent, fullPathName);
					
					//	Register the new page.
					//  String id, String site, String title, String file, String path
					pr.createPage(id, siteName, title, fileName, path);
					
					ct++;
					
				}
				// read next line
				line = reader.readLine();
				if (line == null)
					break;
			}
			System.out.println("Files created: "+ct);
			
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	private static void fix() throws Exception {
		PageRegistry pr = new PageRegistry();
		SiteRegistry sr = new SiteRegistry();
		
		BufferedReader reader;

		try {
			reader = new BufferedReader(new FileReader("d:/nolaria-edited.csv"));
			
			String line = reader.readLine();

			int ct = 1;
			while (line != null) {
				System.out.println(ct++ +": "+line);
				
				//if (ct ==2)
				//	break;
				
				//	Extract the page record fields.
				String[]parts = line.split(",");
				String siteName = parts[0];
				String path = parts[1];
				String fileName = parts[2];
				String title = parts[3];
				//String id = parts[4];
				String id = UUID.randomUUID().toString(); 
				
				//	Create the full path file name to be created.
				String fullPathName = null;
				if (path.length() == 0)
					fullPathName = SiteRegistry.FILE_ROOT+"/"+siteName+"/"+fileName;
				else
					fullPathName = SiteRegistry.FILE_ROOT+"/"+siteName+"/"+path+"/"+fileName;

				System.out.println(fullPathName);
				
				//	Fix the damn file.
				String content = Util.loadFile(fullPathName);
				PageInfo info = Util.getHeaderInfo(content);
				String cssUrl = "http://localhost:8080/nolaria/green.css";
				content = Util.updateHeaderInfo(content, cssUrl, info);
				Util.saveFile(content, fullPathName);
				
				// read next line
				line = reader.readLine();
				if (line == null)
					break;
			}
			System.out.println("Files fixed: "+ct);
			
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}

}
