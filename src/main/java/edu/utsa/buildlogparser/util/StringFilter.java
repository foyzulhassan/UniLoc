package edu.utsa.buildlogparser.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringFilter {
	
	private static final String IMAGE_PATTERN ="([^\\s]+(\\.(?i)(/bmp|jpg|gif|java))+)";

	
	public static String getStringRemvingFilePath(String text)
	{
		String filteredString = null;
		
		Pattern word = Pattern.compile(IMAGE_PATTERN);
	    int index=text.indexOf(".java");
	   
	    
	    if(index>=0) {
	        
	         int startindex=text.lastIndexOf(" /", index);
	         
	         int endindex=text.lastIndexOf("/");
	 		
	         
	         if(startindex>0 && endindex>0)
	         {
	        	 StringBuffer textbuf = new StringBuffer(text);	         
	         	 textbuf.replace(startindex, endindex+1, "");	         
	         	 filteredString=textbuf.toString();
	         }
	         else
	         {
	        	 filteredString=text;
	         }
	    }
	    else
	    {
	    	filteredString=text;
	    }	
		
		return filteredString;		
		
	}

}
