package com.build.java.ast.selection;

import java.util.ArrayList;
import java.util.List;

public class JavaSelectedASTEntities {
	private List<String> importList;
	private List<String> classList;
	private List<String> methodList;
	private List<String> decList;
	
	public JavaSelectedASTEntities()
	{
		importList=new ArrayList<>();
		classList=new ArrayList<>();
		methodList=new ArrayList<>();
		decList=new ArrayList<>();
	}
	
	public List<String> getImportList() {
		return importList;
	}

	public void setImportList(List<String> importList) {
		this.importList = importList;
	}

	public List<String> getClassList() {
		return classList;
	}

	public void setClassList(List<String> classList) {
		this.classList = classList;
	}

	public List<String> getMethodList() {
		return methodList;
	}

	public void setMethodList(List<String> methodList) {
		this.methodList = methodList;
	}

	public List<String> getDecList() {
		return decList;
	}

	public void setDecList(List<String> decList) {
		this.decList = decList;
	}

}
