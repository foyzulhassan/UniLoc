package com.build.logfilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.queryparser.classic.ParseException;

import com.build.analyzer.config.Config;
import com.build.analyzer.entity.Gradlebuildfixdata;
import com.build.docsim.CosineDocumentSimilarity;
import com.build.docsimilarity.DocWordFreqGenerator;
import com.build.docsimilarity.WordFrequencyData;

public class FilterLogText {

	public FilterLogText() {

	}

	public String performFiltering(Gradlebuildfixdata buildfixdata) {
		DocWordFreqGenerator freqgen = new DocWordFreqGenerator();

		ArrayList<WordFrequencyData> tfdatafail = freqgen.getTFData(buildfixdata, false);

		ArrayList<WordFrequencyData> tfdatapass = freqgen.getTFData(buildfixdata, true);

		double failmedian = getMedianFreq(tfdatafail);
		double passmedian = getMedianFreq(tfdatapass);

		ArrayList<String> frqfailwords = getWordsWithHigerFreq(tfdatafail, failmedian);
		ArrayList<String> frqpasswords = getWordsWithHigerFreq(tfdatapass, passmedian);

		ArrayList<String> regexps = new ArrayList<String>();

		for (int index = 0; index < frqfailwords.size(); index++) {
			String word = frqfailwords.get(index);

			// if(frqpasswords.contains(word))
			// {
			String exp = "\\s+" + word + "\\s+";
			regexps.add(exp);
			// }
		}

		String buildlog = buildfixdata.getFailChange();

		// For Cleanup
		String regex = "([a-zA-Z]+)(\\d)";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(buildlog);
		while (m.find()) {
			buildlog = buildlog.replaceAll(m.group(), m.group(1) + " " + m.group(2));
		}

		// buildlog = buildlog.toLowerCase();
		for (int index = 0; index < regexps.size(); index++) {
			buildlog = buildlog.replaceAll(regexps.get(index), "");
		}

		return buildlog;

	}

