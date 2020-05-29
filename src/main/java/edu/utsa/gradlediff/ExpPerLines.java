package edu.utsa.gradlediff;

import java.util.ArrayList;
import java.util.List;

public class ExpPerLines {
	private int lineNo;
	
	private List<GradleChange> changesPerLine;
	
	public ExpPerLines(int lineno)
	{
		this.lineNo=lineno;
		changesPerLine=new ArrayList<GradleChange>();
	}

	public int getLineNo() {
		return lineNo;
	}

	public void setLineNo(int lineNo) {
		this.lineNo = lineNo;
	}

	public List<GradleChange> getChangesPerLine() {
		return changesPerLine;
	}

	public void setChangesPerLine(List<GradleChange> changesPerLine) {
		this.changesPerLine = changesPerLine;
	}
	
	public void addChangetoLine(GradleChange change)
	{
		changesPerLine.add(change);
	}

}
