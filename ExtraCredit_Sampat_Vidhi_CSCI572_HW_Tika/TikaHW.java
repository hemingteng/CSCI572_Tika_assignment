import java.io.*;
import java.util.*;
import java.util.regex.*;

import java.io.File; 
import java.io.FileInputStream; 
import java.io.FileNotFoundException; 
import java.io.InputStream; 
import java.io.IOException; 

import org.apache.log4j.BasicConfigurator;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.tika.Tika; 
import org.apache.tika.exception.TikaException; 
import org.apache.tika.metadata.Metadata; 
import org.apache.tika.parser.AutoDetectParser; 
import org.apache.tika.parser.Parser; 
import org.apache.tika.parser.ParseContext; 
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler; 
import org.xml.sax.ContentHandler; 
import org.xml.sax.SAXException; 
import org.xml.sax.helpers.DefaultHandler;
//import org.apache.commons.*;
//import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.lang3.StringUtils;

public class TikaHW {

	List<String> keywords;
	PrintWriter logfile;
	int num_keywords, num_files, num_fileswithkeywords;
	Map<String,Integer> keyword_counts; 	
	Map<String,Integer> file_keyword_counts;
	Map<String,Integer> LDcount;
	Date timestamp;
	int countkey[];
	int countfiles[];

	/**
	 * constructor
	 * DO NOT MODIFY
	 */
	public TikaHW() {
		keywords = new ArrayList<String>();
		num_keywords=0;
		num_files=0;
		num_fileswithkeywords=0;
		keyword_counts = new HashMap<String,Integer>();
		file_keyword_counts = new HashMap<String, Integer>();
		LDcount = new HashMap<String,Integer>();
		timestamp = new Date();
			
		try {
			logfile = new PrintWriter("log.txt");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * destructor
	 * DO NOT MODIFY
	 */
	protected void finalize() throws Throwable {
		try {
			logfile.close();
	    } finally {
	        super.finalize();
	    }
	}

	/**
	 * main() function
	 * instantiate class and execute
	 * DO NOT MODIFY
	 */
	public static void main(String[] args) {
//		BasicConfigurator.configure();
		TikaHW instance = new TikaHW();
		instance.run();
	}

	/**
	 * execute the program
	 * DO NOT MODIFY
	 */
	private void run() {

		// Open input file and read keywords
		try {
			BufferedReader keyword_reader = new BufferedReader(new FileReader("keywords.txt"));
			String str;
			while ((str = keyword_reader.readLine()) != null) {
				keywords.add(str);
				num_keywords++;
				keyword_counts.put(str, 0);
				file_keyword_counts.put(str, 0);
				LDcount.put(str,0);
			}
			
			countkey  = new int[num_keywords];
			countfiles = new int[num_keywords];	
					
			keyword_reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Open all pdf files, process each one
		File pdfdir = new File("./vault");
		File[] pdfs = pdfdir.listFiles(new PDFFilenameFilter());
		for (File pdf:pdfs) {
			num_files++;
			processfile(pdf);
		}

		/*for (int i=0; i<11; i++) {
			File pdf=pdfs[i];
			num_files++;
			processfile(pdf);
		}*/
		// Print output file
		try {
			PrintWriter outfile = new PrintWriter("output.txt");
			outfile.print("Keyword(s) used: ");
			if (num_keywords>0) outfile.print(keywords.get(0));
			for (int i=1; i<num_keywords; i++) outfile.print(", "+keywords.get(i));
			outfile.println();
			outfile.println("No of files processed: " + num_files);
			/*
			for(int i=0; i< num_keywords; i++)
			{
				num_fileswithkeywords=num_fileswithkeywords + countfiles[i];
			}
			
			*/
			outfile.println("No of files containing keyword(s): " + num_fileswithkeywords);
			outfile.println();
			outfile.println("No of occurrences of each keyword:");
			outfile.println("----------------------------------");
			for (int i=0; i<num_keywords; i++) {
				String keyword = keywords.get(i);
				outfile.println("\t"+keyword+": "+keyword_counts.get(keyword));
			}
			
			outfile.println("No of files containing each keyword:");
			outfile.println("----------------------------------");
			for (int i=0; i<num_keywords; i++) {
				String keyword = keywords.get(i);
				outfile.println("\t"+keyword+": "+file_keyword_counts.get(keyword));
			}
			outfile.println("No of matches with Levenshtein Distance for each keyword:");
			outfile.println("----------------------------------");
			for (int i=0; i<num_keywords; i++) {
				String keyword = keywords.get(i);
				outfile.println("\t"+keyword+": "+LDcount.get(keyword));
			}
			
			outfile.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Process a single file
	 * 
	 * Here, you need to:
	 *  - use Tika to extract text contents from the file
	 *  - (optional) check OCR quality before proceeding
	 *  - search the extracted text for the given keywords
	 *  - update num_fileswithkeywords and keyword_counts as needed
	 *  - update log file as needed
	 * 
	 * @param f File to be processed
	 */
	private void processfile(File f) {

		/***** YOUR CODE GOES HERE *****/

		InputStream input = null;  
		
		
        try {  
            input = new FileInputStream(f);  
  
            PDFParser parser = new PDFParser();  
            ContentHandler contentHandler = new BodyContentHandler(-1);  
            Metadata metadata = new Metadata();  
            parser.parse(input, contentHandler, metadata, new ParseContext());  
			//System.out.println("content: " + contentHandler.toString());  
			
			int temp=0, temp1=0,dist;
			String originalstring = contentHandler.toString().toLowerCase();
			String[] temparray = originalstring.split(" ");
			String[] matcharray = new String[temparray.length];
			
			for(int i=0; i< num_keywords ; i++)
			{
				// Adding space before and after the keywords so that wrong matches are not found
				String tosearch = " "+keywords.get(i).toLowerCase()+" ";
				int index = 0;
	//exact match
				 temp=(originalstring.length() - originalstring.replace(tosearch, "").length()) / tosearch.length();
		
	//levenshtein distance 1 match
				 for(int j=0; j< temparray.length; j++)
				 {
					 dist = StringUtils.getLevenshteinDistance(keywords.get(i).toLowerCase(), temparray[j]);
					 if(dist == 1)
					 {
					 	 updatelog(temparray[j],f.getName());
				    	 //  matcharray[index] = temparray[j];
						 index++;
					 }
				 }
				 //System.out.println(index);
				String str = keywords.get(i); 
				int t = LDcount.get(str);
				index = index +t;
				LDcount.put(str, index);
			    
				
			    if(temp!=0)
				{// to update the log file with a search hit, use:
					updatelog(tosearch,f.getName());
					countkey[i] = countkey[i] + temp;	
					keyword_counts.put(keywords.get(i),countkey[i]);
				
					if(countkey[i]>0)
					{
						countfiles[i] = countfiles[i] + 1;
						file_keyword_counts.put(keywords.get(i),countfiles[i]);
					}
				}
			}
			for(int i=0; i< num_keywords ; i++)
			{
				String tosearch = " "+keywords.get(i).toLowerCase()+" ";
				originalstring = contentHandler.toString().toLowerCase();
	
				temp=(originalstring.length() - originalstring.replace(tosearch, "").length()) / tosearch.length();
				if(temp > 0)
				{
					//System.out.println(f+" "+i);
					num_fileswithkeywords++;
					break;
				}
			}
        } catch (IOException e) {  
            e.printStackTrace();  
        } catch (TikaException e) {  
            e.printStackTrace();  
        } catch (SAXException e) {  
            e.printStackTrace();  
        } finally {  
            if (input != null) {  
                try {  
                    input.close();  
                } catch (IOException e) {  
                    e.printStackTrace();  
                }  
            }  
        }
    //   System.out.println("\n");
	}
	

	
	/**
	 * Update the log file with search hit
	 * Appends a log entry with the system timestamp, keyword found, and filename of PDF file containing the keyword
	 * DO NOT MODIFY
	 */
	private void updatelog(String keyword, String filename) {
		timestamp.setTime(System.currentTimeMillis());
		logfile.println(timestamp + " -- \"" + keyword + "\" found in file \"" + filename +"\"");
		logfile.flush();
	}

	/**
	 * Filename filter that accepts only *.pdf
	 * DO NOT MODIFY 
	 */
	static class PDFFilenameFilter implements FilenameFilter {
		private Pattern p = Pattern.compile(".*\\.pdf",Pattern.CASE_INSENSITIVE);
		public boolean accept(File dir, String name) {
			Matcher m = p.matcher(name);
			return m.matches();
		}
	}
}