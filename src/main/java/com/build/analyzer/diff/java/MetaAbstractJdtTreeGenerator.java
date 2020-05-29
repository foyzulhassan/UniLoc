package com.build.analyzer.diff.java;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.github.gumtreediff.gen.TreeGenerator;
import com.github.gumtreediff.gen.jdt.AbstractJdtVisitor;
import com.github.gumtreediff.gen.jdt.JdtVisitor;
import com.github.gumtreediff.gen.jdt.cd.CdJdtVisitor;
import com.github.gumtreediff.tree.TreeContext;
import com.github.gumtreediff.gen.TreeGenerator;
import com.github.gumtreediff.tree.TreeContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;

public class MetaAbstractJdtTreeGenerator extends TreeGenerator {
	
	
	public TreeContext generate(File sourcefile) throws IOException {
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        Map pOptions = JavaCore.getOptions();
        pOptions.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
        pOptions.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
        pOptions.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
        pOptions.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
        parser.setCompilerOptions(pOptions);
        
        String source = null;
		try {
			source = readFile(sourcefile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		parser.setSource(source.toCharArray());	
      
		
        //AbstractJdtVisitor v = new CdJdtVisitor();
		
		TreeContext ctx=null;
		try
		{
			CompilationUnit unit = (CompilationUnit) parser.createAST(new NullProgressMonitor());	
			MetaAbstractJdtVisitor v = new MetaJdtVisitor(unit);
			
			unit.accept(v);
			ctx=v.getTreeContext();
		}catch(Exception e){
			System.out.println(e.getMessage());
		}

        //parser.createAST(null).accept(v);
		return ctx;
        
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

	@Override
	protected TreeContext generate(Reader r) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	
}
