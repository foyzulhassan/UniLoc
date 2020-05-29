package com.build.analyzer.diff.gradle;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class GradlePatchFormater {

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

	public String getXMLPatch(HashMap<Integer, ExpPerLines> expmap) {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = null;
		try {
			docBuilder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// root elements
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("patch");
		doc.appendChild(rootElement);
		return null;
	}

	public String getXMLPatch(List<Integer> linelist, HashMap<Integer, ExpPerLines> expmap) {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = null;
		try {
			docBuilder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// root elements
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("patch");
		doc.appendChild(rootElement);

		for (int lineno = 0; lineno < linelist.size(); lineno++) {

			Element line = doc.createElement("lineno");
			rootElement.appendChild(line);

			// set attribute to staff element
			int lineid=linelist.get(lineno);
			
			Attr attr = doc.createAttribute("id");
			attr.setValue(Integer.toString(lineid));
			line.setAttributeNode(attr);

			int key = linelist.get(lineno);
			ExpPerLines obj = expmap.get(key);

			List<GradleChange> gdchanges = obj.getChangesPerLine();

			for (int chindex = 0; chindex < gdchanges.size(); chindex++) {

				GradleChange change = gdchanges.get(chindex);

				Element exp = doc.createElement("exp");
				line.appendChild(exp);
				// set attribute to staff element
				Attr expattr = doc.createAttribute("id");
				expattr.setValue(Integer.toString(chindex));
				exp.setAttributeNode(expattr);

				Element operation = doc.createElement("operation");
				operation.appendChild(doc.createTextNode(change.getOperationName()));
				exp.appendChild(operation);

				Element nodetype = doc.createElement("nodetype");
				String nstype=" ";
				
				//quickfix for nodetype missmatch
				if(isNumeric(change.getNodeType()))
				{
					nstype=TypeUtil.getExpressionName(Integer.parseInt(change.getNodeType()));
				}
				else
				{
					nstype=change.getNodeType();
				}
				
				nodetype.appendChild(doc.createTextNode(nstype));
				exp.appendChild(nodetype);

				Element nodeexp = doc.createElement("nodeexp");
				nodeexp.appendChild(doc.createTextNode(change.getNodeExp()));
				exp.appendChild(nodeexp);

				Element nodeparenttype = doc.createElement("nodeparenttype");
				
				
				//quickfix for nodetype missmatch
				String pstype=" ";
				if(isNumeric(change.getParentType()))
				{
					pstype=TypeUtil.getExpressionName(Integer.parseInt(change.getParentType()));
				}
				else
				{
					pstype=change.getParentType();
					int expindex=pstype.indexOf(":");	
					
					
					String part1=" ";
					String part2=" ";
					
					if(expindex>=0)
					{
						part1=pstype.substring(0, expindex);
						String tpart1=part1.trim();
						
						
						if(isNumeric(tpart1))
							part1=TypeUtil.getExpressionName(Integer.parseInt(tpart1));
						
						part2=pstype.substring(expindex+1);
						
						pstype=part1+":"+part2;
					}
					
				}
										
			
				nodeparenttype.appendChild(doc.createTextNode(pstype));
				exp.appendChild(nodeparenttype);

				Element nodeparentexp = doc.createElement("nodeparentexp");
				nodeparentexp.appendChild(doc.createTextNode(change.getParentExp()));
				exp.appendChild(nodeparentexp);

				Element nodeblockname = doc.createElement("nodeblockname");
				nodeblockname.appendChild(doc.createTextNode(change.getBlockName()));
				exp.appendChild(nodeblockname);

				Element nodetaskname = doc.createElement("nodetaskname");
				nodetaskname.appendChild(doc.createTextNode(change.getTaskName()));
				exp.appendChild(nodetaskname);

				Element nodestatementexp = doc.createElement("nodestatementexp");
				nodestatementexp.appendChild(doc.createTextNode(change.getStatementExp()));
				exp.appendChild(nodestatementexp);

			}

		}

		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		try {
			Transformer transformer = transformerFactory.newTransformer();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DOMSource source = new DOMSource(doc);
		
		StringWriter writer = new StringWriter();
	    StreamResult result = new StreamResult(writer);
	    TransformerFactory tf = TransformerFactory.newInstance();
	    Transformer transformer = null;
		try {
			transformer = tf.newTransformer();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String xmlstr = "";

		xmlstr = writer.toString();		

		return xmlstr;
	}
	
	public static boolean isNumeric(String str)  
	{  
	  try  
	  {  
	    int d = Integer.parseInt(str);  
	  }  
	  catch(NumberFormatException nfe)  
	  {  
	    return false;  
	  }  
	  return true;  
	}

}
