package com.build.util;

import java.util.List;

public class StringAnalyzer {

	public static boolean isArrayContainsString(String[] strArray, String str) {
		int index = 0;
		boolean ret = false;

		while (index < strArray.length) {

			if (str.equals(strArray[index])) {

				ret = true;
				break;
			}
			index++;
		}

		return ret;
	}

	public static String getStringFromList(List<String> strList) {

		StringBuilder strbuild = new StringBuilder();

		for (int index = 0; index < strList.size(); index++) {
			strbuild.append(strList.get(index));
			strbuild.append(" ");
		}

		return strbuild.toString();
	}

}
