package edu.utsa.gradlediff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.builder.AstBuilder;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.control.CompilePhase;

import com.build.analyzer.config.Config;
import com.github.gumtreediff.actions.ActionGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Addition;
import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.actions.model.Update;
import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.matchers.CompositeMatchers;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;
import com.github.gumtreediff.tree.TreeUtils;

import edu.utsa.buildlogparser.util.TextFileReaderWriter;
import edu.utsa.gradlediff.GradleNodeVisitor;
import edu.utsa.gradlediff.GradleTreeVisitor;
import edu.utsa.gradlediff.SASTNode;
import edu.utsa.gradlediff.GradleChange;

public class GradleASTParseMngr {

	List<GradleChange> gradlechanges;
	public List<Action> changeActions = new ArrayList<Action>();

	public List<GradleChange> getGradlechanges() {
		return gradlechanges;
	}

	List<Integer> linelist;

	public boolean hasFixes() {
		if (gradlechanges.size() > 0)
			return true;
		else
			return false;
	}

	public GradleASTParseMngr() {
		gradlechanges = new ArrayList<GradleChange>();
		linelist = new ArrayList<Integer>();
	}

	public void generatePatch(String srcfilepath, String destfilepath) {
		Run.initGenerators();
		StringMenupulator strmenu = new StringMenupulator();

		String patchstr = "";
		TreeContext tsrc;
		TreeContext tdst;

		String srcstr = "";
		List<String> strlist = TextFileReaderWriter.GetFileContentByLine(srcfilepath);

		for (int index = 0; index < strlist.size(); index++) {
			String str = strlist.get(index);

			str = strmenu.getMarkedString(str);

			strlist.set(index, str);

		}

		List<String> strlist1 = TextFileReaderWriter.GetFileContentByLine(destfilepath);

		for (int index = 0; index < strlist1.size(); index++) {

			String str = strlist1.get(index);

			str = strmenu.getMarkedString(str);

			strlist1.set(index, str);

		}

		tsrc = getTreeContext(strlist);
		tdst = getTreeContext(strlist1);

		TreeUtils.computeDepth(tsrc.getRoot());
		TreeUtils.computeHeight(tsrc.getRoot());
		TreeUtils.computeSize(tsrc.getRoot());

		TreeUtils.computeDepth(tdst.getRoot());
		TreeUtils.computeHeight(tdst.getRoot());
		TreeUtils.computeSize(tdst.getRoot());

		Matcher match = new CompositeMatchers.ClassicGumtree(tsrc.getRoot(), tdst.getRoot(), new MappingStore());

		match.match();

		ActionGenerator g = new ActionGenerator(tsrc.getRoot(), tdst.getRoot(), match.getMappings());

		g.generate();

		List<Action> actions = g.getActions();

		changeActions.addAll(actions);

		for (int index = 0; index < actions.size(); index++) {
			Action ac = actions.get(index);

			if (Config.debugPrint == true) {
				String test = ac.getNode().toPrettyString(tsrc);
				System.out.println(test);
			}

			if (ac.getNode().isLeaf()) {

				String strblock = " ";
				String strtask = " ";
				String strexpstmt = " ";
				String strparent = "#";

				GradleChange change = new GradleChange();

				ITree block = getBlock(ac.getNode());
				ITree task = getTask(ac.getNode());
				ITree expstmt = getExpStmt(ac.getNode());

				if (block != null) {
					strblock = (String) block.getMetadata("block");
				}
				if (task != null) {
					strtask = (String) task.getMetadata("task");
				}
				if (expstmt != null) {
					strexpstmt = (String) expstmt.getMetadata("ST");
				}
				if (ac.getNode().getParent() != null) {
					strparent = ac.getNode().getParent().getLabel();
				}

				if (ac instanceof Update) {
					Update action = (Update) ac;

					// System.out.println(ac.getClass().getSimpleName() + "-->"
					// + ac.getNode().toPrettyString(tsrc) + "-->"
					// + action.getValue() + "-->" + action.getNode().getPos() +
					// "Parent Type:"
					// + ac.getNode().getParent().toPrettyString(tsrc) +
					// "=>Parent:" + strparent + "=>Block:"
					// + strblock + "=>task:" + strtask + "=>Stament:" +
					// strexpstmt);

					String prettyString = ac.getNode().toPrettyString(tsrc);

					int expindex = prettyString.indexOf(":");

					String part1 = " ";
					String part2 = " ";

					if (expindex >= 0) {
						part1 = prettyString.substring(0, expindex);
						part2 = prettyString.substring(expindex + 1);
					} else {
						part1 = prettyString;
					}

					String nodetype = " ";// parts[0]; // 004
					String nodeexp = " ";// parts[1]; // 034556

					if (part1 != null)
						nodetype = part1;
					else
						nodetype = " ";

					if (part2 != null)
						nodeexp = part2;
					else
						nodeexp = " ";

					if (!linelist.contains(action.getNode().getPos())) {
						if (action.getNode().getPos() >= 0)
							linelist.add(action.getNode().getPos());
					}

					change.setOperationName(ac.getClass().getSimpleName());
					change.setNodeType(nodetype);
					change.setNodeExp(nodeexp);
					change.setLineNumber(action.getNode().getPos());
					change.setParentType(ac.getNode().getParent().toPrettyString(tsrc));
					change.setParentExp(strparent);
					change.setBlockName(strblock);
					change.setTaskName(strtask);
					change.setStatementExp(strexpstmt);

					gradlechanges.add(change);
					// linelist.add(action.getNode().getPos());
				}

				else if (ac instanceof Insert) {

					Insert action = (Insert) ac;
					// System.out.println(ac.getClass().getSimpleName() + "-->"
					// + ac.getNode().toPrettyString(tsrc) + "-->"
					// + action.getNode().getPos() + "Parent Type:" +
					// ac.getNode().getParent().toPrettyString(tsrc)
					// + "=>Parent:" + strparent + "=>Block:" + strblock +
					// "=>task:" + strtask + "=>Stament:"
					// + strexpstmt);

					String prettyString = ac.getNode().toPrettyString(tsrc);

					int expindex = prettyString.indexOf(":");

					String part1 = " ";
					String part2 = " ";

					if (expindex >= 0) {
						part1 = prettyString.substring(0, expindex);
						part2 = prettyString.substring(expindex + 1);
					} else {
						part1 = prettyString;
					}

					String nodetype = " ";// parts[0]; // 004
					String nodeexp = " ";// parts[1]; // 034556

					if (part1 != null)
						nodetype = part1;
					else
						nodetype = " ";

					if (part2 != null)
						nodeexp = part2;
					else
						nodeexp = " ";

					if (!linelist.contains(action.getNode().getPos())) {
						if (action.getNode().getPos() >= 0)
							linelist.add(action.getNode().getPos());
					}

					change.setOperationName(ac.getClass().getSimpleName());
					change.setNodeType(nodetype);
					change.setNodeExp(nodeexp);
					change.setLineNumber(action.getNode().getPos());
					change.setParentType(ac.getNode().getParent().toPrettyString(tsrc));
					change.setParentExp(strparent);
					change.setBlockName(strblock);
					change.setTaskName(strtask);
					change.setStatementExp(strexpstmt);
					gradlechanges.add(change);
				}

				else if (ac instanceof Move) {

					Move action = (Move) ac;
					// System.out.println(ac.getClass().getSimpleName() + "-->"
					// + ac.getNode().toPrettyString(tsrc) + "-->"
					// + action.getNode().getPos() + "Parent Type:" +
					// ac.getNode().getParent().toPrettyString(tsrc)
					// + "=>Parent:" + strparent + "=>Block:" + strblock +
					// "=>task:" + strtask + "=>Stament:"
					// + strexpstmt);

					String prettyString = ac.getNode().toPrettyString(tsrc);

					int expindex = prettyString.indexOf(":");

					String part1 = " ";
					String part2 = " ";

					if (expindex >= 0) {
						part1 = prettyString.substring(0, expindex);
						part2 = prettyString.substring(expindex + 1);
					} else {
						part1 = prettyString;
					}

					String nodetype = " ";// parts[0]; // 004
					String nodeexp = " ";// parts[1]; // 034556

					if (part1 != null)
						nodetype = part1;
					else
						nodetype = " ";

					if (part2 != null)
						nodeexp = part2;
					else
						nodeexp = " ";

					if (!linelist.contains(action.getNode().getPos())) {
						if (action.getNode().getPos() >= 0)
							linelist.add(action.getNode().getPos());
					}

					change.setOperationName(ac.getClass().getSimpleName());
					change.setNodeType(nodetype);
					change.setNodeExp(nodeexp);
					change.setLineNumber(action.getNode().getPos());
					change.setParentType(ac.getNode().getParent().toPrettyString(tsrc));
					change.setParentExp(strparent);
					change.setBlockName(strblock);
					change.setTaskName(strtask);
					change.setStatementExp(strexpstmt);
					gradlechanges.add(change);
				} else if (ac instanceof Delete) {

					Delete action = (Delete) ac;
					// System.out.println(ac.getClass().getSimpleName() + "-->"
					// + ac.getNode().toPrettyString(tsrc) + "-->"
					// + action.getNode().getPos()+"Parent
					// Type:"+ac.getNode().getParent().toPrettyString(tsrc)+"=>Parent:"+strparent+"=>Block:"+strblock+"=>task:"+strtask+"=>Stament:"+strexpstmt);

					String prettyString = ac.getNode().toPrettyString(tsrc);

					int expindex = prettyString.indexOf(":");

					String part1 = " ";
					String part2 = " ";

					if (expindex >= 0) {
						part1 = prettyString.substring(0, expindex);
						part2 = prettyString.substring(expindex + 1);
					} else {
						part1 = prettyString;
					}

					String nodetype = " ";// parts[0]; // 004
					String nodeexp = " ";// parts[1]; // 034556

					if (part1 != null)
						nodetype = part1;
					else
						nodetype = " ";

					if (part2 != null)
						nodeexp = part2;
					else
						nodeexp = " ";
					if (!linelist.contains(action.getNode().getPos())) {
						if (action.getNode().getPos() >= 0)
							linelist.add(action.getNode().getPos());
					}

					change.setOperationName(ac.getClass().getSimpleName());
					change.setNodeType(nodetype);
					change.setNodeExp(nodeexp);
					change.setLineNumber(action.getNode().getPos());
					change.setParentType(ac.getNode().getParent().toPrettyString(tsrc));
					change.setParentExp(strparent);
					change.setBlockName(strblock);
					change.setTaskName(strtask);
					change.setStatementExp(strexpstmt);
					gradlechanges.add(change);
				}

				else if (ac instanceof Addition) {

					Addition action = (Addition) ac;
					// System.out.println(ac.getClass().getSimpleName() + "-->"
					// + ac.getNode().toPrettyString(tsrc) + "-->"
					// + action.getNode().getPos()+"Parent
					// Type:"+ac.getNode().getParent().toPrettyString(tsrc)+"=>Parent:"+strparent+"=>Block:"+strblock+"=>task:"+strtask+"=>Stament:"+strexpstmt);

					String prettyString = ac.getNode().toPrettyString(tsrc);

					int expindex = prettyString.indexOf(":");

					String part1 = " ";
					String part2 = " ";

					if (expindex >= 0) {
						part1 = prettyString.substring(0, expindex);
						part2 = prettyString.substring(expindex + 1);
					} else {
						part1 = prettyString;
					}

					String nodetype = " ";// parts[0]; // 004
					String nodeexp = " ";// parts[1]; // 034556

					if (part1 != null)
						nodetype = part1;
					else
						nodetype = " ";

					if (part2 != null)
						nodeexp = part2;
					else
						nodeexp = " ";

					if (!linelist.contains(action.getNode().getPos())) {
						if (action.getNode().getPos() >= 0)
							linelist.add(action.getNode().getPos());
					}

					change.setOperationName(ac.getClass().getSimpleName());
					change.setNodeType(nodetype);
					change.setNodeExp(nodeexp);
					change.setLineNumber(action.getNode().getPos());
					change.setParentType(ac.getNode().getParent().toPrettyString(tsrc));
					change.setParentExp(strparent);
					change.setBlockName(strblock);
					change.setTaskName(strtask);
					change.setStatementExp(strexpstmt);
					gradlechanges.add(change);
				} else {
					String prettyString = ac.getNode().toPrettyString(tsrc);

					int expindex = prettyString.indexOf(":");

					String part1 = " ";
					String part2 = " ";

					if (expindex >= 0) {
						part1 = prettyString.substring(0, expindex);
						part2 = prettyString.substring(expindex + 1);
					} else {
						part1 = prettyString;
					}

					String nodetype = " ";// parts[0]; // 004
					String nodeexp = " ";// parts[1]; // 034556

					if (part1 != null)
						nodetype = part1;
					else
						nodetype = " ";

					if (part2 != null)
						nodeexp = part2;
					else
						nodeexp = " ";

					if (!linelist.contains(ac.getNode().getPos())) {
						if (ac.getNode().getPos() >= 0)
							linelist.add(ac.getNode().getPos());
					}

					change.setOperationName(ac.getClass().getSimpleName());
					change.setNodeType(nodetype);
					change.setNodeExp(nodeexp);
					change.setLineNumber(ac.getNode().getPos());
					change.setParentType(ac.getNode().getParent().toPrettyString(tsrc));
					change.setParentExp(strparent);
					change.setBlockName(strblock);
					change.setTaskName(strtask);
					change.setStatementExp(strexpstmt);
					gradlechanges.add(change);
				}
			}

		}

		// GradlePatchGenMngr.Collections.sort(linelist);
		// HashMap<Integer, ExpPerLines> linechange = getExpPerLine(linelist,
		// gradlechanges);

		/* This part is for initial change string */
		// // Iterating over keys only
		// for (int lineno = 0; lineno < linelist.size(); lineno++) {
		//
		// int key = linelist.get(lineno);
		// ExpPerLines obj = linechange.get(key);
		//
		// List<GradleChange> gdchanges = obj.getChangesPerLine();
		//
		// for (int chindex = 0; chindex < gdchanges.size(); chindex++) {
		// GradleChange change = gdchanges.get(chindex);
		// patchstr = patchstr + change.getOperationName() + "-->" +
		// change.getNodeType() + "-->"
		// + change.getNodeExp() + "==>" + change.getLineNumber() + "\n";
		// // System.out.println(change.getOperationName() + "-->" +
		// // change.getNodeType() + "-->" +
		// // change.getNodeExp()+"==>"+change.getLineNumber());
		// }
		//
		// }
		/* This part is for initial change string */

		// GradlePatchFormater patchformater=new GradlePatchFormater();
		// String xmlstr=patchformater.getXMLPatch(linelist,linechange);

		// return xmlstr;

	}

