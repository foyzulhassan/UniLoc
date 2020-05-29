package edu.utsa.gradlediff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

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
import org.codehaus.groovy.ast.expr.NamedArgumentListExpression;
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
import org.codehaus.groovy.ast.stmt.EmptyStatement;
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

import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;

import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.InnerClassNode;

/**
 * @author Lovett Li
 */
public class GradleSubProjDependencyVisitor extends CodeVisitorSupport {

	private int dependenceLineNum = -1;
	private int columnNum = -1;
	// private List<GradleDependency> dependencies = new ArrayList<>();

	private Map<Statement, Statement> trees;
	private TreeContext context;
	List<SASTNode> astList = new ArrayList<SASTNode>();

	List<String> subprojects = new ArrayList<>();

	private String parentProject;

	private Map<String, List<String>> projectDependencyies;
	
	private String rootProjectName;

	public String getRootProjectName() {
		return rootProjectName;
	}

	public Map<String, List<String>> getProjectDependencyies() {
		return projectDependencyies;
	}

	public List<String> getSubprojects() {
		return subprojects;
	}

	// List<String> projdependency = new ArrayList<>();
	//
	// public List<String> getProjdependency() {
	// return projdependency;
	// }

	public GradleSubProjDependencyVisitor(ASTNode parent, String strparent) {

		SASTNode node = new SASTNode(null, parent, TypeUtil.getExpressionType(parent));
		astList.add(node);
		this.parentProject = strparent;
		this.projectDependencyies = new HashMap<String, List<String>>();
	}

	@Override
	public void visitMethodCallExpression(MethodCallExpression call) {

		boolean blockflag = false;
		boolean taskflag = false;
		String taskname = "";

		if (!(call.getMethodAsString().equals("buildscript"))) {
			if (call.getMethodAsString().equals("dependencies")) {
				if (dependenceLineNum == -1) {
					dependenceLineNum = call.getLastLineNumber();
				}
			}

			// call.g
		}

		if (call.getMethodAsString().equals("mavenCentral")) {
			int test = 1;
		}

		if (call.getMethodAsString().equals("task")) {
			Expression arguments = call.getArguments();
			if (arguments != null && arguments instanceof ArgumentListExpression) {
				ArgumentListExpression ale = (ArgumentListExpression) arguments;
				if (ale != null) {
					List<Expression> expressions = ale.getExpressions();
					if (expressions.size() > 0 && expressions.get(0) instanceof MethodCallExpression) {
						MethodCallExpression methodExpression = (MethodCallExpression) expressions.get(0);
						taskname = methodExpression.getMethodAsString();
						taskflag = true;
					}

				}
			}

			if (taskflag == true) {
				call.setNodeMetaData("task", "task " + taskname);
			}

			else {
				call.setNodeMetaData("task", "task");
			}

		}

		Expression arguments = call.getArguments();
		if (arguments != null && arguments instanceof ArgumentListExpression) {
			ArgumentListExpression ale = (ArgumentListExpression) arguments;
			if (ale != null) {
				List<Expression> expressions = ale.getExpressions();
				if (expressions.size() == 1 && expressions.get(0) instanceof ClosureExpression) {
					ClosureExpression closureExpression = (ClosureExpression) expressions.get(0);
					Statement block = closureExpression.getCode();
					if (block instanceof BlockStatement) {

						blockflag = true;
					}
				}
			}

		}


		if (call.getObjectExpression() != null) {
			SASTNode node = new SASTNode(call, call.getObjectExpression(),
					TypeUtil.getExpressionType(call.getObjectExpression()));
			astList.add(node);

		}

		if (call.getMethod() != null) {
			SASTNode node = new SASTNode(call, call.getMethod(), TypeUtil.getExpressionType(call.getMethod()));
			astList.add(node);
		}

		if (call.getArguments() != null) {
			SASTNode node = new SASTNode(call, call.getArguments(), TypeUtil.getExpressionType(call.getArguments()));
			astList.add(node);

		}

		if (call.getNodeMetaData("lbl") == null)
			call.setNodeMetaData("lbl", call.getMethodAsString());

		if (call.getMethodAsString().equals("include")) {
			Expression argumentsinfo = call.getArguments();
			if (argumentsinfo != null && argumentsinfo instanceof ArgumentListExpression) {
				ArgumentListExpression ale = (ArgumentListExpression) argumentsinfo;
				if (ale != null) {
					List<Expression> expressions = ale.getExpressions();

					for (Expression expr : expressions) {
						if (expr != null && expr instanceof ConstantExpression) {
							if(!expr.getText().startsWith(":"))
								subprojects.add(":"+expr.getText());
							else
								subprojects.add(expr.getText());
						}
					}

				}
			}
		}

		// This part is for compile or testCompile Project Start
		if ((call.getMethodAsString().equals("compile") || call.getMethodAsString().equals("testCompile"))
				&& call.getNodeMetaData("visited") == null) {
			List<String> dependency = getCompileTestCompileDependency(call);

			if (dependency != null & dependency.size() > 0) {
				Map<String, List<String>> deps = new HashMap<String, List<String>>();
				deps.put(parentProject, dependency);
				mergeDependencyMap(deps);

			}
		}

		// This part is for compile or testCompile Project End

		// This part is for following case
		// project(':app') {
		// dependencies {
		// compile project(':common'), project(':api')
		// compile 'org.apache.logging.log4j:log4j-core:2.6.2'
		// }
		if (call.getMethodAsString().equals("project")) {
			Map<String, List<String>> mapdependency = getProjectDependencyForCompile(call);
		}

		if ((call.getMethodAsString().equals("provided")) && call.getNodeMetaData("visited") == null) {
			List<String> dependency = getProvidedDependency(call);

			if (dependency != null & dependency.size() > 0) {
				Map<String, List<String>> deps = new HashMap<String, List<String>>();
				deps.put(parentProject, dependency);
				mergeDependencyMap(deps);

			}
		}

		if ((call.getMethodAsString().equals("runtime") || call.getMethodAsString().equals("testRuntime"))
				&& call.getNodeMetaData("visited") == null) {
			List<String> dependency = getRuntimeTestRuntimeDependency(call);

			if (dependency != null & dependency.size() > 0) {
				Map<String, List<String>> deps = new HashMap<String, List<String>>();
				deps.put(parentProject, dependency);
				mergeDependencyMap(deps);

			}
		}

		super.visitMethodCallExpression(call);

	}

