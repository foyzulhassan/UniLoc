package edu.utsa.gradlediff;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ClosureListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.ElvisOperatorExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.RangeExpression;
import org.codehaus.groovy.ast.expr.SpreadExpression;
import org.codehaus.groovy.ast.expr.SpreadMapExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.UnaryMinusExpression;
import org.codehaus.groovy.ast.expr.UnaryPlusExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.BreakStatement;
import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ContinueStatement;
import org.codehaus.groovy.ast.stmt.DoWhileStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.stmt.SynchronizedStatement;
import org.codehaus.groovy.ast.stmt.ThrowStatement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.codehaus.groovy.classgen.BytecodeExpression;

import com.github.gumtreediff.io.LineReader;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;

public class GradleTreeVisitor extends CodeVisitorSupport {

	private Map<ASTNode, ITree> trees;
	public Map<Integer, ASTNode> nodes;
	public Map<Integer, ASTNode> getNodes() {
		return nodes;
	}

	private TreeContext context;
	private static int nodeid = 0;
	//// private LineReader lr;

	public GradleTreeVisitor(SASTNode root) {
		nodeid = 0;
		trees = new HashMap<>();
		nodes= new HashMap<>();
		context = new TreeContext();
		ITree tree = buildTree(root);
		context.setRoot(tree);
		// lr=new LineReader();

	}

	public TreeContext getTree(SASTNode root) {
		return context;
	}

	public boolean visit(SASTNode node) {
		if (node.getParent() == null)
			return true;
		else {
			ITree t = buildTree(node);
			ITree p = trees.get(node.getParent());

			if (p == null || t == null)
				return true;

			if (node.current instanceof ConstantExpression) {

				t.setLabel(node.current.getText());
			}

			if (node.current.getNodeMetaData("lbl") != null) {

				t.setLabel((String) node.current.getNodeMetaData("lbl"));
				t.setMetadata("lbl", node.current.getNodeMetaData("lbl"));

			}

			if (node.current.getNodeMetaData("block") != null) {
				// if (node.current instanceof MethodCallExpression) {
				//
				// MethodCallExpression exp=(MethodCallExpression) node.current;

				// t.setLabel(exp.getMethodAsString());
				t.setMetadata("block", node.current.getNodeMetaData("block"));
				// }
			}

			if (node.current.getNodeMetaData("task") != null) {
				// if (node.current instanceof MethodCallExpression) {
				//
				// MethodCallExpression exp=(MethodCallExpression) node.current;

				// t.setLabel((String) node.current.getNodeMetaData("task"));
				t.setMetadata("task", node.current.getNodeMetaData("task"));
				// }
			}

			if (node.current.getNodeMetaData("ST") != null) {
				// if (node.current instanceof ExpressionStatement) {
				//
				// ExpressionStatement exp=(ExpressionStatement) node.current;

				// t.setLabel((String) node.current.getNodeMetaData("ST"));
				t.setMetadata("ST", node.current.getNodeMetaData("ST"));
				// }
			}

			p.addChild(t);
			// node.current.getNodeMetaData(key)
			// ExpressionStatement
			return true;
		}
	}

	private ITree buildTree(SASTNode node) {

		String test = node.current.getClass().getSimpleName();

		ITree t = context.createTree(node.getId(), ITree.NO_LABEL, node.current.getClass().getSimpleName());

		int pos = node.current.getLineNumber();
		int end = node.current.getLastLineNumber() + node.current.getLastColumnNumber();

		int length = end - pos;
		t.setPos(pos);
		t.setLength(length);
		t.setId(nodeid);

		// t.setPos(node.current.getLineNumber());
		// t.setLength(node.current.getLastColumnNumber()-node.current.getColumnNumber());
		trees.put(node.getCurrent(), t);
		nodes.put(nodeid, node.getCurrent());
		nodeid++;
		return t;
	}

}
