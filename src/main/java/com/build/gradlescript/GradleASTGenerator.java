package com.build.gradlescript;

import java.util.List;

import com.github.gumtreediff.tree.TreeContext;

import edu.utsa.gradlediff.GradleASTParseMngr;
import edu.utsa.gradlediff.SASTNode;

public class GradleASTGenerator {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		GradleASTParseMngr astParser=new GradleASTParseMngr();
		
		String filestr="C:\\Users\\foyzul\\Desktop\\ASE2019\\Sample.gradle";
		
		TreeContext tree=astParser.getGradleScriptTree(filestr);
		
		System.out.println(tree.toString());
		
//		List<SASTNode> nodes= astParser.getGradleASTNodes(filestr);
//		
//		for(SASTNode node:nodes)
//		{
//			if(node.getCurrent().getNodeMetaData("lbl")!=null)
//				System.out.println(node.getCurrent().getNodeMetaData("lbl").toString());
//		}
	}

}
