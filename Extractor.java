import java.util.Scanner;
import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;

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

	    //reaching a tag sets it up to save whatever line is under it
	    //as title, author, date, content, etc.
	    boolean expectTitle = false;

	    while(scan.hasNextLine()){
		myList.add(scan.nextLine());
	    }
	    for(String line : myList){
		if(expectTitle){
		    //there are 2 tabs before title-tag
		    String eventTitle = line.substring(9, line.length() - 8);
		    String fileName = toFileName(eventTitle);
		    System.out.println("Event title: " + eventTitle + "      File name: " + fileName);
		    expectTitle = false;
		}
		if(line.contains("<item>")){
		    expectTitle = true;
		}
	    }
	    scan.close();
	}
	catch(FileNotFoundException e){
	    System.out.println("@@@FileNotFoundException");
	}
    }

    public static String toFileName(String title){
	title = title.toLowerCase();
	title = title.replace(' ', '-');
	title = title.replace("@", "at");
	title = title.replace(":", "");
	//if(file(title + ".md") already exists in md-events){
	//    title = title.concat("-2.md");
	//} else{
	        title = title.concat(".md");
	//}
	return title;
    }
}
