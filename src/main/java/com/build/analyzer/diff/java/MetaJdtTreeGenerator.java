package com.build.analyzer.diff.java;

import java.io.File;

import com.github.gumtreediff.gen.Register;

@Register(id = "java-jdt", accept = "\\.java$")
public class MetaJdtTreeGenerator extends MetaAbstractJdtTreeGenerator {

	public MetaJdtTreeGenerator()
	{
		super();
	} 

}