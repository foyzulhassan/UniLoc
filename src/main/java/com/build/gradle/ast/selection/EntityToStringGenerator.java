package com.build.gradle.ast.selection;

import java.util.List;
import java.util.StringTokenizer;

public class EntityToStringGenerator {

	public static String getStringFromEntity(GradleSelectedASTEntities entity) {

		StringBuilder strbuilder = new StringBuilder();
		List<String> deps = entity.getDependencyList();

		if (deps.size() > 0)
			strbuilder.append("dependencies ");

		for (String dep : deps) {
			StringTokenizer st = new StringTokenizer(dep, " ,!*^/:'\"");
			while (st.hasMoreTokens()) {
				strbuilder.append(st.nextToken());
				strbuilder.append(" ");
			}
		}

		List<String> tasks = entity.getTaskList();

		for (String task : tasks) {
			StringTokenizer st = new StringTokenizer(task, " ,!*^/:'\"");
			strbuilder.append("task ");
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				strbuilder.append(token);
				strbuilder.append(" ");

			}
		}

		List<String> props = entity.getPropertyList();

		for (String prop : props) {
			StringTokenizer st = new StringTokenizer(prop, " ,!*^/:'\"");
			while (st.hasMoreTokens()) {
				strbuilder.append(st.nextToken());
				strbuilder.append(" ");
			}
		}

		List<String> subprops = entity.getSubprojList();

		for (String subprop : subprops) {
			StringTokenizer st = new StringTokenizer(subprop, " ,!*^/:'\"");
			strbuilder.append("project ");
			while (st.hasMoreTokens()) {
				strbuilder.append(st.nextToken());
				strbuilder.append(" ");
			}
		}

		return strbuilder.toString();
	}

}
