package com.build.ASTAnalyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class JavaTypeWiseASTParser {
	public JavaASTEntity parseJavaMethodDecs(File sourcefile) {

		ASTParser parser = ASTParser.newParser(AST.JLS8);
		String source = null;
		try {
			source = readFile(sourcefile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		parser.setSource(source.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		ChangeASTVisitor visitor=new ChangeASTVisitor();
		try
		{
			CompilationUnit unit = (CompilationUnit) parser.createAST(new NullProgressMonitor());	
			
			unit.accept(visitor);
		}catch(Exception e){
			System.out.println(e.getMessage());
		}

		List<String> astList=visitor.getAstTextList();
		
		JavaASTEntity javaentity=new JavaASTEntity();	
		
		javaentity.setAllAST(astList);
		javaentity.setClassName(visitor.getClassName());
		javaentity.setMethodList(visitor.getMethodList());	
		javaentity.setImportList(visitor.getImportList());
		javaentity.setClassName(visitor.getDecList());
		
		return javaentity;
	}
	
	public JavaASTEntity parseJavaMethodDecs(String source) {

		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setSource(source.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		ChangeASTVisitor visitor=new ChangeASTVisitor();
		try
		{
			CompilationUnit unit = (CompilationUnit) parser.createAST(new NullProgressMonitor());	
			
			unit.accept(visitor);
		}catch(Exception e){
			System.out.println("parseJavaMethodDecs Error:->"+e.getMessage());
		}

		List<String> astList=visitor.getAstTextList();
		
		JavaASTEntity javaentity=new JavaASTEntity();	
		
		javaentity.setAllAST(astList);
		javaentity.setClassName(visitor.getClassName());
		javaentity.setMethodList(visitor.getMethodList());	
		javaentity.setImportList(visitor.getImportList());
		javaentity.setDecList(visitor.getDecList());
		
		return javaentity;
	}
	
	

	public String readFile(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		try {
			StringBuilder content = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				content.append(line);
				content.append('\n');
			}
			return content.toString();
		} finally {
			if (reader != null)
				reader.close();
		}
	}
}
