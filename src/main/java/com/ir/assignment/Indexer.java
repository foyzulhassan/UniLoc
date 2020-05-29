package com.ir.assignment;
/*
 * This code is used for building Index for the retrieval program. It first consume the files
 * from desired directory we want to index and convert data to those files into string and 
 * index it. Later the indexed data can be searched and a ranked result can be obtained.
 *   
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.tartarus.snowball.ext.PorterStemmer;

public class Indexer {
	private IndexWriter writer;

	public Indexer(String indexDirectoryPath) throws IOException {
		// this directory will contain the indexes
		
		List<String> stopWords = Arrays.asList(
			      "a", "an", "and", "are", "as", "at", "be", "but", "by",
			      "for", "if", "in", "into", "is", "it",
			      "no", "not", "of", "on", "or", "such",
			      "that", "the", "their", "then", "there", "these",
			      "they", "this", "to", "was", "will", "with", "java","build"
			    );
		
		CharArraySet stopSet = new CharArraySet(stopWords, false);
		Analyzer analyzer = new StandardAnalyzer(stopSet);
		
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		FSDirectory directory = FSDirectory.open(Paths.get(indexDirectoryPath));

		// create the indexer
		IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
		writer = new IndexWriter(directory, iwc);
	}

	public void close() throws CorruptIndexException, IOException {
		writer.close();
	}

	// For every document we are storing its file name, path and content in our
	// index.
	// The content from files are converted into string and
	private Document getDocument(File file) {

		Document document = new Document();
		try {
			// path of the file
			FieldType filePathField = new FieldType();
			filePathField.setIndexOptions(IndexOptions.NONE);
			filePathField.setStored(true);
			filePathField.setTokenized(true);

			// File Name
			FieldType fileNameField = new FieldType();
			fileNameField.setIndexOptions(IndexOptions.NONE);
			fileNameField.setStored(true);
			fileNameField.setOmitNorms(true);
			fileNameField.setTokenized(true);

			FieldType fileHtmlTitle = new FieldType();
			fileHtmlTitle.setIndexOptions(IndexOptions.DOCS);
			fileHtmlTitle.setStored(true);
			fileHtmlTitle.setTokenized(true);

			// path of the file
			FieldType fileTimeStampField = new FieldType();
			fileTimeStampField.setIndexOptions(IndexOptions.NONE);
			fileTimeStampField.setStored(true);
			fileTimeStampField.setTokenized(true);

			// Content of the file
			FieldType contentField = new FieldType();
			contentField.setIndexOptions(IndexOptions.DOCS);
			contentField.setStored(true);
			contentField.setTokenized(true);
			contentField.setStoreTermVectors(true);
			contentField.setStoreTermVectorPositions(true);
			contentField.setStoreTermVectorOffsets(true);
			contentField.setStoreTermVectorPayloads(true);
			Scanner scan = new Scanner(file);
			scan.useDelimiter("\\Z");
			String content = scan.next();
			scan.close();
			Field contentValue;
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			Field fileNameValue = new Field(LuceneConstants.FILE_NAME, file.getName(), fileNameField);
			if (file.getName().contains(".htm") || file.getName().contains(".html")) {
				String htmlContent = readFile(file);
				Field FileHtmlTitle = new Field(LuceneConstants.FILE_HTML_TITLE,
						htmlContent.substring(htmlContent.indexOf("<TITLE>") + 7, htmlContent.indexOf("</TITLE")),
						fileHtmlTitle);
				document.add(FileHtmlTitle);
				contentValue = new Field(LuceneConstants.CONTENTS,
						StringPorterStemmer(RemoveTags(htmlContent.substring(htmlContent.indexOf("<BODY>") + 6,
								htmlContent.indexOf("</BODY")))) + " "
								+ htmlContent.substring(htmlContent.indexOf("<TITLE>") + 7,
										htmlContent.indexOf("</TITLE")),
						contentField);
			} else {
				contentValue = new Field(LuceneConstants.CONTENTS, porterstemmer(file).replace(".", " "), contentField);
			}
			Field filePathValue = new Field(LuceneConstants.FILE_PATH, file.getCanonicalPath(), filePathField);
			Field fileTimeStamp = new Field(LuceneConstants.FILE_TIMESTAMP, sdf.format(file.lastModified()).toString(),
					fileTimeStampField);
			document.add(contentValue);
			document.add(fileTimeStamp);
			document.add(fileNameValue);
			document.add(filePathValue);

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return document;
	}

	private void indexFile(File file) throws IOException {
		System.out.println("Indexing " + file.getCanonicalPath());
		Document document = getDocument(file);
		writer.addDocument(document);
	}

	public int createIndex(String dataDirPath, FileFilter filter) throws IOException {
		// get all files in the data directory
		File[] files = new File(dataDirPath).listFiles();

		try {
			for (File file : files) {
				if (!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead() && filter.accept(file)) {
					indexFile(file);
				} else if (file.isDirectory()) {
					createIndex(file.getAbsolutePath(), filter);
				}
			}
		} catch (Exception e) {
			System.out.println(
					"Can not find any data file at location : " + dataDirPath + " .Please provide correct path");
		}
		return writer.numDocs();
	}

	public List<String> getHtmlContent(File file) {
		List<String> htmlContent = new ArrayList<String>();
		String fileContent = new String();
		String htmlTitle = new String();
		try {
			fileContent = readFile(file);
			htmlTitle = fileContent.substring(fileContent.indexOf("<title>") + 7, fileContent.indexOf("</"));
			fileContent = fileContent.substring(fileContent.indexOf("<body>") + 6, fileContent.indexOf("</"));
			htmlContent.add(htmlTitle);
			htmlContent.add(fileContent);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return htmlContent;
	}

	public String readFile(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = System.getProperty("line.separator");

		try {
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append(" ");
			}

			return stringBuilder.toString();
		} finally {
			reader.close();
		}
	}

	public String porterstemmer(File file) {
		Scanner sc;
		List<String> lines = new ArrayList<String>();
		List<String> stemmedLines = new ArrayList<String>();
		try {
			sc = new Scanner(file);
			while (sc.hasNextLine()) {
				lines.add(sc.nextLine());
			}
			sc.close();
			PorterStemmer stemmer = new PorterStemmer();
			for (String line : lines) {
				StringBuffer stemedLine = new StringBuffer();
				String[] words = line.split(" ");
				for (int i = 0; i < words.length; i++) {
					stemmer.setCurrent(words[i]);
					stemmer.stem();
					stemedLine.append(stemmer.getCurrent() + " ");
				}
				stemmedLines.add(stemedLine.toString());
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return stemmedLines.toString();
	}

	public String RemoveTags(String content) {
		int startIndex = 0;
		int endIndex = 0;
		String toBeReplaced = "";
		while ((content.contains("<")) && (content.contains(">"))) {
			startIndex = content.indexOf("<");
			endIndex = content.indexOf(">");
			toBeReplaced = content.substring(startIndex, endIndex + 1);
			content = content.replace(toBeReplaced, " ");
		}

		return content;
	}

	static public String StringPorterStemmer(String QueryString) {
		PorterStemmer stemmer = new PorterStemmer();

		StringBuffer stemedLine = new StringBuffer();
		String[] words = QueryString.split(" ");
		for (int i = 0; i < words.length; i++) {
			stemmer.setCurrent(words[i]);
			stemmer.stem();
			stemedLine.append(stemmer.getCurrent() + " ");
		}
		return stemedLine.toString();
	}

}