	public String getXMLChange() {
		Collections.sort(linelist);
		String xmlstr = null;
		if (linelist.size() > 0) {
			HashMap<Integer, ExpPerLines> linechange = getExpPerLine(linelist, gradlechanges);

			GradlePatchFormater patchformater = new GradlePatchFormater();
			xmlstr = patchformater.getXMLPatch(linelist, linechange);
		}
		return xmlstr;
	}

	public HashMap<Integer, ExpPerLines> getExpPerLine(List<Integer> lines, List<GradleChange> gradleChanges) {
		HashMap<Integer, ExpPerLines> expmap = new HashMap<Integer, ExpPerLines>();

		for (int index = 0; index < lines.size(); index++) {
			int lineno = lines.get(index);

			for (int iindex = 0; iindex < gradleChanges.size(); iindex++) {
				GradleChange gradlechange = gradleChanges.get(iindex);

				if (lineno == gradlechange.getLineNumber() && expmap.get(lineno) != null) {
					ExpPerLines expobj = expmap.get(lineno);
					expobj.addChangetoLine(gradlechange);

				}
				if (lineno == gradlechange.getLineNumber() && expmap.get(lineno) == null) {
					ExpPerLines expobj = new ExpPerLines(lineno);
					expobj.addChangetoLine(gradlechange);
					expmap.put(lineno, expobj);
				}
			}
		}

		return expmap;
	}

