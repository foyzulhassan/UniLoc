package com.build.logdiff;

import com.github.difflib.DiffUtils;

import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.Delta;
import com.github.difflib.patch.Patch;
import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.diff.EditScript;
import org.apache.commons.text.diff.StringsComparator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class LogDiff {

	public static void main(String[] args) {
		findDiff();
	}

	public static void findDiff() {
		// List<String> original = Files.readAllLines(new
		// File(ORIGINAL).toPath());
		// List<String> revised = Files.readAllLines(new
		// File(RIVISED).toPath());

		// Compute diff. Get the Patch object. Patch is the container for
		// computed deltas.
		Patch<String> patch = null;
		try {
			patch = DiffUtils.diff("4-12-2018: What went wrong 12", "4-13-2018: What went wrong 13");
		} catch (DiffException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Compute diff. Get the Patch object. Patch is the container for
		// computed deltas.

		for (Delta<String> delta : patch.getDeltas()) {
			System.out.println(delta);
		}

		TextCompareComponent obj2 = new TextCompareComponent("4-12-2018: What went wrong 12",
				"4-13-2018: What went wrong 13");

		ArrayList<String> str2 = obj2.longestCommonSubsequence("4-12-2018: What went wrong 12 testin testing",
				"4-13-2018: What went wrong 13 testing");

		String str3 = obj2.markTextDifferences("4-12-2018: What went wrong 12 testin testing",
				"4-13-2018: What went wrong 13 testing", str2, "#99FFCC", "#99FFCC");
		System.out.println(str2);
		System.out.println(str3);

		DiffRowGenerator generator = DiffRowGenerator.create().showInlineDiffs(true).inlineDiffByWord(true).build();

		List<DiffRow> rows = null;
		try {
			rows = generator.generateDiffRows(
					Arrays.asList("This is a test senctence.", "This is the second line.", "ABC",
							"And here is the finish."),
					Arrays.asList("This is a test for diffutils senctence shakil.", "This is the second line.",
							"And here is the finish."));
		} catch (DiffException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("|original|new|");
		System.out.println("|--------|---|");
		for (DiffRow row : rows) {
			System.out.println("|" + row.getOldLine() + "|" + row.getNewLine() + "|");
			// row.
			if (row.getOldLine().length() >= 0 && row.getNewLine().length() >= 0) {

				String str = row.getOldLine();
				str = str.replace("<span class=\"editOldInline\">", "");
				str = str.replace("</span>", "");
				System.out.println("NEW LINES----------->" + str);

				List<String> inserted = getTagValues(row.getNewLine());

				for (int i = 0; i < inserted.size(); i++) {
					System.out.println(inserted.get(i));
				}

			}
		}

	}

	public static String getLogDiff(List<String> passlines, List<String> faillines) {

		StringBuilder strbuilder = new StringBuilder();

		DiffRowGenerator generator = DiffRowGenerator.create().showInlineDiffs(true).inlineDiffByWord(true).build();

		List<DiffRow> rows = null;
		try {
			rows = generator.generateDiffRows(passlines, faillines);
		} catch (DiffException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String cascadedstr="";
		for (DiffRow row : rows) {
			if (row.getOldLine().length() <= 0 && row.getNewLine().length() >= 0) {

				String str = row.getNewLine();
				str=cascadedstr+str;
				str = str.replace("<span class=\"editNewInline\">", "");
				str = str.replace("</span>", "");
				strbuilder.append(str);
				strbuilder.append("\n");
				cascadedstr="";

			} else if (row.getOldLine().length() >= 0 && row.getNewLine().length() >= 0) {
				List<String> inserted = getTagValues(cascadedstr+row.getNewLine());
				boolean match = false;

				int i=0;
				while(i < inserted.size()) {
					if ((inserted.get(i).length() > 15 || inserted.size()>2)) {
						//strbuilder.append(inserted.get(i));
						//strbuilder.append(" ");
						match = true;
						break;
					}
					i++;
				}

				if (match) {
					String str = cascadedstr+row.getNewLine();
					str = str.replace("<span class=\"editNewInline\">", "");
					str = str.replace("</span>", "");
					strbuilder.append(str);
					strbuilder.append("\n");
					cascadedstr="";
				}
				else if(row.getNewLine().contains("<span class=\"editNewInline\">") && !row.getNewLine().contains("</span>"))
				{
					cascadedstr=cascadedstr+row.getNewLine();
				}		
				
				else if((cascadedstr+row.getNewLine().toLowerCase()).contains("fail"))
				{
					String str = row.getNewLine();
					str = str.replace("<span class=\"editNewInline\">", "");
					str = str.replace("</span>", "");
					strbuilder.append(str);
					strbuilder.append("\n");
					cascadedstr="";
				}
			}
		}

		return strbuilder.toString();
	}
	
	public static String getLogSame(List<String> passlines, List<String> faillines) {

		StringBuilder strbuilder = new StringBuilder();

		DiffRowGenerator generator = DiffRowGenerator.create().showInlineDiffs(true).inlineDiffByWord(true).build();

		List<DiffRow> rows = null;
		try {
			rows = generator.generateDiffRows(passlines, faillines);
		} catch (DiffException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String cascadedstr="";
		for (DiffRow row : rows) {
			if (row.getOldLine().length() <= 0 && row.getNewLine().length() >= 0) {

				String str = row.getNewLine();
				str=cascadedstr+str;
				str = str.replace("<span class=\"editNewInline\">", "");
				str = str.replace("</span>", "");

				cascadedstr="";

			} else if (row.getOldLine().length() >= 0 && row.getNewLine().length() >= 0) {
				List<String> inserted = getTagValues(cascadedstr+row.getNewLine());
				boolean match = false;

				int i=0;
				while(i < inserted.size()) {
					if ((inserted.get(i).length() > 15 || inserted.size()>2)) {
						//strbuilder.append(inserted.get(i));
						//strbuilder.append(" ");
						match = true;
						break;
					}
					i++;
				}

				if (match) {
					String str = cascadedstr+row.getNewLine();
					str = str.replace("<span class=\"editNewInline\">", "");
					str = str.replace("</span>", "");
					cascadedstr="";
				}
				else if(row.getNewLine().contains("<span class=\"editNewInline\">") && !row.getNewLine().contains("</span>"))
				{
					cascadedstr=cascadedstr+row.getNewLine();
				}		
				
				else if((cascadedstr+row.getNewLine().toLowerCase()).contains("fail"))
				{
					String str = row.getNewLine();
					str = str.replace("<span class=\"editNewInline\">", "");
					cascadedstr="";
				}
				else
				{
					String str = cascadedstr+row.getNewLine();
					strbuilder.append(str);
					strbuilder.append("\n");
					cascadedstr="";
					
				}
			}
		}

		return strbuilder.toString();
	}

	private static final Pattern TAG_REGEX = Pattern.compile("<span class=\"editNewInline\">(.+?)</span>");
	private static final Pattern TAG_REGEX1 = Pattern.compile("<span class=\"editNewInline\">(.+?)</span>");
	private static final Pattern TAG_REGEX2 = Pattern.compile("<span class=\"editNewInline\">(.+?)");

	private static List<String> getTagValues(final String str) {
		final List<String> tagValues = new ArrayList<String>();
		Matcher matcher = TAG_REGEX.matcher(str);
		while (matcher.find()) {
			tagValues.add(matcher.group(1));
		}
		
		if(tagValues.size()<=0)
		{
			matcher = TAG_REGEX1.matcher(str);
			while (matcher.find()) {
				tagValues.add(matcher.group(1));
			}
			
		}
		return tagValues;
	}
	
	private static List<String> getStartTagValues(final String str) {
		final List<String> tagValues = new ArrayList<String>();
		Matcher matcher = TAG_REGEX2.matcher(str);
		while (matcher.find()) {
			tagValues.add(matcher.group(1));
		}		
		return tagValues;
	}

}
