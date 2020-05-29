package com.build.analyzer.diff.gradle;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;

public class SASTNode {
	ASTNode parent;
	ASTNode current;
	int id;
	
	public SASTNode(ASTNode parent,ASTNode current,int id)
	{
		if(parent==null || current==null)
		{
			int test=1;
		}
		
		if(current instanceof DeclarationExpression)
		{
			int test=1;
		}
		
		if(parent instanceof DeclarationExpression)
		{
			int test=1;
		}
		this.parent=parent;
		this.current=current;
		this.id=id;
	}
	
	public ASTNode getParent()
	{
		return parent;		
	}
	
	public ASTNode getCurrent()
	{
		return current;
	}
	
	public int getId()
	{
		return id;
	}

}
