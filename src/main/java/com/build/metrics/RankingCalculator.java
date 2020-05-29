package com.build.metrics;

import java.util.ArrayList;
import java.util.List;

public class RankingCalculator {

	public double getMeanAveragePrecision(ArrayList<String> rankedfiles, String[] actualfiles) {
		List<Double> precissions = new ArrayList<Double>();
		double avg = 0.0;
		int count = 1;
		int matched = 0;

		for (int index = 0; index < rankedfiles.size(); index++) {

			int fileindex = 0;
			boolean matcheflag = false;
			while (fileindex < actualfiles.length) {

				if (rankedfiles.get(index).equals(actualfiles[fileindex])) {
					matched++;
					matcheflag = true;
					break;
				}
				fileindex++;
			}

			if (matcheflag) {
				Double precession = (double) ((double)matched / (double)count);
				precissions.add(precession);
			}

			count++;
		}

		double sum = 0.0;
		for (int index = 0; index < precissions.size(); index++) {
			sum = sum + precissions.get(index);
		}

		if (precissions.size() > 0)
			avg = (double) (sum / precissions.size());

		return avg;
	}
	
	public int getTopN(ArrayList<String> rankedfiles, String[] actualfiles) {
		int firstindex = 999999999;
		int fileindex = 0;

		fileindex = 0;
		while (fileindex < actualfiles.length) {
			int tindex = 0;
			while (tindex < rankedfiles.size()) {

				String file = rankedfiles.get(tindex);

				if (file.equals(actualfiles[fileindex])) {
					if (firstindex > tindex) {
						firstindex = tindex;
						break;
					}
				}

				tindex++;
			}

			fileindex++;
		}

		return firstindex+1;
	}

	public double getMeanAverageReciprocal(ArrayList<String> rankedfiles, String[] actualfiles) {
//		List<Double> precissions = new ArrayList<Double>();
//		double avg = 0.0;
//
//		for (int index = 0; index < actualfiles.length; index++) {
//			int fileindex = 0;
//			boolean matcheflag = false;
//
//			while (fileindex < rankedfiles.size()) {
//
//				if (rankedfiles.get(fileindex).equals(actualfiles[index])) {
//
//					matcheflag = true;
//					index=999999;
//					break;
//				}
//				fileindex++;
//			}
//
//			if (matcheflag) {
//				Double precession = (double) ((double)1.00 / (double)(fileindex+1));
//				precissions.add(precession);
//				avg=precession;
//				
//				break;
//			}
//
//		}
//
//		double sum = 0.0;
//		for (int index = 0; index < precissions.size(); index++) {
//			sum = sum + precissions.get(index);
//		}

//		if (actualfiles.length > 0)
//			avg = (double) (sum / actualfiles.length);
		
		double avg = 0.0;

		int topn=getTopN(rankedfiles,actualfiles);
		
		avg = (double) ((double)1.00 / (double)(topn));
		
		return avg;
	}
	
	public double calculateNDCG(ArrayList<String> predictionData, String[] actualfiles)
	{
		return NDCG.calculateNDCG(predictionData,actualfiles);
	}

}
