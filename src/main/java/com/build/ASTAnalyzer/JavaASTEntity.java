package com.build.ASTAnalyzer;

import java.util.ArrayList;
import java.util.List;

public class JavaASTEntity {
	private List<String> astTextList;	
	private List<String> importList;
	private List<String> className;
	private List<String> methodList;
	private List<String> decList;
	private List<String> allAST;
	
	public JavaASTEntity()
	{
		astTextList=new ArrayList<>();
		importList=new ArrayList<>();
		className=new ArrayList<>();
		methodList=new ArrayList<>();
		decList=new ArrayList<>();
	}
	
	public List<String> getAstTextList() {
		return astTextList;
	}
	public void setAstTextList(List<String> astTextList) {
		this.astTextList = astTextList;
	}
	public List<String> getImportList() {
		return importList;
	}
	public void setImportList(List<String> importList) {
		this.importList = importList;
	}
	public List<String> getClassName() {
		return className;
	}
	public void setClassName(List<String> className) {
		this.className = className;
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
	public List<String> getAllAST() {
		return allAST;
	}
	public void setAllAST(List<String> allAST) {
		this.allAST = allAST;
	}
	
	

}
