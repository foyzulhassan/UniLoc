package com.build.analyzer.diff.gradle;

public class GradleChange {
	String fileName;
	int lineNumber;
	String operationName;
	String nodeType;
	String nodeExp;
	String parentType;
	String parentExp;	
	String blockName;
	String taskName;
	String statementExp	;
	
	
	public GradleChange(String filename,int lineNumber, String operationName, String nodeType, String nodeExp, String parentType,String parentExp, String blockName,
			String taskName, String statementExp) {
		super();
		this.fileName=filename;
		this.lineNumber = lineNumber;
		this.operationName = operationName;
		this.nodeType = nodeType;
		this.nodeExp=nodeExp;
		this.parentType=parentType;
		this.parentExp = parentExp;
		this.blockName = blockName;
		this.taskName = taskName;
		this.statementExp = statementExp;
	}
	
	public GradleChange() {
		super();
		this.fileName="";
		this.lineNumber = -1;
		this.operationName = "";
		this.nodeType = "";
		this.nodeExp="";
		this.parentType ="";
		this.parentExp = "";
		this.blockName ="";
		this.taskName = "";
		this.statementExp = "";
	}
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public String getNodeExp() {
		return nodeExp;
	}

	public void setNodeExp(String nodeExp) {
		this.nodeExp = nodeExp;
	}

	public String getParentType() {
		return parentType;
	}

	public void setParentType(String parentType) {
		this.parentType = parentType;
	}

	public String getParentExp() {
		return parentExp;
	}

	public void setParentExp(String parentExp) {
		this.parentExp = parentExp;
	}

	public String getStatementExp() {
		return statementExp;
	}

	public void setStatementExp(String statementExp) {
		this.statementExp = statementExp;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	public String getOperationName() {
		return operationName;
	}

	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

	public String getNodeType() {
		return nodeType;
	}

	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}

	public String getParentName() {
		return parentExp;
	}

	public void setParentName(String parentName) {
		this.parentExp = parentName;
	}

	public String getBlockName() {
		return blockName;
	}

	public void setBlockName(String blockName) {
		this.blockName = blockName;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
	

}
