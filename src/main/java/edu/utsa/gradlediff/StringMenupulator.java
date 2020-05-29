package edu.utsa.gradlediff;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.build.analyzer.config.Config;



public class StringMenupulator {

	public StringMenupulator() {

	}

	private boolean isStringContainsDollarBrace(String text) {
		boolean flag = false;

		if (text.contains("${") && text.contains("}")) {
			flag = true;
		}

		return flag;
	}

	// private String getReplaceDollarBrace(String text)
	// {
	// String stamptext = "";
	// boolean isbegin = true;
	// boolean found = false;
	// int counttrack=0;
	// int count=getQueteCount(text);
	//
	// if(text.length()>0)
	// stamptext += text.charAt(0);
	//
	// for (int i = 1; i < text.length(); i++) {
	// found = false;
	//
	// if (text.charAt(i-1) == '$' && text.charAt(i) == '{') {
	// found = true;
	// }
	//
	// else if (text.charAt(i) == '}') {
	// found = true;
	// }
	//
	// else {
	// stamptext += text.charAt(i);
	// }
	//
	// if (isbegin == true && found == true) {
	// if (text.charAt(i) == '{') {
	// stamptext += text.charAt(i) + Config.beginCurlBrace;
	// counttrack++;
	// }
	//
	// isbegin = false;
	// } else if (isbegin == false && found == true) {
	//
	// if (text.charAt(i) == '}') {
	// stamptext +=Config.endCurlBrace+ text.charAt(i) ;
	// }
	//
	// isbegin = true;
	// }
	//
	// }
	//
	// return stamptext;
	// }

	private String getReplaceDollarBrace(String text) {
		String stamptext = "";
		boolean isbegin = true;
		boolean found = false;
		int counttrack = 0;
		int count = getQueteCount(text);
		boolean brokenpair = false;

		if (text.length() > 0)
			stamptext += text.charAt(0);

		for (int i = 1; i < text.length(); i++) {
			found = false;

			if (text.charAt(i - 1) == '$' && text.charAt(i) == '{') {
				found = true;
				brokenpair = true;
			}

			else if (text.charAt(i) == '}') {
				if (brokenpair == true) {
					found = true;
				} else {
					stamptext += text.charAt(i);
				}
			}

			else {
				stamptext += text.charAt(i);
			}

			if (isbegin == true && found == true) {
				if (text.charAt(i) == '{') {
					stamptext += text.charAt(i) + Config.beginCurlBrace;
					counttrack++;
				}

				isbegin = false;
			} else if (isbegin == false && found == true) {

				if (text.charAt(i) == '}') {
					stamptext += Config.endCurlBrace + text.charAt(i);
					brokenpair = false;
				}

				isbegin = true;
			}

		}

		return stamptext;
	}

	private boolean isStringContainsQuete(String text) {
		boolean flag = false;

		if (text.contains("'") || text.contains("\"")) {
			flag = true;
		}

		if (text.contains("$")) {
			{
				int qpos = text.indexOf("'");
				int dpos = text.indexOf("$");

				if (dpos < qpos)
					flag = false;
				else
					flag = true;

			}
		}

		return flag;
	}

	private String cleanUpSpecialCases(String text) {
		String newtext = "";

		if (text.contains("(#edc#\""))
			text = text.replace("(#edc#\"", "(\"#bdc#");

		if (text.contains("\"#bdc#)"))
			text = text.replace("\"#bdc#)", "#edc#\")");
		
		
			
		if(text.contains("<<<<<<<"))
		{
			text=" ";
		}
		if(text.contains("======="))
		{
			text=" ";
		}
		if(text.contains(">>>>>>>"))
		{
			text=" ";
		}	
		if(text.contains("\"#bdc#${bcubrrootProject.projectDirecubr}/quasar-core/build/libs/quasar-core-${bcubrversionecubr}${bcubrext.java8 ? #edc#\"-jdk8\"#bdc# : #edc#\"\"#bdc#ecubr}.jar#edc#\""))
		{
			text="ext.quasarJar = \"#bdc#${bcubrrootProject.projectDirecubr}/quasar-core/build/libs/quasar-core-${bcubrversionecubr}${bcubrext.java8 ? \"#bdc#-jdk8#edc#\" : \"#bdc##edc#\" }.jar#edc#\""; 
		}

		return text;

	}

