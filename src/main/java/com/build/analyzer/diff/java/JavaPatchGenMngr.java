package com.build.analyzer.diff.java;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.build.analyzer.diff.gradle.StringMenupulator;
import com.build.util.TextFileReaderWriter;
import com.github.gumtreediff.actions.ActionGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.gen.Generators;
import com.github.gumtreediff.matchers.CompositeMatchers;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;
import com.github.gumtreediff.tree.TreeUtils;


public class JavaPatchGenMngr {
	
	public List<Action> getJavaChanges(String srcfilepath, String destfilepath)
	{
		Run.initGenerators();
		
		MetaJdtTreeGenerator mtgen=new MetaJdtTreeGenerator();
		
		MetaJdtTreeGenerator mtgen1=new MetaJdtTreeGenerator();
	  

		TreeContext srcContext = null;
		TreeContext dstContext = null;
		try {
			srcContext = mtgen.generate(new File(srcfilepath));
			dstContext= mtgen1.generate(new File(destfilepath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		TreeUtils.computeDepth(srcContext.getRoot());
		TreeUtils.computeHeight(srcContext.getRoot());
		TreeUtils.computeSize(srcContext.getRoot());

		TreeUtils.computeDepth(dstContext.getRoot());
		TreeUtils.computeHeight(dstContext.getRoot());
		TreeUtils.computeSize(dstContext.getRoot());
	
		
		ITree src = srcContext.getRoot();
		ITree dst = dstContext.getRoot();	
		
		// ClassicGumtree m=new ClassicGumtree(src,dst, new MappingStore());
		Matcher m = Matchers.getInstance().getMatcher(src, dst); 
		m.match();
		ActionGenerator g = new ActionGenerator(src, dst, m.getMappings());
		g.generate();
		List<Action> actions = g.getActions(); // return the actions
		
		return actions;

	}

}
