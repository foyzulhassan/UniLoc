package com.build.analyzer.diff.java;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.*;

import com.github.gumtreediff.gen.jdt.AbstractJdtVisitor;

public class MetaJdtVisitor  extends MetaAbstractJdtVisitor {
    public MetaJdtVisitor(CompilationUnit unit) {
        super(unit);
    }
    
   

    @Override
    public void preVisit(ASTNode n) {
        pushNode(n, getLabel(n));
    }

    protected String getLabel(ASTNode n) {
        if (n instanceof Name) return ((Name) n).getFullyQualifiedName();
        if (n instanceof Type) return n.toString();
        if (n instanceof Modifier) return n.toString();
        if (n instanceof StringLiteral) return ((StringLiteral) n).getEscapedValue();
        if (n instanceof NumberLiteral) return ((NumberLiteral) n).getToken();
        if (n instanceof CharacterLiteral) return ((CharacterLiteral) n).getEscapedValue();
        if (n instanceof BooleanLiteral) return ((BooleanLiteral) n).toString();
        if (n instanceof InfixExpression) return ((InfixExpression) n).getOperator().toString();
        if (n instanceof PrefixExpression) return ((PrefixExpression) n).getOperator().toString();
        if (n instanceof PostfixExpression) return ((PostfixExpression) n).getOperator().toString();
        if (n instanceof Assignment) return ((Assignment) n).getOperator().toString();
        if (n instanceof TextElement) return n.toString();
        if (n instanceof TagElement) return ((TagElement) n).getTagName();
        if (n instanceof BodyDeclaration) return ((BodyDeclaration) n).toString();
        if (n instanceof Statement) return ((Statement) n).toString();
        if (n instanceof Expression) return ((Expression) n).toString();
       
       
      

        return "**";
    }

    @Override
    public boolean visit(TagElement e) {
        return true;
    }

    @Override
    public boolean visit(QualifiedName name) {
        return false;
    }

    @Override
    public void postVisit(ASTNode n) {
        popNode();
    }
}