	private String getStampedString(String text) {
		String stamptext = "";
		boolean isbegin = true;
		boolean found = false;
		int counttrack = 0;
		int count = getQueteCount(text);

		for (int i = 0; i < text.length(); i++) {
			found = false;

			if (text.charAt(i) == '\'' || text.charAt(i) == '\"') {
				found = true;
			}

			else {
				stamptext += text.charAt(i);
			}

			if (isbegin == true && found == true) {
				if (text.charAt(i) == '\'') {
					stamptext += text.charAt(i) + Config.beginSingleQuete;
					counttrack++;
				}

				else if (text.charAt(i) == '\"') {
					stamptext += text.charAt(i) + Config.beginDoubleQuete;
					counttrack++;
				}
				isbegin = false;
			} else if (isbegin == false && found == true) {

				if (text.charAt(i) == '\'') {
					stamptext += Config.endSingleQuete + text.charAt(i);
				}

				else if (text.charAt(i) == '\"') {
					stamptext += Config.endDoubleQuete + text.charAt(i);
				}

				isbegin = true;
			}

		}

		return stamptext;
	}

	private int getQueteCount(String text) {
		int count = 0;

		for (int i = 0; i < text.length(); i++) {

			if (text.charAt(i) == '\'' || text.charAt(i) == '\"') {
				count++;
			}
		}

		return count;
	}

	private boolean isStampedStringforMethodDef(String text) {

		boolean flag = false;

		Pattern whitespace = Pattern.compile("def\\s+[a-zA-Z](");

		Matcher matcher = whitespace.matcher(text);

		while (matcher.find()) {
			flag = true;
		}

		return flag;

	}

	private boolean isStringContainsMethodDef(String text) {

		boolean flag = false;

		Pattern whitespace = Pattern.compile("def\\s+[a-zA-Z]*[(]");

		Matcher matcher = whitespace.matcher(text);

		while (matcher.find()) {
			flag = true;
		}

		return flag;

	}

	private String getStampedStringforMethodDef(String text) {

		String modtext = text;

		Pattern whitespace = Pattern.compile("def\\s+[a-zA-Z]*[(]");

		Matcher matcher = whitespace.matcher(modtext);

		while (matcher.find()) {
			modtext = modtext.replaceFirst("def ", Config.defTag);
		}

		return modtext;

	}

	private boolean isStringContainsImport(String text) {

		boolean flag = false;

		Pattern whitespace = Pattern.compile("import\\s");

		Matcher matcher = whitespace.matcher(text);

		while (matcher.find()) {
			flag = true;
		}

		return flag;

	}

	private String getStampedStringforImport(String text) {

		String modtext = text;

		Pattern whitespace = Pattern.compile("import\\s");

		Matcher matcher = whitespace.matcher(modtext);

		while (matcher.find()) {
			modtext = modtext.replaceFirst("import", Config.importTag);
		}

		return modtext;

	}

	private boolean isStringContainsForwardQuete(String text) {
		boolean flag = false;

		if ((text.contains("http") || text.contains("/*") || text.contains("*/") || text.contains("//"))
				&& text.contains("/")) {
			flag = false;
		}

		else if ((text.contains("'") || text.contains("\"")) && text.contains("/")) {
			flag = false;
		} else if (text.contains("/"))
			flag = true;

		return flag;
	}

	private String getFwdSlashStampedString(String text) {
		String stamptext = "";
		boolean isbegin = true;
		boolean found = false;
		int counttrack = 0;

		for (int i = 0; i < text.length(); i++) {
			found = false;

			if (text.charAt(i) == '/') {
				found = true;
			}

			else {
				stamptext += text.charAt(i);
			}

			if (isbegin == true && found == true) {
				if (text.charAt(i) == '/') {
					stamptext += text.charAt(i) + Config.beginfwdSlash;
					counttrack++;
				}

				isbegin = false;
			} else if (isbegin == false && found == true) {

				if (text.charAt(i) == '/') {
					stamptext += Config.endfwdSlash + text.charAt(i);
				}

				isbegin = true;
			}

		}

		return stamptext;
	}

	public String getMarkedString(String str) {

//		if (isStringContainsQuete(str)) {
//			str = getStampedString(str);
//		}

		///////////////////////////////////////////////////////////////////////edited here
//		if (isStringContainsMethodDef(str))
//			str = getStampedStringforMethodDef(str);
//
//		if (isStringContainsImport(str))
//			str = getStampedStringforImport(str);
		/////////////////////////////////////////////////////////////////////////////////

//		if (isStringContainsDollarBrace(str))
//			str = getReplaceDollarBrace(str);

//		if (isStringContainsForwardQuete(str))
//			str = getFwdSlashStampedString(str);

//		str = cleanUpSpecialCases(str);

		return str;
	}
}
