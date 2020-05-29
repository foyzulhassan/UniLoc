package com.build.gradle.ast.selection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.builder.AstBuilder;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.control.CompilePhase;

import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.tree.TreeContext;

import edu.utsa.buildlogparser.util.TextFileReaderWriter;
import edu.utsa.gradlediff.GradleSubProjDependencyVisitor;
import edu.utsa.gradlediff.SASTNode;
import edu.utsa.gradlediff.StringMenupulator;

public class GradleExtractSelectedAST {
	
	private String rootFolder;
	
	public GradleExtractSelectedAST(String root)
	{
		this.rootFolder=root;
	}
	
	public GradleExtractSelectedAST()
	{
		
	}

	public GradleSelectedASTEntities getGradleASTNodes(String srcfilepath,String path) {
		//Run.initGenerators();
		StringMenupulator strmenu = new StringMenupulator();
		String rootproj;
		//TreeContext tsrc;
		List<String> strlist = TextFileReaderWriter.GetFileContentByLine(srcfilepath);

		for (int index = 0; index < strlist.size(); index++) {
			String str = strlist.get(index);
			str = strmenu.getMarkedString(str);
			strlist.set(index, str);

		}
		
		if(rootFolder!=null && rootFolder.length()>0)
		{
			rootproj=rootFolder;
		}
		else
		{
			rootproj="root";
		}

		return getSubProjConnectivity(strlist,rootproj,path);
	}

	private GradleSelectedASTEntities getSubProjConnectivity(List<String> strlist, String rootfolder,String path) {
		String srcstr = "";

		for (int size = 0; size < strlist.size(); size++) {
			srcstr = srcstr + strlist.get(size);
			srcstr = srcstr + "\r\n";
		}

		List<ASTNode> nodes = null;

		try {
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

		GradleASTNodeSelectionVisitor visitordep = new GradleASTNodeSelectionVisitor(astnodes[0], rootfolder, strlist);

		for (ASTNode node : astnodes) {
			node.visit(visitordep);
		}

		visitordep.getProjectDependencyies();

		List<String> dependency = visitordep.getDependencyList();
		List<String> tasklist = visitordep.getTaskList();
		List<String> propertylist = visitordep.getPropertyList();
		List<String> subprojlist = visitordep.getSubprojects();
		Map<String, List<String>> subproj = visitordep.getProjectDependencyies();

		for (String key : subproj.keySet()) {
			subprojlist.addAll(subproj.get(key));
			subprojlist.add(key);
		}
		subprojlist.add(path);

		GradleSelectedASTEntities astentities = new GradleSelectedASTEntities(dependency, tasklist, propertylist,
				subprojlist);

		return astentities;

	}

	public static void main(String[] args) {
		String strfile = "C:\\Users\\foyzul\\Desktop\\ASE2019\\build.gradle";
		GradleExtractSelectedAST ast = new GradleExtractSelectedAST();
		ast.getGradleASTNodes(strfile,strfile);
	}

}
