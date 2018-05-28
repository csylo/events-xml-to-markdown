import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

	    int eventsFound = 0;
	    int outputCount = 0;

	    boolean expectTitle = false;
	    boolean expectPubDate = false;
	    boolean expectContents = false;

	    String eventTitle = "";
	    String fileName = "";
	    String pubDate = "";
	    String contents = "";

	    //output to md-events directory
	    new File("md-events").mkdirs();

	    while(scan.hasNextLine()){
		myList.add(scan.nextLine());
	    }
	    for(String line : myList){
		if(expectTitle && line.contains("<title>")){
		    //there are 2 tabs before html tags
		    eventTitle = line.substring(9, line.length() - 8);
		    eventsFound++;
		    expectTitle = false;
		    expectPubDate = true;
		} else if(expectPubDate && line.contains("<pubDate>")){
		    //wordpress may export post year as -0001, change to 2000
		    line = line.replace("-0001", "2000");
		    pubDate = line.substring(11, line.length() - 10);
		    
		    fileName = toFileName(eventTitle, pubDate);
		    pubDate = toFrontMatterDate(pubDate);
		    expectPubDate = false;
		} else if(line.contains("<content:encoded>")){
		    expectContents = true;
		} else if(expectContents && (!line.contains("<![CDATA["))){
		    if(line.contains("]]>")){
			//contents ended, output the collected event to file
			String fmTop = "---\n";
			String fmTitle = "title: " + eventTitle.replace(":", "") + "\n";
			String fmDate = "date: " + pubDate + "\n";
			String fmLayout = "layout: post\n";
			String fmBottom = "---\n\n";

			//make sure html content is formatted correctly
			contents = htmlFormat(contents);
			
			try{
			    File outputFile = new File("md-events", fileName);
			    if(outputFile.exists()) {
				//replace file
				outputFile.delete();
				outputFile.createNewFile();
			    } else{
				//doesn't exist, create it
				outputFile.createNewFile();
			    }
			    FileOutputStream fos = new FileOutputStream(outputFile);
			    try{
				fos.write(fmTop.getBytes());
				fos.write(fmTitle.getBytes());
				fos.write(fmDate.getBytes());
				fos.write(fmLayout.getBytes());
				fos.write(fmBottom.getBytes());
				fos.write(contents.getBytes());
				fos.flush();
				fos.close();
				outputCount++;

				//reset for next event collection
				eventTitle = "";
				fileName = "";
				pubDate = "";
				contents = "";
				expectContents = false;
			    } catch(IOException e){
				System.out.println("IOException 1: fos method failed");
			    } finally{
				try{
				    if(fos != null){
					fos.close();
				    }
				} catch(IOException e){
				    System.out.println("IOException 2: fos method failed");
				}
			    }
			} catch(IOException e){
			    System.out.println("IOException 3: outputFile method failed");
			}
		    } else{
			//this line is content
			contents = contents.concat(line);
		    }
		} else if(line.contains("<item>")){
		    expectTitle = true;
		}
	    }
	    scan.close();
	    System.out.println("   ___________________________");
	    System.out.println("  | Extracted to \"md-events/\"");
	    System.out.println("  |");
	    System.out.println("  | Events read: " + eventsFound);
	    System.out.println("  | Files created: " + outputCount);
	    System.out.println("  |___________________________\n");
	} catch(FileNotFoundException e){
	    System.out.println("FileNotFoundException 1: events.xml not found");
	}
    }

    public static String toFileName(String title, String date){
	//format event title to file name conventions
	title = title.toLowerCase();
	title = title.replace(' ', '-');
	title = title.replace("@", "at");
	title = title.replace(":", "");
	title = title.replace("&#039;", "");
	title = title.replace("!", "");

	//prefixed with jekyll post date
	title = toDatePrefix(date) + "-" + title;

	//don't overwrite existing md event if it has same name
	File mayExist = new File("md-events", title + ".md");
	
	if(mayExist.exists()){
	    title = title.concat("-2.md");
	} else{
	    title = title.concat(".md");
	}
	
	return title;
    }

    public static String toDatePrefix(String date){
	//format wp date to filename date format
	//wp date is: Mon, 02 Oct 2017 19:17:10 +0000
	//change to post name y/m/d date: 2017-10-02
	String year = date.substring(12, 16);
	String month = monthAbbrToNum(date.substring(8, 11));
	String day = date.substring(5, 7);
	return year + "-" + month + "-" + day;
    }

    public static String toFrontMatterDate(String date){
	//format wp date to markdown front matter date format
	//wp date is: Mon, 02 Oct 2017 19:17:10 +0000
	//change to jekyll y/m/d date: 2017-10-02 19:17:10 +0000
	String year = date.substring(12, 16);
	String month = monthAbbrToNum(date.substring(8, 11));
	String day = date.substring(5, 7);
	String time = date.substring(17);
	return year + "-" + month + "-" + day + " " + time;
    }

    public static String htmlFormat(String contents){
	//newline every tag so it renders properly
	contents = contents.replace("><", ">\n<");
	return contents;
    }

    public static String monthAbbrToNum(String month){
	//month abbreviation to month number
	switch(month.toLowerCase()){
	    case "jan": {
		return "01";
	    }
	    case "feb": {
		return "02";
	    }
	    case "mar": {
		return "03";
	    }
	    case "apr": {
		return "04";
	    }
	    case "may": {
		return "05";
	    }
	    case "jun": {
		return "06";
	    }
	    case "jul": {
		return "07";
	    }
	    case "aug": {
		return "08";
	    }
	    case "sep": {
		return"09";
	    }
	    case "oct": {
		return "10";
	    }
	    case "nov": {
		return "11";
	    }
	    case "dec": {
		return "12";
	    }
	}
	return "error";
    }
}
