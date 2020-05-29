package edu.utsa.buildlogparser.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class TextFileReaderWriter {
	public static List<String> GetFileContentByLine(String filename) {
		List<String> list = new ArrayList<String>();

		try {
			File file = new File(filename);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			// StringBuffer stringBuffer = new StringBuffer();
			String line;
			while ((line = bufferedReader.readLine()) != null) {

				if (!line.trim().startsWith("<!--") && !line.trim().startsWith("<<") && !line.trim().startsWith(">>")
						&& !line.trim().startsWith("=="))
					list.add(line);
			}
			fileReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return list;

	}

	public static void DumptoCommonLabelFile(String filepath, String filename, String line) {

		Writer writer = null;
		String file;

		if (filepath.endsWith("/"))
			file = filepath + filename;
		else
			file = filepath + "/" + filename;

		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "utf-8"));
			writer.write(line);
			writer.write("\n");
		} catch (IOException ex) {
			// report
		} finally {
			try {
				writer.close();
			} catch (Exception ex) {
				/* ignore */}
		}

	}

	public static void DumptoCommonSampleFile(String filepath, String filename, String line) {

		Writer writer = null;
		String file;

		if (filepath.endsWith("/"))
			file = filepath + filename;
		else
			file = filepath + "/" + filename;

		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "utf-8"));
			writer.write(line);
			writer.write("\n");
		} catch (IOException ex) {
			// report
		} finally {
			try {
				writer.close();
			} catch (Exception ex) {
				/* ignore */}
		}

	}

	public static void logprintln(String filepath, String filename, String line) {

		Writer writer = null;
		String file;

		if (filepath.endsWith("/"))
			file = filepath + filename;
		else
			file = filepath + "/" + filename;

		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "utf-8"));
			writer.write(line);
			writer.write("\n");
		} catch (IOException ex) {
			// report
		} finally {
			try {
				writer.close();
			} catch (Exception ex) {
				/* ignore */}
		}

	}

	public static void appendLineToFile(String filenamewithpath, String line) {

		Writer writer = null;
		String file = filenamewithpath;

		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "utf-8"));
			writer.write(line);
			writer.write("\n");
		} catch (IOException ex) {
			// report
		} finally {
			try {
				writer.close();
			} catch (Exception ex) {
				/* ignore */}
		}

	}

	public static String readFile(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		try {
			StringBuilder content = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				content.append(line);
				content.append('\n');
			}
			return content.toString();
		} finally {
			if (reader != null)
				reader.close();
		}
	}

}
