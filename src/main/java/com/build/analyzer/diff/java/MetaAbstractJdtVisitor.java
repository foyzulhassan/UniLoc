package com.build.analyzer.diff.java;

import java.util.ArrayDeque;
import java.util.Deque;

import com.github.gumtreediff.gen.jdt.cd.EntityType;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Statement;

import com.github.gumtreediff.gen.jdt.cd.EntityType;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;

public abstract class MetaAbstractJdtVisitor extends ASTVisitor {

	protected TreeContext context = new TreeContext();

	private Deque<ITree> trees = new ArrayDeque<>();

	private CompilationUnit unit;

	private static int nodeid = 0;

	public MetaAbstractJdtVisitor(CompilationUnit unit) {
		super(true);
		this.unit = unit;

	}

	public TreeContext getTreeContext() {
		return context;
	}

	protected void pushNode(ASTNode n, String label) {
		int type = n.getNodeType();
		String typeName = n.getClass().getSimpleName();

		int lineNumber = unit.getLineNumber(unit.getExtendedStartPosition(n));
		push(type, typeName, label, lineNumber, n.getLength());

	}

	protected void pushFakeNode(EntityType n, int startPosition, int length) {
		int type = -n.ordinal(); // Fake types have negative types (but does it
									// matter ?)
		String typeName = n.name();
		push(type, typeName, "", startPosition, length);
	}

	private void push(int type, String typeName, String label, int startPosition, int length) {
		ITree t = context.createTree(type, label, typeName);

		t.setPos(startPosition);
		t.setLength(length);
		t.setId(nodeid);

		nodeid++;

		if (trees.isEmpty())
			context.setRoot(t);
		else {
			ITree parent = trees.peek();
			t.setParentAndUpdateChildren(parent);
		}

		trees.push(t);
	}

	protected ITree getCurrentParent() {
		return trees.peek();
	}

	protected void popNode() {
		trees.pop();
	}
}
