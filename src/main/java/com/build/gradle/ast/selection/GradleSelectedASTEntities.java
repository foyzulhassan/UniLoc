package com.build.gradle.ast.selection;

import java.util.ArrayList;
import java.util.List;

public class GradleSelectedASTEntities {
	private List<String> dependencyList;
	private List<String> taskList;
	private List<String> propertyList;
	private List<String> subprojList;
	
	public GradleSelectedASTEntities()
	{
		dependencyList=new ArrayList<>();
		taskList=new ArrayList<>();
		propertyList=new ArrayList<>();
		subprojList=new ArrayList<>();
	}
	
	public GradleSelectedASTEntities(List<String> deps,List<String> tasks, List<String> props, List<String> subprojs)
	{
		dependencyList=deps;
		taskList=tasks;
		propertyList=props;
		subprojList=subprojs;
	}
	
	public List<String> getDependencyList() {
		return dependencyList;
	}

	public void setDependencyList(List<String> dependencyList) {
		this.dependencyList = dependencyList;
	}

	public List<String> getTaskList() {
		return taskList;
	}

	public void setTaskList(List<String> taskList) {
		this.taskList = taskList;
	}

	public List<String> getPropertyList() {
		return propertyList;
	}

	public void setPropertyList(List<String> propertyList) {
		this.propertyList = propertyList;
	}

	public List<String> getSubprojList() {
		return subprojList;
	}

	public void setSubprojList(List<String> subprojList) {
		this.subprojList = subprojList;
	}

}