	public String performFilteringV3(Gradlebuildfixdata buildfixdata) {

		StringBuilder strbuilder = new StringBuilder();

		String faillog = buildfixdata.getFailChange();

		String passlog = buildfixdata.getFixChange();

		// For Cleanup
		String regex = "([a-zA-Z]+)(\\d)";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(faillog);
		while (m.find()) {
			faillog = faillog.replaceAll(m.group(), m.group(1) + " " + m.group(2));
		}
		// For Cleanup
		regex = "([a-zA-Z]+)(\\d)";
		p = Pattern.compile(regex);
		m = p.matcher(faillog);
		while (m.find()) {
			passlog = passlog.replaceAll(m.group(), m.group(1) + " " + m.group(2));
		}

		List<String> buildfaillines = new ArrayList<String>(Arrays.asList(faillog.split("\n")));

		List<String> buildpasslines = new ArrayList<String>(Arrays.asList(passlog.split("\n")));
		
		buildfaillines=getListAfterRemoveDuplicate(buildfaillines);
		buildpasslines=getListAfterRemoveDuplicate(buildpasslines);	
		

		CosineDocumentSimilarity cosdocsim = new CosineDocumentSimilarity(buildpasslines, buildfaillines);

		List<String> filteredlines = new ArrayList<String>();

		for (int failindex = 0; failindex < buildfaillines.size(); failindex++) {
			
			int passindex = 0;
			boolean matchfound = false;
			while (passindex < buildpasslines.size()) {

				double simval = 0.0;
				try {
					simval = cosdocsim.getCosineSimilarity(passindex, failindex);
					if (simval > 0.7) {
						matchfound = true;
						break;
					}
					// }
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				passindex++;
			} // end while

			if (!matchfound) {
				filteredlines.add(buildfaillines.get(failindex));
			}
		}

		for (int index = 0; index < filteredlines.size(); index++) {
			strbuilder.append(filteredlines.get(index));
			strbuilder.append("\n");
		}

		return strbuilder.toString();
	}
	
	public String performFilteringOnSimValue(Gradlebuildfixdata buildfixdata) {

		StringBuilder strbuilder = new StringBuilder();

		String faillog = buildfixdata.getFailPartSim();

		List<String> buildfaillines = new ArrayList<String>(Arrays.asList(faillog.split("\n")));

		for (int failindex = 0; failindex < buildfaillines.size(); failindex++) {

			String strline = buildfaillines.get(failindex);

			// Checking if line has sufficient length
			if (strline.length() >= Config.lineSimSeperator.length()) {

				String[] strparts = strline.split(Config.lineSimSeperator);

				if (strparts[1] != null && Double.parseDouble(strparts[1]) <= Config.thresholdForSimFilter) {

					if (strparts[0] != null) {
						String strpart1 = strparts[0];
						strpart1 = strpart1.trim();
						if (strpart1.length() > 0) {
							strbuilder.append(strpart1);
							strbuilder.append("\n");
						}
					}

				}

			}

		}
		
//		String str=strbuilder.toString();
//		
//		if(str.length()<=0)
//		{
//			for (int failindex = 0; failindex < buildfaillines.size(); failindex++) {
//
//				String strline = buildfaillines.get(failindex);
//
//				// Checking if line has sufficient length
//				if (strline.length() >= Config.lineSimSeperator.length()) {
//
//					String[] strparts = strline.split(Config.lineSimSeperator);
//
//					if (strparts[1] != null && Double.parseDouble(strparts[1]) <= 0.5) {
//
//						if (strparts[0] != null) {
//							strbuilder.append(strparts[0]);
//							strbuilder.append("\n");
//						}
//
//					}
//
//				}
//
//			}
//			
//		}

		return strbuilder.toString();
	}
	
	public List<String> getListFilteringOnSimValue(Gradlebuildfixdata buildfixdata) {

		StringBuilder strbuilder = new StringBuilder();

		String faillog = buildfixdata.getFailPartSim();

		List<String> buildfaillines = new ArrayList<String>(Arrays.asList(faillog.split("\n")));
		List<String> strlist = new ArrayList<>();

		for (int failindex = 0; failindex < buildfaillines.size(); failindex++) {

			String strline = buildfaillines.get(failindex);

			// Checking if line has sufficient length
			if (strline.length() >= Config.lineSimSeperator.length()) {

				String[] strparts = strline.split(Config.lineSimSeperator);

				if (strparts[1] != null && Double.parseDouble(strparts[1]) <= Config.thresholdForSimFilter) {

					if (strparts[0] != null && strparts[0].length() <500) {
						String strpart1 = strparts[0];
						strpart1 = strpart1.trim();
						if (strpart1.length() > 0) {
							strlist.add(strpart1);
						}
					}

				}

			}

		}

		return strlist;
	}

	public String performFilteringOld(Gradlebuildfixdata buildfixdata) {

		StringBuilder strbuilder = new StringBuilder();

		String faillog = buildfixdata.getFailChange();

		String passlog = buildfixdata.getFixChange();

		List<String> buildfaillines = new ArrayList<String>(Arrays.asList(faillog.split("\n")));

		List<String> buildpasslines = new ArrayList<String>(Arrays.asList(passlog.split("\n")));
		HashMap<String, Double> linesimmap = new HashMap<String, Double>();

		for (int failindex = 0; failindex < buildfaillines.size(); failindex++) {

			List<Double> simlist = new ArrayList<Double>();
			String failline = buildfaillines.get(failindex);

			// if(failline!=null && failline.length()>10 && failline.contains("
			// "))
			// {
			for (int passindex = 0; passindex < buildpasslines.size(); passindex++) {

				String passline = buildpasslines.get(passindex);
				double simval = 0.0;
				try {

					// if(passline!=null && passline.length()>10 &&
					// passline.contains(" "))
					// {
					simval = CosineDocumentSimilarity.getCosineSimilarityFromText(failline, passline);
					// }
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				simlist.add(simval);
				// }

				Double maxsim = Collections.max(simlist);
				linesimmap.put(failline, maxsim);
				simlist.clear();
			}
		}

		for (String name : linesimmap.keySet()) {
			String key = name.toString();
			Double value = linesimmap.get(key);

			System.out.println(key + "--->" + value);

			if (value < 0.7) {
				strbuilder.append(key);
				strbuilder.append("\n");
			}

		}

		return strbuilder.toString();

	}

	public String performFilteringV2(Gradlebuildfixdata buildfixdata) {

		StringBuilder strbuilder = new StringBuilder();

		String faillog = buildfixdata.getFailChange();

		String passlog = buildfixdata.getFixChange();

		List<String> buildfaillines = new ArrayList<String>(Arrays.asList(faillog.split("\n")));

		List<String> buildpasslines = new ArrayList<String>(Arrays.asList(passlog.split("\n")));
		HashMap<String, Double> linesimmap = new HashMap<String, Double>();

		LogLineSimGenerator linesim = new LogLineSimGenerator(buildpasslines);

		for (int failindex = 0; failindex < buildfaillines.size(); failindex++) {

			double maxsim = 0.0;
			try {
				maxsim = linesim.getTopSimValue(buildfaillines.get(failindex));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			linesimmap.put(buildfaillines.get(failindex), maxsim);

		}

		for (String name : linesimmap.keySet()) {
			String key = name.toString();
			Double value = linesimmap.get(key);

			System.out.println(key + "--->" + value);

			if (value < 0.7) {
				strbuilder.append(key);
				strbuilder.append("\n");
			}

		}

		return strbuilder.toString();

	}

	private double getMedianFreq(ArrayList<WordFrequencyData> tfdata) {
		double dmedian = 0.0;
		List<Double> freqs = new ArrayList<Double>();

		for (int index = 0; index < tfdata.size(); index++) {
			WordFrequencyData worddata = tfdata.get(index);
			freqs.add(worddata.getData());
		}

		Collections.sort(freqs);

		dmedian = median(freqs);

		return dmedian;
	}

	public double median(List<Double> a) {
		int middle = a.size() / 2;

		if (a.size() % 2 == 1) {
			return a.get(middle);
		} else {
			return (a.get(middle - 1) + a.get(middle)) / 2.0;
		}
	}

	private ArrayList<String> getWordsWithHigerFreq(ArrayList<WordFrequencyData> tfdata, double dmedian) {
		ArrayList<String> wordlist = new ArrayList<String>();

		for (int index = 0; index < tfdata.size(); index++) {
			WordFrequencyData worddata = tfdata.get(index);
			if (worddata.getData() > dmedian) {
				wordlist.add(worddata.getWord());
			}
		}

		return wordlist;
	}
	
	public List<String> getListAfterRemoveDuplicate(List<String> originallist)
	{
		
		List<String> withoutduplicate=new ArrayList<String>();
		
		for(int index=0;index<originallist.size();index++)
		{
			String str=originallist.get(index);
			
			if(!withoutduplicate.contains(str))
			{
				withoutduplicate.add(str);
			}			
		}
		
		return withoutduplicate;
	}

}
