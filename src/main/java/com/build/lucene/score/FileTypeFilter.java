package com.build.lucene.score;

import java.io.File;
import java.io.FileFilter;

public class FileTypeFilter implements FileFilter {
	public boolean accept(File pathname) {
		// Return true id txt or html file
		if (pathname.getName().toLowerCase().endsWith(".txt")) {
			return true;
		} else {
			return false;
		}
	}
}
