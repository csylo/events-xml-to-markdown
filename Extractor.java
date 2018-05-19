import java.util.ArrayList;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/*
 * Get all event html content and put them into markdown files
 *
 */

public class Extractor{
    public static void main(String[] args){
	try{
	    File inputFile = new File("events.xml");
	    Scanner scan = new Scanner(inputFile);
	    ArrayList<String> myList = new ArrayList<String>();

	    boolean expectTitle = false;
	    boolean expectPubDate = false;
	    boolean expectContents = false;

	    while(scan.hasNextLine()){
		myList.add(scan.nextLine());
	    }
	    for(String line : myList){
		String eventTitle = "";
		String fileName = "";
		String pubDate = "";
		String contents = "";
		if(expectTitle && line.contains("<title>")){
		    //there are 2 tabs before html tags
		    eventTitle = line.substring(9, line.length() - 8);
		    fileName = toFileName(eventTitle);
		    expectTitle = false;
		    expectPubDate = true;
		} else if(expectPubDate && line.contains("<pubDate>")){
		    pubDate = line.substring(11, line.length() - 10);
		    expectPubDate = false;
		    expectContents = true;
		} else if(line.contains("<content:encoded>")){
		    expectContents = true;
		} else if(expectContents && (!line.contains("<![CDATA["))){
		    if(line.contains("]]>")){
			//this line is CDATA close
			expectContents = false;
		    } else{
			//this line is content
			contents = line;
		    }
		} else if(line.contains("<item>")){
		    expectTitle = true;
		}

		//output to md file
		try{
		    String filePath = "md-events/" + fileName;
		    File mayNotExist = new File(filePath);
		    if (!mayNotExist.exists()) {
			mayNotExist.createNewFile();
		    }
		    FileWriter fw = new FileWriter(filePath);
		    BufferedWriter bw = new BufferedWriter(fw);
		    try{
			bw.write("---\n");
			bw.write("title: " + eventTitle + "\n");
			bw.write("date: " + pubDate + "\n");
			bw.write("layout: post\n\n");
			bw.write("---\n");
			bw.write(contents);
		    } catch(IOException e){
			System.out.println("@@@IOException 1");
		    } finally{
			try{
			    fw.close();
			    bw.close();
			} catch(IOException e){
			    System.out.println("@@@IOException 2");
			}
		    }
		} catch(IOException e){
		    System.out.println("@@@IOException 3");
		}
	    }
	    scan.close();
	}
	catch(FileNotFoundException e){
	    System.out.println("@@@FileNotFoundException");
	}
    }

    public static String toFileName(String title){
	//format event title to file name conventions
	title = title.toLowerCase();
	title = title.replace(' ', '-');
	title = title.replace("@", "at");
	title = title.replace(":", "");
	title = title.replace("&#039;", "");

	//don't overwrite existing md event if it has same name
	File mayExist = new File("md-events/" + title + ".md");
	
	if(mayExist.exists()){
	    title = title.concat("-2.md");
	} else{
	    title = title.concat(".md");
	}
	
	return title;
    }

    public static String toFMdate(String date){
	//format wp date to front matter date format
	return date;
    }
}