	private void getStatements() {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitArgumentlistExpression(ArgumentListExpression ale) {

		if (ale.getNodeMetaData("lbl") == null)
			ale.setNodeMetaData("lbl", ale.getText());

		List<Expression> expressions = ale.getExpressions();

		if (expressions.size() == 1 && expressions.get(0) instanceof ConstantExpression) {
			String depStr = expressions.get(0).getText();
			String[] deps = depStr.split(":");

			if (deps.length == 3) {
				// dependencies.add(new GradleDependency(deps[0], deps[1],
				// deps[2]));
			}
		}

		for (Expression exp : ale.getExpressions()) {

			SASTNode node = new SASTNode(ale, exp, TypeUtil.getExpressionType(exp));
			astList.add(node);

		}

		super.visitArgumentlistExpression(ale);
	}

	@Override
	public void visitClosureExpression(ClosureExpression expression) {

		if (expression.getNodeMetaData("lbl") == null)
			expression.setNodeMetaData("lbl", expression.getText());
		if (dependenceLineNum != -1 && expression.getLineNumber() == expression.getLastLineNumber()) {
			columnNum = expression.getLastColumnNumber();
		}

		SASTNode node = new SASTNode(expression, expression.getCode(),
				TypeUtil.getExpressionType(expression.getCode()));
		astList.add(node);

		super.visitClosureExpression(expression);
	}

	@Override
	public void visitMapExpression(MapExpression expression) {

		if (expression.getNodeMetaData("lbl") == null)
			expression.setNodeMetaData("lbl", expression.getText());
		List<MapEntryExpression> mapEntryExpressions = expression.getMapEntryExpressions();
		Map<String, String> dependenceMap = new HashMap<String, String>();

		for (MapEntryExpression mapEntryExpression : mapEntryExpressions) {
			String key = mapEntryExpression.getKeyExpression().getText();
			String value = mapEntryExpression.getValueExpression().getText();
			dependenceMap.put(key, value);
			// System.out.println("MapExpression~~~~~~~"+key+":"+value);
		}

		// dependencies.add(new GradleDependency(dependenceMap));
		// System.out.println("MapExpression");

		for (Expression exp : expression.getMapEntryExpressions()) {

			SASTNode node = new SASTNode(expression, exp, TypeUtil.getExpressionType(exp));
			astList.add(node);

		}

		super.visitMapExpression(expression);
	}

	public int getDependenceLineNum() {
		return dependenceLineNum;
	}

	public int getColumnNum() {
		return columnNum;
	}

	// public List<GradleDependency> getDependencies() {
	// return dependencies;
	// }

	@Override
	public void visitBlockStatement(BlockStatement statements) {
		// TODO Auto-generated method stub

		// statements.setNodeMetaData("lbl", statements.getText());

		for (Statement statement : statements.getStatements()) {

			SASTNode node = new SASTNode(statements, statement, TypeUtil.getExpressionType(statement));
			astList.add(node);
			// statement.visit(this);

		}

		super.visitBlockStatement(statements);
	}

	@Override
	public void visitForLoop(ForStatement forLoop) {
		// TODO Auto-generated method stub
		if (forLoop.getNodeMetaData("lbl") == null)
			forLoop.setNodeMetaData("lbl", forLoop.getText());

		SASTNode node = new SASTNode(forLoop, forLoop.getCollectionExpression(),
				TypeUtil.getExpressionType(forLoop.getCollectionExpression()));
		astList.add(node);

		SASTNode node1 = new SASTNode(forLoop, forLoop.getLoopBlock(),
				TypeUtil.getExpressionType(forLoop.getLoopBlock()));
		astList.add(node1);

		super.visitForLoop(forLoop);

	}

	@Override
	public void visitWhileLoop(WhileStatement loop) {
		// TODO Auto-generated method stub
		if (loop.getNodeMetaData("lbl") == null)
			loop.setNodeMetaData("lbl", loop.getText());
		SASTNode node = new SASTNode(loop, loop.getBooleanExpression(),
				TypeUtil.getExpressionType(loop.getBooleanExpression()));
		astList.add(node);

		SASTNode node1 = new SASTNode(loop, loop.getLoopBlock(), TypeUtil.getExpressionType(loop.getLoopBlock()));
		astList.add(node1);

		super.visitWhileLoop(loop);

	}

	@Override
	public void visitDoWhileLoop(DoWhileStatement loop) {
		// TODO Auto-generated method stub
		if (loop.getNodeMetaData("lbl") == null)
			loop.setNodeMetaData("lbl", loop.getText());
		SASTNode node = new SASTNode(loop, loop.getLoopBlock(), TypeUtil.getExpressionType(loop.getLoopBlock()));
		astList.add(node);

		SASTNode node1 = new SASTNode(loop, loop.getBooleanExpression(),
				TypeUtil.getExpressionType(loop.getBooleanExpression()));
		astList.add(node1);

		super.visitDoWhileLoop(loop);

	}

	@Override
	public void visitIfElse(IfStatement ifElse) {
		// TODO Auto-generated method stub
		if (ifElse.getNodeMetaData("lbl") == null)
			ifElse.setNodeMetaData("lbl", ifElse.getText());
		SASTNode node = new SASTNode(ifElse, ifElse.getBooleanExpression(),
				TypeUtil.getExpressionType(ifElse.getBooleanExpression()));
		astList.add(node);

		SASTNode node1 = new SASTNode(ifElse, ifElse.getIfBlock(), TypeUtil.getExpressionType(ifElse.getIfBlock()));
		astList.add(node1);

		super.visitIfElse(ifElse);

	}

	@Override
	public void visitExpressionStatement(ExpressionStatement statement) {
		// statement.get
		// TODO Auto-generated method stub
		// System.out.println("ExpressionStatement-->"+statement.getText());
		// statement.getExpression().visit(this);
		String label = statement.getExpression().getText();

		Expression exp = statement.getExpression();

		if (exp instanceof StaticMethodCallExpression) {
			int index = label.indexOf('.');

			label = label.substring(index + 1, label.length());
		}

		if (statement.getNodeMetaData("ST") == null)
			statement.setNodeMetaData("ST", label);
		SASTNode node = new SASTNode(statement, statement.getExpression(),
				TypeUtil.getExpressionType(statement.getExpression()));
		astList.add(node);
		super.visitExpressionStatement(statement);
	}

	@Override
	public void visitReturnStatement(ReturnStatement statement) {
		// TODO Auto-generated method stub
		if (statement.getNodeMetaData("lbl") == null)
			statement.setNodeMetaData("lbl", statement.getText());
		SASTNode node = new SASTNode(statement, statement.getExpression(),
				TypeUtil.getExpressionType(statement.getExpression()));
		astList.add(node);
		super.visitReturnStatement(statement);

	}

	@Override
	public void visitAssertStatement(AssertStatement statement) {
		// TODO Auto-generated method stub
		if (statement.getNodeMetaData("lbl") == null)
			statement.setNodeMetaData("lbl", statement.getText());
		SASTNode node = new SASTNode(statement, statement.getBooleanExpression(),
				TypeUtil.getExpressionType(statement.getBooleanExpression()));
		astList.add(node);

		SASTNode node1 = new SASTNode(statement, statement.getMessageExpression(),
				TypeUtil.getExpressionType(statement.getMessageExpression()));
		astList.add(node1);
		super.visitAssertStatement(statement);

	}

	@Override
	public void visitTryCatchFinally(TryCatchStatement finally1) {
		// TODO Auto-generated method stub

		if (finally1.getNodeMetaData("lbl") == null)
			finally1.setNodeMetaData("lbl", finally1.getText());

		for (CatchStatement catchStatement : finally1.getCatchStatements()) {
			SASTNode node = new SASTNode(finally1, catchStatement, TypeUtil.getExpressionType(catchStatement));
			astList.add(node);
		}
		Statement finallyStatement = finally1.getFinallyStatement();

		if (finallyStatement != null) {
			SASTNode node = new SASTNode(finally1, finallyStatement, TypeUtil.getExpressionType(finallyStatement));
			astList.add(node);
		}

		super.visitTryCatchFinally(finally1);
	}

	@Override
	public void visitSwitch(SwitchStatement statement) {
		// TODO Auto-generated method stub

		if (statement.getNodeMetaData("lbl") == null)
			statement.setNodeMetaData("lbl", statement.getText());

		for (CaseStatement caseStatement : statement.getCaseStatements()) {
			SASTNode node = new SASTNode(statement, caseStatement, TypeUtil.getExpressionType(caseStatement));
			astList.add(node);
		}

		Statement dftstmt = statement.getDefaultStatement();

		if (dftstmt != null) {
			SASTNode node = new SASTNode(statement, dftstmt, TypeUtil.getExpressionType(dftstmt));
			astList.add(node);
		}

		super.visitSwitch(statement);
	}

	@Override
	public void visitCaseStatement(CaseStatement statement) {
		// TODO Auto-generated method stub
		if (statement.getNodeMetaData("lbl") == null)
			statement.setNodeMetaData("lbl", statement.getText());

		SASTNode node = new SASTNode(statement, statement.getExpression(),
				TypeUtil.getExpressionType(statement.getExpression()));
		astList.add(node);

		SASTNode node1 = new SASTNode(statement, statement.getCode(), TypeUtil.getExpressionType(statement.getCode()));
		astList.add(node1);
		;

		super.visitCaseStatement(statement);
	}

	@Override
	public void visitBreakStatement(BreakStatement statement) {
		// TODO Auto-generated method stub
		if (statement.getNodeMetaData("lbl") == null)
			statement.setNodeMetaData("lbl", statement.getText());

		super.visitBreakStatement(statement);
	}

	@Override
	public void visitContinueStatement(ContinueStatement statement) {

		if (statement.getNodeMetaData("lbl") == null)
			statement.setNodeMetaData("lbl", statement.getText());

		super.visitContinueStatement(statement);
	}

	@Override
	public void visitThrowStatement(ThrowStatement statement) {
		// TODO Auto-generated method stub
		if (statement.getNodeMetaData("lbl") == null)
			statement.setNodeMetaData("lbl", statement.getText());

		SASTNode node = new SASTNode(statement, statement.getExpression(),
				TypeUtil.getExpressionType(statement.getExpression()));
		astList.add(node);

		super.visitThrowStatement(statement);
	}

	@Override
	public void visitSynchronizedStatement(SynchronizedStatement statement) {
		// TODO Auto-generated method stub
		if (statement.getNodeMetaData("lbl") == null)
			statement.setNodeMetaData("lbl", statement.getText());

		SASTNode node = new SASTNode(statement, statement.getExpression(),
				TypeUtil.getExpressionType(statement.getExpression()));
		astList.add(node);

		SASTNode node1 = new SASTNode(statement, statement.getCode(), TypeUtil.getExpressionType(statement.getCode()));
		astList.add(node1);

		super.visitSynchronizedStatement(statement);
	}

	@Override
	public void visitCatchStatement(CatchStatement statement) {
		// TODO Auto-generated method stub
		if (statement.getNodeMetaData("lbl") == null)
			statement.setNodeMetaData("lbl", statement.getText());

		SASTNode node1 = new SASTNode(statement, statement.getCode(), TypeUtil.getExpressionType(statement.getCode()));
		astList.add(node1);

		super.visitCatchStatement(statement);
	}

	@Override
	public void visitStaticMethodCallExpression(StaticMethodCallExpression expression) {
		// TODO Auto-generated method stub
		// System.out.println("StaticMethodCallExpression");
		if (expression.getNodeMetaData("lbl") == null)
			expression.setNodeMetaData("lbl", expression.getText());
		SASTNode node = new SASTNode(expression, expression.getArguments(),
				TypeUtil.getExpressionType(expression.getArguments()));
		astList.add(node);

		super.visitStaticMethodCallExpression(expression);
	}

	@Override
	public void visitConstructorCallExpression(ConstructorCallExpression expression) {
		// TODO Auto-generated method stub
		if (expression.getNodeMetaData("lbl") == null)
			expression.setNodeMetaData("lbl", expression.getText());
		SASTNode node = new SASTNode(expression, expression.getArguments(),
				TypeUtil.getExpressionType(expression.getArguments()));
		astList.add(node);
		super.visitConstructorCallExpression(expression);
	}

	@Override
	public void visitTernaryExpression(TernaryExpression expression) {
		// TODO Auto-generated method stub
		if (expression.getNodeMetaData("lbl") == null)
			expression.setNodeMetaData("lbl", expression.getText());
		SASTNode node = new SASTNode(expression, expression.getBooleanExpression(),
				TypeUtil.getExpressionType(expression.getBooleanExpression()));
		astList.add(node);

		SASTNode node1 = new SASTNode(expression, expression.getTrueExpression(),
				TypeUtil.getExpressionType(expression.getTrueExpression()));
		astList.add(node1);

		SASTNode node2 = new SASTNode(expression, expression.getFalseExpression(),
				TypeUtil.getExpressionType(expression.getFalseExpression()));
		astList.add(node2);

		super.visitTernaryExpression(expression);

	}

	@Override
	public void visitShortTernaryExpression(ElvisOperatorExpression expression) {
		// TODO Auto-generated method stub
		if (expression.getNodeMetaData("lbl") == null)
			expression.setNodeMetaData("lbl", expression.getText());

		super.visitShortTernaryExpression(expression);
	}

	@Override
	public void visitBinaryExpression(BinaryExpression expression) {
		// TODO Auto-generated method stub
		// System.out.println("BinaryExpression###"+expression.getText());
		if (expression.getNodeMetaData("lbl") == null)
			expression.setNodeMetaData("lbl", expression.getText());

		SASTNode node = new SASTNode(expression, expression.getLeftExpression(),
				TypeUtil.getExpressionType(expression.getLeftExpression()));
		astList.add(node);

		SASTNode node1 = new SASTNode(expression, expression.getRightExpression(),
				TypeUtil.getExpressionType(expression.getRightExpression()));
		astList.add(node1);
		
		Expression left=expression.getLeftExpression();
		
		Expression right=expression.getRightExpression();
		
		if(left instanceof PropertyExpression)
		{
			PropertyExpression leftprop=(PropertyExpression) left;
			
			if(leftprop.getText().toLowerCase().contains("rootproject.name"))
			{
				if(right instanceof ConstantExpression)
				{
					ConstantExpression rightconst=(ConstantExpression) right;
					
					rootProjectName=rightconst.getText();
				}
			}
		}

		super.visitBinaryExpression(expression);
	}

	@Override
	public void visitPrefixExpression(PrefixExpression expression) {
		// TODO Auto-generated method stub
		if (expression.getNodeMetaData("lbl") == null)
			expression.setNodeMetaData("lbl", expression.getText());

		SASTNode node = new SASTNode(expression, expression.getExpression(),
				TypeUtil.getExpressionType(expression.getExpression()));
		astList.add(node);

		super.visitPrefixExpression(expression);
	}

	@Override
	public void visitPostfixExpression(PostfixExpression expression) {
		// TODO Auto-generated method stub
		if (expression.getNodeMetaData("lbl") == null)
			expression.setNodeMetaData("lbl", expression.getText());

		SASTNode node = new SASTNode(expression, expression.getExpression(),
				TypeUtil.getExpressionType(expression.getExpression()));
		astList.add(node);

		super.visitPostfixExpression(expression);
	}

	@Override
	public void visitBooleanExpression(BooleanExpression expression) {
		// TODO Auto-generated method stub
		// System.out.println("BooleanExpression&&&&&&&&&&&&&"+expression.getText());
		if (expression.getNodeMetaData("lbl") == null)
			expression.setNodeMetaData("lbl", expression.getText());

		SASTNode node = new SASTNode(expression, expression.getExpression(),
				TypeUtil.getExpressionType(expression.getExpression()));
		astList.add(node);

		super.visitBooleanExpression(expression);
	}

	@Override
	public void visitTupleExpression(TupleExpression expression) {
		// TODO Auto-generated method stub
		if (expression.getNodeMetaData("lbl") == null)
			expression.setNodeMetaData("lbl", expression.getText());

		List<? extends Expression> list = expression.getExpressions();
		if (list != null) {
			for (Expression expression1 : list) {
				if (expression1 instanceof SpreadExpression) {
					Expression spread = ((SpreadExpression) expression1).getExpression();
					SASTNode node = new SASTNode(expression, spread, TypeUtil.getExpressionType(spread));
					astList.add(node);
					// spread.visit(this);
				}
				// else {
				//
				// SASTNode node = new SASTNode(expression, expression1,
				// TypeUtil.getExpressionType(expression1));
				// astList.add(node);
				// // expression1.visit(this);
				// }
			}
		}

		super.visitTupleExpression(expression);
	}

	@Override
	public void visitMapEntryExpression(MapEntryExpression expression) {
		// TODO Auto-generated method stub
		// System.out.println("BooleanExpression");
		if (expression.getNodeMetaData("lbl") == null)
			expression.setNodeMetaData("lbl", expression.getText());
		SASTNode node = new SASTNode(expression, expression.getKeyExpression(),
				TypeUtil.getExpressionType(expression.getKeyExpression()));
		astList.add(node);

		SASTNode node1 = new SASTNode(expression, expression.getValueExpression(),
				TypeUtil.getExpressionType(expression.getValueExpression()));
		astList.add(node1);

		super.visitMapEntryExpression(expression);
	}

	@Override
	public void visitListExpression(ListExpression expression) {
		// TODO Auto-generated method stub
		if (expression.getNodeMetaData("lbl") == null)
			expression.setNodeMetaData("lbl", expression.getText());
		List<? extends Expression> list = expression.getExpressions();
		if (list != null) {
			for (Expression expression1 : list) {
				if (expression1 instanceof SpreadExpression) {
					Expression spread = ((SpreadExpression) expression1).getExpression();
					SASTNode node = new SASTNode(expression, spread, TypeUtil.getExpressionType(spread));
					astList.add(node);
					spread.visit(this);
				} else {

					SASTNode node = new SASTNode(expression, expression1, TypeUtil.getExpressionType(expression1));
					astList.add(node);
					// expression1.visit(this);
				}
			}
		}

		super.visitListExpression(expression);
	}

	@Override
	public void visitRangeExpression(RangeExpression expression) {
		// TODO Auto-generated method stub
		if (expression.getNodeMetaData("lbl") == null)
			expression.setNodeMetaData("lbl", expression.getText());

		SASTNode node = new SASTNode(expression, expression.getFrom(),
				TypeUtil.getExpressionType(expression.getFrom()));
		astList.add(node);

		SASTNode node1 = new SASTNode(expression, expression.getTo(), TypeUtil.getExpressionType(expression.getTo()));
		astList.add(node1);

		super.visitRangeExpression(expression);
	}

	@Override
	public void visitPropertyExpression(PropertyExpression expression) {
		// TODO Auto-generated method stub
		if (expression.getNodeMetaData("lbl") == null)
			expression.setNodeMetaData("lbl", expression.getText());
		SASTNode node = new SASTNode(expression, expression.getObjectExpression(),
				TypeUtil.getExpressionType(expression.getObjectExpression()));
		astList.add(node);

		SASTNode node1 = new SASTNode(expression, expression.getProperty(),
				TypeUtil.getExpressionType(expression.getProperty()));
		astList.add(node1);

		super.visitPropertyExpression(expression);
	}

	@Override
	public void visitAttributeExpression(AttributeExpression attributeExpression) {
		// TODO Auto-generated method stub
		if (attributeExpression.getNodeMetaData("lbl") == null)
			attributeExpression.setNodeMetaData("lbl", attributeExpression.getText());
		SASTNode node = new SASTNode(attributeExpression, attributeExpression.getObjectExpression(),
				TypeUtil.getExpressionType(attributeExpression.getObjectExpression()));
		astList.add(node);

		SASTNode node1 = new SASTNode(attributeExpression, attributeExpression.getProperty(),
				TypeUtil.getExpressionType(attributeExpression.getProperty()));
		astList.add(node1);

		super.visitAttributeExpression(attributeExpression);
	}

	@Override
	public void visitFieldExpression(FieldExpression expression) {

		if (expression.getNodeMetaData("lbl") == null)
			expression.setNodeMetaData("lbl", expression.getText());

		super.visitFieldExpression(expression);
		// TODO Auto-generated method stub

	}

	@Override
	public void visitMethodPointerExpression(MethodPointerExpression expression) {
		// TODO Auto-generated method stub
		if (expression.getNodeMetaData("lbl") == null)
			expression.setNodeMetaData("lbl", expression.getText());

		System.out.println("MethodPointerExpression");
		super.visitMethodPointerExpression(expression);
	}

	@Override
	public void visitConstantExpression(ConstantExpression expression) {
		// TODO Auto-generated method stub

		if (expression.getValue() == null)
			return;

		if (expression.getNodeMetaData("lbl") == null)
			expression.setNodeMetaData("lbl", expression.getText());

		super.visitConstantExpression(expression);
	}

	@Override
	public void visitClassExpression(ClassExpression expression) {
		// TODO Auto-generated method stub

		// if (classParent != null) {
		if (expression.getNodeMetaData("lbl") == null)
			expression.setNodeMetaData("lbl", expression.getText());
		SASTNode node = new SASTNode(null, expression, TypeUtil.getExpressionType(expression));
		astList.add(node);
		// }

		super.visitClassExpression(expression);
	}

	@Override
	public void visitVariableExpression(VariableExpression expression) {
		// TODO Auto-generated method stub

		if (expression.getNodeMetaData("lbl") == null)
			expression.setNodeMetaData("lbl", expression.getText());

		super.visitVariableExpression(expression);
	}

	@Override
	public void visitDeclarationExpression(DeclarationExpression expression) {
		// TODO Auto-generated method stub
		if (expression.getNodeMetaData("lbl") == null)
			expression.setNodeMetaData("lbl", expression.getText());
		// SASTNode node = new SASTNode(expression,
		// expression.getLeftExpression(),
		// TypeUtil.getExpressionType(expression.getLeftExpression()));
		// astList.add(node);
		//
		// SASTNode node1 = new SASTNode(expression,
		// expression.getRightExpression(),
		// TypeUtil.getExpressionType(expression.getRightExpression()));
		// astList.add(node1);

		super.visitDeclarationExpression(expression);
	}

	@Override
	public void visitGStringExpression(GStringExpression expression) {
		// TODO Auto-generated method stub
		if (expression.getNodeMetaData("lbl") == null)
			expression.setNodeMetaData("lbl", expression.getText());
		List<? extends Expression> list = expression.getStrings();
		if (list != null) {
			for (Expression exp : list) {
				if (exp instanceof SpreadExpression) {
					Expression spread = ((SpreadExpression) exp).getExpression();
					SASTNode node = new SASTNode(expression, spread, TypeUtil.getExpressionType(spread));
					astList.add(node);

				} else {
					SASTNode node = new SASTNode(expression, exp, TypeUtil.getExpressionType(exp));
					astList.add(node);
				}
			}
		}

		list = expression.getValues();
		if (list != null) {
			for (Expression exp : list) {
				if (exp instanceof SpreadExpression) {
					Expression spread = ((SpreadExpression) exp).getExpression();
					SASTNode node = new SASTNode(expression, spread, TypeUtil.getExpressionType(spread));
					astList.add(node);

				} else {
					SASTNode node = new SASTNode(expression, exp, TypeUtil.getExpressionType(exp));
					astList.add(node);
				}
			}
		}

		super.visitGStringExpression(expression);
	}

	@Override
	public void visitArrayExpression(ArrayExpression expression) {
		// TODO Auto-generated method stub

		super.visitArrayExpression(expression);
	}

	@Override
	public void visitSpreadExpression(SpreadExpression expression) {
		// TODO Auto-generated method stub
		if (expression.getNodeMetaData("lbl") == null)
			expression.setNodeMetaData("lbl", expression.getText());
		SASTNode node = new SASTNode(expression, expression.getExpression(),
				TypeUtil.getExpressionType(expression.getExpression()));
		astList.add(node);
		super.visitSpreadExpression(expression);
	}

	@Override
	public void visitSpreadMapExpression(SpreadMapExpression expression) {
		// TODO Auto-generated method stub
		if (expression.getNodeMetaData("lbl") == null)
			expression.setNodeMetaData("lbl", expression.getText());
		SASTNode node = new SASTNode(expression, expression.getExpression(),
				TypeUtil.getExpressionType(expression.getExpression()));
		astList.add(node);

		super.visitSpreadMapExpression(expression);
	}

	@Override
	public void visitNotExpression(NotExpression expression) {
		// TODO Auto-generated method stub
		expression.setNodeMetaData("lbl", expression.getText());
		SASTNode node = new SASTNode(expression, expression.getExpression(),
				TypeUtil.getExpressionType(expression.getExpression()));
		astList.add(node);

		super.visitNotExpression(expression);
	}

	@Override
	public void visitUnaryMinusExpression(UnaryMinusExpression expression) {
		// TODO Auto-generated method stub
		if (expression.getNodeMetaData("lbl") == null)
			expression.setNodeMetaData("lbl", expression.getText());
		SASTNode node = new SASTNode(expression, expression.getExpression(),
				TypeUtil.getExpressionType(expression.getExpression()));
		astList.add(node);
		super.visitUnaryMinusExpression(expression);
	}

	@Override
	public void visitUnaryPlusExpression(UnaryPlusExpression expression) {
		// TODO Auto-generated method stub
		if (expression.getNodeMetaData("lbl") == null)
			expression.setNodeMetaData("lbl", expression.getText());
		SASTNode node = new SASTNode(expression, expression.getExpression(),
				TypeUtil.getExpressionType(expression.getExpression()));
		astList.add(node);
		super.visitUnaryPlusExpression(expression);
	}

	@Override
	public void visitBitwiseNegationExpression(BitwiseNegationExpression expression) {
		// TODO Auto-generated method stub
		if (expression.getNodeMetaData("lbl") == null)
			expression.setNodeMetaData("lbl", expression.getText());
		SASTNode node = new SASTNode(expression, expression.getExpression(),
				TypeUtil.getExpressionType(expression.getExpression()));
		astList.add(node);
		super.visitBitwiseNegationExpression(expression);
	}

	@Override
	public void visitCastExpression(CastExpression expression) {
		// TODO Auto-generated method stub
		if (expression.getNodeMetaData("lbl") == null)
			expression.setNodeMetaData("lbl", expression.getText());

		SASTNode node = new SASTNode(expression, expression.getExpression(),
				TypeUtil.getExpressionType(expression.getExpression()));
		astList.add(node);
		super.visitCastExpression(expression);
	}

	@Override
	public void visitClosureListExpression(ClosureListExpression closureListExpression) {
		// TODO Auto-generated method stub
		if (closureListExpression.getNodeMetaData("lbl") == null)
			closureListExpression.setNodeMetaData("lbl", closureListExpression.getText());
		System.out.println("ClosureListExpression");
		super.visitClosureListExpression(closureListExpression);

	}

	@Override
	public void visitBytecodeExpression(BytecodeExpression expression) {
		// TODO Auto-generated method stub
		if (expression.getNodeMetaData("lbl") == null)
			expression.setNodeMetaData("lbl", expression.getText());

		super.visitBytecodeExpression(expression);

	}

	public List<SASTNode> getNodes() {
		return astList;
	}

	public List<String> getCompileTestCompileDependency(MethodCallExpression call) {
		List<String> dependency = new ArrayList<String>();

		if (call.getMethodAsString().equals("compile") || call.getMethodAsString().equals("testCompile")) {
			call.putNodeMetaData("visited", "true");
			Expression argumentsinfo = call.getArguments();
			if (argumentsinfo != null && argumentsinfo instanceof ArgumentListExpression) {
				ArgumentListExpression ale = (ArgumentListExpression) argumentsinfo;
				if (ale != null) {
					List<Expression> expressions = ale.getExpressions();

					for (Expression expr : expressions) {
						if (expr != null && expr instanceof MethodCallExpression) {
							MethodCallExpression methodexp = (MethodCallExpression) expr;

							if (methodexp.getMethodAsString().equals("project")) {
								Expression argumentsinfo2 = methodexp.getArguments();
								if (argumentsinfo2 != null && argumentsinfo2 instanceof ArgumentListExpression) {
									ArgumentListExpression ale2 = (ArgumentListExpression) argumentsinfo2;
									if (ale != null) {
										List<Expression> expressions2 = ale2.getExpressions();

										for (Expression expr2 : expressions2) {
											if (expr2 != null && expr2 instanceof ConstantExpression) {
												dependency.add(expr2.getText());
											}
										}

									}
								}

							}
						} else if (expr != null && expr instanceof PropertyExpression) {
							PropertyExpression propexp = (PropertyExpression) expr;

							if (propexp.getText().contains("this.project") && propexp.getText().contains(":")) {

								String str = propexp.getText();
								int firstindex = str.indexOf(':');
								int lastindex = str.indexOf(')', firstindex);

								dependency.add(str.substring(firstindex, lastindex));

							}
						}
					}

				}
			}
		}
		// This part is for compile or testCompile Project End

		return dependency;
	}

	public List<String> getRuntimeTestRuntimeDependency(MethodCallExpression call) {
		List<String> dependency = new ArrayList<String>();

		if (call.getMethodAsString().equals("runtime") || call.getMethodAsString().equals("testRuntime")) {
			call.putNodeMetaData("visited", "true");
			Expression argumentsinfo = call.getArguments();
			if (argumentsinfo != null && argumentsinfo instanceof ArgumentListExpression) {
				ArgumentListExpression ale = (ArgumentListExpression) argumentsinfo;
				if (ale != null) {
					List<Expression> expressions = ale.getExpressions();

					for (Expression expr : expressions) {
						if (expr != null && expr instanceof MethodCallExpression) {
							MethodCallExpression methodexp = (MethodCallExpression) expr;

							if (methodexp.getMethodAsString().equals("project")) {
								Expression argumentsinfo2 = methodexp.getArguments();
								if (argumentsinfo2 != null && argumentsinfo2 instanceof ArgumentListExpression) {
									ArgumentListExpression ale2 = (ArgumentListExpression) argumentsinfo2;
									if (ale != null) {
										List<Expression> expressions2 = ale2.getExpressions();

										for (Expression expr2 : expressions2) {
											if (expr2 != null && expr2 instanceof ConstantExpression) {
												dependency.add(expr2.getText());
											}
										}

									}
								}

							}
						} else if (expr != null && expr instanceof PropertyExpression) {
							PropertyExpression propexp = (PropertyExpression) expr;

							if (propexp.getText().contains("this.project") && propexp.getText().contains(":")) {

								String str = propexp.getText();
								int firstindex = str.indexOf(':');
								int lastindex = str.indexOf(')', firstindex);

								dependency.add(str.substring(firstindex, lastindex));

							}
						}
					}

				}
			}
		}
		// This part is for compile or testCompile Project End

		return dependency;
	}

	public List<String> getProvidedDependency(MethodCallExpression call) {
		List<String> dependency = new ArrayList<String>();

		if (call.getMethodAsString().equals("provided")) {
			call.putNodeMetaData("visited", "true");
			Expression argumentsinfo = call.getArguments();
			if (argumentsinfo != null && argumentsinfo instanceof ArgumentListExpression) {
				ArgumentListExpression ale = (ArgumentListExpression) argumentsinfo;
				if (ale != null) {
					List<Expression> expressions = ale.getExpressions();

					for (Expression expr : expressions) {
						if (expr != null && expr instanceof MethodCallExpression) {
							MethodCallExpression methodexp = (MethodCallExpression) expr;

							if (methodexp.getMethodAsString().equals("project")) {
								Expression argumentsinfo2 = methodexp.getArguments();
								if (argumentsinfo2 != null && argumentsinfo2 instanceof ArgumentListExpression) {
									ArgumentListExpression ale2 = (ArgumentListExpression) argumentsinfo2;
									if (ale != null) {
										List<Expression> expressions2 = ale2.getExpressions();

										for (Expression expr2 : expressions2) {
											if (expr2 != null && expr2 instanceof ConstantExpression) {
												dependency.add(expr2.getText());
											}
										}

									}
								}

								else if (argumentsinfo2 != null && argumentsinfo2 instanceof TupleExpression) {
									List<? extends Expression> list = ((TupleExpression) argumentsinfo2)
											.getExpressions();
									if (list != null) {
										for (Expression expression1 : list) {

											for (MapEntryExpression exp : ((MapExpression) expression1)
													.getMapEntryExpressions()) {

												if (exp.getValueExpression().getText().startsWith(":"))
													dependency.add(exp.getValueExpression().getText());
											}

										}
									}

								}

							}
						} else if (expr != null && expr instanceof PropertyExpression) {
							PropertyExpression propexp = (PropertyExpression) expr;

							if (propexp.getText().contains("this.project") && propexp.getText().contains(":")) {

								String str = propexp.getText();
								int firstindex = str.indexOf(':');
								int lastindex = str.indexOf(')', firstindex);

								dependency.add(str.substring(firstindex, lastindex));

							}
						}
					}

				}
			}
		}
		// This part is for compile or testCompile Project End

		return dependency;
	}

	public Map<String, List<String>> getProjectDependencyForCompile(MethodCallExpression call) {
		Map<String, List<String>> mapdependency = new HashMap<String, List<String>>();
		String parentproj = null;
		if (call.getMethodAsString().equals("project")) {
			Expression argumentsinfo = call.getArguments();

			if (argumentsinfo != null && argumentsinfo instanceof ArgumentListExpression) {
				ArgumentListExpression ale = (ArgumentListExpression) argumentsinfo;
				if (ale != null) {
					List<Expression> expressions = ale.getExpressions();

					for (Expression expr : expressions) {
						if (expr != null && expr instanceof ConstantExpression) {
							parentproj = expr.getText();
						}

						else if (expr != null && expr instanceof ClosureExpression) {
							ClosureExpression closureExpression = (ClosureExpression) expr;
							Statement block = closureExpression.getCode();
							if (block instanceof BlockStatement) {

								BlockStatement blk = (BlockStatement) block;

								List<Statement> stmts = blk.getStatements();

								for (Statement stmt : stmts) {

									if (stmt instanceof ExpressionStatement) {
										ExpressionStatement expstmt = (ExpressionStatement) stmt;
										Expression node = expstmt.getExpression();

										if (node != null && node instanceof MethodCallExpression) {
											MethodCallExpression methodexp = (MethodCallExpression) node;

											if (methodexp.getMethodAsString().equals("dependencies")) {
												Expression argumentsinfo2 = methodexp.getArguments();

												if (argumentsinfo2 != null
														&& argumentsinfo2 instanceof ArgumentListExpression) {
													ArgumentListExpression ale2 = (ArgumentListExpression) argumentsinfo2;

													if (ale2 != null) {
														List<Expression> expressions2 = ale2.getExpressions();

														for (Expression expr2 : expressions2) {

															if (expr2 != null && expr2 instanceof ClosureExpression) {

																ClosureExpression closureExpression2 = (ClosureExpression) expr2;
																Statement block2 = closureExpression2.getCode();
																if (block2 instanceof BlockStatement) {

																	BlockStatement blk2 = (BlockStatement) block2;

																	List<Statement> stmts2 = blk2.getStatements();

																	for (Statement stmt2 : stmts2) {

																		if (stmt2 instanceof ExpressionStatement) {
																			ExpressionStatement expstmt2 = (ExpressionStatement) stmt2;
																			Expression node2 = expstmt2.getExpression();

																			if (node2 != null
																					&& node2 instanceof MethodCallExpression) {

																				MethodCallExpression methodcall = (MethodCallExpression) node2;

																				List<String> dependency = getCompileTestCompileDependency(
																						methodcall);

																				if (dependency != null
																						&& dependency.size() > 0) {
																					mapdependency.put(parentproj,
																							dependency);

																					mergeDependencyMap(mapdependency);
																				}

																				dependency = getProvidedDependency(
																						methodcall);

																				if (dependency != null
																						&& dependency.size() > 0) {
																					mapdependency.put(parentproj,
																							dependency);

																					mergeDependencyMap(mapdependency);
																				}

																				dependency = getRuntimeTestRuntimeDependency(
																						methodcall);

																				if (dependency != null
																						&& dependency.size() > 0) {
																					mapdependency.put(parentproj,
																							dependency);

																					mergeDependencyMap(mapdependency);
																				}

																			}

																		}

																	}

																}
															}
														}

													}
												}
											}

										}
									}
								}

							}
						}
					}

				}
			}

		}

		return mapdependency;
	}

	public void mergeDependencyMap(Map<String, List<String>> depdencymap) {

		for (String key : depdencymap.keySet()) {
			if (projectDependencyies.containsKey(key)) {
				List<String> deps = projectDependencyies.get(key);

				deps.addAll(depdencymap.get(key));

				List<String> deDupStringList = new ArrayList<>(new HashSet<>(deps));

				projectDependencyies.put(key, deDupStringList);
			} else {
				projectDependencyies.put(key, depdencymap.get(key));
			}

		}

	}

}