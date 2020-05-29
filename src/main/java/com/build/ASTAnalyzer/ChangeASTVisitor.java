package com.build.ASTAnalyzer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.*;

public class ChangeASTVisitor extends ASTVisitor {

	private List<String> astTextList;
	private List<String> importList;
	private List<String> className;
	public List<String> getClassName() {
		return className;
	}

	public void setClassName(List<String> className) {
		this.className = className;
	}

	private List<String> methodList;
	private List<String> decList;
	
	
	public List<String> getImportList() {
		return importList;
	}

	public void setImportList(List<String> importList) {
		this.importList = importList;
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

	public void setAstTextList(List<String> astTextList) {
		this.astTextList = astTextList;
	}

	public List<String> getAstTextList() {
		return astTextList;
	}

	public ChangeASTVisitor() {
		astTextList = new ArrayList<String>();
		
		importList= new ArrayList<String>();
		className= new ArrayList<String>();
		methodList= new ArrayList<String>();
		decList= new ArrayList<String>();
	}

	// For imports
	public boolean visit(ImportDeclaration node) {
		
		if(!node.toString().contains("import org.junit") && !node.toString().contains("import java."))
		{
			astTextList.add(node.toString());
			importList.add(node.toString());
		}
		return super.visit(node);
	}

	public boolean visit(TypeDeclaration node) {
		astTextList.add(node.getName().toString());
		className.add(node.getName().toString());
		return super.visit(node);
	}

	public boolean visit(MethodDeclaration node) {
		astTextList.add(node.getName().toString());
		methodList.add(node.getName().toString());
		return super.visit(node);
	}

	public boolean visit(FieldDeclaration node) {
		astTextList.add(node.toString());
		decList.add(node.toString());
		return super.visit(node);
	}

	public boolean visit(VariableDeclarationStatement node) {
		for(int i = 0; i < node.fragments().size(); ++i)
        {
            VariableDeclarationFragment frag = (VariableDeclarationFragment)node.fragments().get(i);
            astTextList.add(frag.getName().toString());
            decList.add(frag.getName().toString());
        }
		return super.visit(node);
	}

	public boolean visit(AnnotationTypeDeclaration node) {
		astTextList.add(node.toString());
		decList.add(node.toString());
		return super.visit(node);
	}

	public boolean visit(AnnotationTypeMemberDeclaration node) {
		astTextList.add(node.toString());
		decList.add(node.toString());
		return super.visit(node);
	}

	public boolean visit(AnonymousClassDeclaration node) {
		astTextList.add(node.toString());
		decList.add(node.toString());
		return super.visit(node);
	}

	public boolean visit(EnumConstantDeclaration node) {
		astTextList.add(node.toString());
		decList.add(node.toString());
		return super.visit(node);
	}

	public boolean visit(EnumDeclaration node) {
		astTextList.add(node.toString());
		decList.add(node.toString());
		return super.visit(node);
	}

	public boolean visit(PackageDeclaration node) {
		astTextList.add(node.getName().toString());
		importList.add(node.getName().toString());
		return super.visit(node);
	}
	
//	public boolean visit(MethodInvocation node)
//	{
//		astTextList.add(node.getName().toString());
//		return super.visit(node);
//	}
}

