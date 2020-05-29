package com.ir.assignment;

import java.io.File;
import java.io.FileFilter;

public class FileTypeFilter implements FileFilter {
	public boolean accept(File pathname) {
		//Return true id txt or html file
		if(pathname.getName().toLowerCase().endsWith(".txt") || pathname.getName().toLowerCase().endsWith(".htm") || pathname.getName().toLowerCase().endsWith(".html")){
			return true;
		}else {
			return false;
		}
	      
	   }
}
