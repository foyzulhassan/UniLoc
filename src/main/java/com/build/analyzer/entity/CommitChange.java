package com.build.analyzer.entity;

public class CommitChange {
	private int importChange;
	private int classChange;
	private int methodChange;
	private int methodBodyChange;
	private int fieldChange;	
	private int buildFileChange;
	
	
	public CommitChange()
	{
		this.importChange=0;
		this.classChange=0;
		this.methodChange=0;
		this.methodBodyChange=0;
		this.fieldChange=0;
		this.buildFileChange=0;
		
		
	}
	public int getImportChange() {
		return importChange;
	}
	public void setImportChange(int importChange) {
		this.importChange = importChange;
	}
	public int getClassChange() {
		return classChange;
	}
	public void setClassChange(int classChange) {
		this.classChange = classChange;
	}
	public int getMethodChange() {
		return methodChange;
	}
	public void setMethodChange(int methodChange) {
		this.methodChange = methodChange;
	}
	
	public int getBuildFileChange() {
		return buildFileChange;
	}
	public void setBuildFileChange(int buildFileChange) {
		this.buildFileChange = buildFileChange;
	}	

	public int getMethodBodyChange() {
		return methodBodyChange;
	}
	public void setMethodBodyChange(int methodBodyChange) {
		this.methodBodyChange = methodBodyChange;
	}
	public int getFieldChange() {
		return fieldChange;
	}
	public void setFieldChange(int fieldChange) {
		this.fieldChange = fieldChange;
	}
}
