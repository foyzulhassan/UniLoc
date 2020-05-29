package com.build.commitanalyzer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.build.analyzer.entity.CommitChange;
import com.github.gumtreediff.actions.ActionGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.gen.Generators;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;



public class FileDiff {

	private List<Action> totalactions;

	public List<Action> getTotalactions() {
		return totalactions;
	}

	public FileDiff() {
		totalactions = new ArrayList<Action>();
	}

	public CommitChange diffJavaFiles(File filesource, File filedest) throws Exception {

		HashMap<ITree, Boolean> methodMap = new HashMap<ITree, Boolean>();
		HashMap<ITree, Boolean> classMap = new HashMap<ITree, Boolean>();
		HashMap<ITree, Boolean> methodBodyMap = new HashMap<ITree, Boolean>();
		HashMap<ITree, Boolean> fieldMap = new HashMap<ITree, Boolean>();
		HashMap<ITree, Boolean> importMap = new HashMap<ITree, Boolean>();

		Run.initGenerators();
		CommitChange commitchangetracker = new CommitChange();
		String file1 = filesource.toString();
		String file2 = filedest.toString();

		TreeContext tsrc;
		TreeContext tdst;

		try {
			tsrc = Generators.getInstance().getTree(file1);
			tdst = Generators.getInstance().getTree(file2);

			Matcher match = Matchers.getInstance().getMatcher(tsrc.getRoot(), tdst.getRoot());

			match.match();

			ActionGenerator g = new ActionGenerator(tsrc.getRoot(), tdst.getRoot(), match.getMappings());

			g.generate();

			totalactions = g.getActions();

			for (int index = 0; index < totalactions.size(); index++) {
				Action ac = totalactions.get(index);

				// String type=tsrc.getTypeLabel(ac.getNode().getType());
				// String
				// parenttype=tsrc.getTypeLabel(ac.getNode().getParent().getType());

				ITree currentnode = ac.getNode();
				// ITree parentnode=ac.getNode().getParent();
				// if(!ac.getNode().isLeaf())
				// System.out.println(type+"--->"+parenttype+"===>"+ac.getNode().toPrettyString(tsrc)+"--->"+ac.getNode().getLabel());
				// ac.getNode();

				// for import
				if (JavaChangeClassifier.isImportStamentChange(tsrc, currentnode)) {
					ITree node = JavaChangeClassifier.getParentNode();
					if (node != null && importMap.get(node) == null) {
						commitchangetracker.setImportChange(commitchangetracker.getImportChange() + 1);
						importMap.put(node, true);
					}
				}

				// FOR CLASS CHANGE
				else if (JavaChangeClassifier.isClassStamentChange(tsrc, currentnode)) {
					ITree node = JavaChangeClassifier.getParentNode();

					if (node != null && classMap.get(node) == null) {
						commitchangetracker.setClassChange(commitchangetracker.getClassChange() + 1);
						classMap.put(node, true);
					}
				}

				// FOR METHOD CHANGE
				else if (JavaChangeClassifier.isMethodChange(tsrc, currentnode)) {
					ITree node = JavaChangeClassifier.getParentNode();

					if (node != null && methodMap.get(node) == null) {
						commitchangetracker.setMethodChange(commitchangetracker.getMethodChange() + 1);
						methodMap.put(node, true);
					}
				}

				// block
				else if (JavaChangeClassifier.isMethodBodyChange(tsrc, currentnode)) {
					ITree node = JavaChangeClassifier.getParentNode();

					if (node != null && methodBodyMap.get(node) == null) {
						commitchangetracker.setMethodBodyChange(commitchangetracker.getMethodBodyChange() + 1);
						methodBodyMap.put(node, true);
					}
				}

				// FILED CHANGE
				else if (JavaChangeClassifier.isFieldChange(tsrc, currentnode)) {
					ITree node = JavaChangeClassifier.getParentNode();

					if (node != null && fieldMap.get(node) == null) {
						commitchangetracker.setFieldChange(commitchangetracker.getFieldChange() + 1);
						fieldMap.put(node, true);
					}
				}
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return commitchangetracker;
	}
}