	static ITree getBlock(ITree currentnode) {
		ITree block = null;

		ITree node = currentnode.getParent();

		while (node != null) {
			if (node.getMetadata("block") != null) {
				block = node;
				break;
			}

			node = node.getParent();
		}

		return block;
	}

	static ITree getTask(ITree currentnode) {
		ITree task = null;

		ITree node = currentnode.getParent();

		while (node != null) {
			if (node.getMetadata("task") != null) {
				task = node;
				break;
			}

			node = node.getParent();
		}

		return task;
	}

	static ITree getExpStmt(ITree currentnode) {
		ITree stmt = null;

		ITree node = currentnode.getParent();

		while (node != null) {
			if (node.getMetadata("ST") != null) {
				stmt = node;
				break;
			}

			node = node.getParent();
		}

		return stmt;
	}

	public static TreeContext getTreeContext(List<String> strlist) {
		String srcstr = "";

		for (int size = 0; size < strlist.size(); size++) {
			srcstr = srcstr + strlist.get(size);
			srcstr = srcstr + "\r\n";

		}

		List<ASTNode> nodes = null;

		try {
			// nodes = new AstBuilder().buildFromString(srcstr);
			// nodes = new
			// AstBuilder().buildFromString(CompilePhase.CANONICALIZATION,
			// srcstr);
			nodes = new AstBuilder().buildFromString(CompilePhase.CONVERSION, srcstr);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		if (nodes == null) {
			ASTNode node = new EmptyStatement();
			nodes = new ArrayList<ASTNode>();
			nodes.add(node);
		}

		ASTNode[] astnodes = new ASTNode[nodes.size()];

		for (int index = 0; index < nodes.size(); index++) {
			if (nodes.get(index) instanceof InnerClassNode) {
				astnodes[index] = new EmptyStatement();
			} else {
				astnodes[index] = nodes.get(index);
			}

		}

		GradleNodeVisitor visitordep = new GradleNodeVisitor(astnodes[0]);

		for (ASTNode node : astnodes) {
			node.visit(visitordep);
		}

		List<SASTNode> astList = visitordep.getNodes();

		//// This part for gumtree tree///////
		GradleTreeVisitor gradletree = new GradleTreeVisitor(astList.get(0));

		for (int i = 0; i < astList.size(); i++) {

			SASTNode item = astList.get(i);
			gradletree.visit(item);

		}

		return gradletree.getTree(astList.get(0));

	}

	public static List<String> getSubProjList(List<String> strlist) {
		String srcstr = "";

		for (int size = 0; size < strlist.size(); size++) {
			srcstr = srcstr + strlist.get(size);
			srcstr = srcstr + "\r\n";

		}

		List<ASTNode> nodes = null;

		try {
			// nodes = new AstBuilder().buildFromString(srcstr);
			// nodes = new
			// AstBuilder().buildFromString(CompilePhase.CANONICALIZATION,
			// srcstr);
			nodes = new AstBuilder().buildFromString(CompilePhase.CONVERSION, srcstr);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		if (nodes == null) {
			ASTNode node = new EmptyStatement();
			nodes = new ArrayList<ASTNode>();
			nodes.add(node);
		}

		ASTNode[] astnodes = new ASTNode[nodes.size()];

		for (int index = 0; index < nodes.size(); index++) {
			if (nodes.get(index) instanceof InnerClassNode) {
				astnodes[index] = new EmptyStatement();
			} else if (nodes.get(index) instanceof ClassNode) {
				astnodes[index] = new EmptyStatement();
			} else {
				astnodes[index] = nodes.get(index);
			}

		}

		GradleSubProjDependencyVisitor visitordep = new GradleSubProjDependencyVisitor(astnodes[0], ":root");

		for (ASTNode node : astnodes) {
			node.visit(visitordep);
		}

		List<String> subprojs = visitordep.getSubprojects();

		// for (String subproj : subprojs) {
		// System.out.println(subproj);
		// }
		//
		// Map<String,List<String>>
		// projdeps=visitordep.getProjectDependencyies();
		//
		// System.out.println("Project Dependencies");
		//
		// for(String key:projdeps.keySet())
		// {
		// System.out.println("Dependency for Subproject: "+key);
		// List<String> deps=projdeps.get(key);
		//
		// System.out.println(deps);
		// }
		//
		//
		//
		// List<SASTNode> astList = visitordep.getNodes();
		//
		// //// This part for gumtree tree///////
		// GradleTreeVisitor gradletree = new GradleTreeVisitor(astList.get(0));
		//
		// for (int i = 0; i < astList.size(); i++) {
		//
		// SASTNode item = astList.get(i);
		// gradletree.visit(item);
		//
		// }
		//
		// return gradletree.getTree(astList.get(0));

		return subprojs;

	}

	public static String getRootProjectName(List<String> strlist) {
		String srcstr = "";

		for (int size = 0; size < strlist.size(); size++) {
			srcstr = srcstr + strlist.get(size);
			srcstr = srcstr + "\r\n";

		}

		List<ASTNode> nodes = null;

		try {
			// nodes = new AstBuilder().buildFromString(srcstr);
			// nodes = new
			// AstBuilder().buildFromString(CompilePhase.CANONICALIZATION,
			// srcstr);
			nodes = new AstBuilder().buildFromString(CompilePhase.CONVERSION, srcstr);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		if (nodes == null) {
			ASTNode node = new EmptyStatement();
			nodes = new ArrayList<ASTNode>();
			nodes.add(node);
		}

		ASTNode[] astnodes = new ASTNode[nodes.size()];

		for (int index = 0; index < nodes.size(); index++) {
			if (nodes.get(index) instanceof InnerClassNode) {
				astnodes[index] = new EmptyStatement();
			} else if (nodes.get(index) instanceof ClassNode) {
				astnodes[index] = new EmptyStatement();
			} else {
				astnodes[index] = nodes.get(index);
			}

		}

		GradleSubProjDependencyVisitor visitordep = new GradleSubProjDependencyVisitor(astnodes[0], ":root");

		for (ASTNode node : astnodes) {
			node.visit(visitordep);
		}

		String rootproject = visitordep.getRootProjectName();

		return rootproject;

	}

	public static Map<String, List<String>> getSubProjConnectivity(List<String> strlist, String rootfolder) {
		String srcstr = "";

		for (int size = 0; size < strlist.size(); size++) {
			srcstr = srcstr + strlist.get(size);
			srcstr = srcstr + "\r\n";

		}

		List<ASTNode> nodes = null;

		try {
			// nodes = new AstBuilder().buildFromString(srcstr);
			// nodes = new
			// AstBuilder().buildFromString(CompilePhase.CANONICALIZATION,
			// srcstr);
			nodes = new AstBuilder().buildFromString(CompilePhase.CONVERSION, srcstr);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		if (nodes == null) {
			ASTNode node = new EmptyStatement();
			nodes = new ArrayList<ASTNode>();
			nodes.add(node);
		}

		ASTNode[] astnodes = new ASTNode[nodes.size()];

		for (int index = 0; index < nodes.size(); index++) {
			if (nodes.get(index) instanceof InnerClassNode) {
				astnodes[index] = new EmptyStatement();
			} else if (nodes.get(index) instanceof ClassNode) {
				astnodes[index] = new EmptyStatement();
			} else {
				astnodes[index] = nodes.get(index);
			}

		}

		GradleSubProjDependencyVisitor visitordep = new GradleSubProjDependencyVisitor(astnodes[0], rootfolder);

		for (ASTNode node : astnodes) {
			node.visit(visitordep);
		}

		Map<String, List<String>> projdeps = visitordep.getProjectDependencyies();

		return projdeps;

	}

	public TreeContext getGradleScriptTree(String srcfilepath) {
		Run.initGenerators();
		StringMenupulator strmenu = new StringMenupulator();
		TreeContext tsrc;
		List<String> strlist = TextFileReaderWriter.GetFileContentByLine(srcfilepath);

		for (int index = 0; index < strlist.size(); index++) {
			String str = strlist.get(index);
			str = strmenu.getMarkedString(str);
			strlist.set(index, str);

		}

		tsrc = getTreeContext(strlist);
		TreeUtils.computeDepth(tsrc.getRoot());
		TreeUtils.computeHeight(tsrc.getRoot());
		TreeUtils.computeSize(tsrc.getRoot());

		return tsrc;
	}

	public List<SASTNode> getGradleASTNodes(String srcfilepath) {
		Run.initGenerators();
		StringMenupulator strmenu = new StringMenupulator();
		TreeContext tsrc;
		List<String> strlist = TextFileReaderWriter.GetFileContentByLine(srcfilepath);

		for (int index = 0; index < strlist.size(); index++) {
			String str = strlist.get(index);
			str = strmenu.getMarkedString(str);
			strlist.set(index, str);

		}

		List<SASTNode> nodes = getGradleNodes(strlist);

		return nodes;
	}

	private List<SASTNode> getGradleNodes(List<String> strlist) {
		String srcstr = "";

		for (int size = 0; size < strlist.size(); size++) {
			srcstr = srcstr + strlist.get(size);
			srcstr = srcstr + "\r\n";

		}

		List<ASTNode> nodes = null;

		try {
			// nodes = new AstBuilder().buildFromString(srcstr);
			// nodes = new
			// AstBuilder().buildFromString(CompilePhase.CANONICALIZATION,
			// srcstr);
			nodes = new AstBuilder().buildFromString(CompilePhase.CONVERSION, srcstr);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		if (nodes == null) {
			ASTNode node = new EmptyStatement();
			nodes = new ArrayList<ASTNode>();
			nodes.add(node);
		}

		ASTNode[] astnodes = new ASTNode[nodes.size()];

		for (int index = 0; index < nodes.size(); index++) {
			if (nodes.get(index) instanceof InnerClassNode) {
				astnodes[index] = new EmptyStatement();
			} else {
				astnodes[index] = nodes.get(index);
			}

		}

		GradleNodeVisitor visitordep = new GradleNodeVisitor(astnodes[0]);
		try {

			for (ASTNode node : astnodes) {
				node.visit(visitordep);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		List<SASTNode> astList = visitordep.getNodes();

		return astList;
	}

}
