package com.build.commitanalyzer;

//import pfe.datas.singletons.AssignmentSingleton;
//import pfe.datas.singletons.FieldWriteSingleton;
//import pfe.datas.singletons.LocalVariableSingleton;
//import pfe.datas.singletons.ReturnSingleton;

public class StoreResult {

	private String project = "";

	private String projectOwner = "";

	public StoreResult(String project, String projectOwner) {
		this.project = project;
		this.projectOwner = projectOwner;
	}

	public void store(String actionType, String commitID, int numberOfChanges) {
//		switch (actionType) {
//		case ("CtLocalVariableImpl"):
//			LocalVariableSingleton.getInstance().addCommit(commitID, numberOfChanges);
//		break;
//		case ("CtReturnImpl"):
//			ReturnSingleton.getInstance().addCommit(commitID, numberOfChanges);
//		break;
//		case ("CtFieldWriteImpl"):
//			FieldWriteSingleton.getInstance().addCommit(commitID, numberOfChanges);
//			break;
//		case ("CtAssignmentImpl"):
//			AssignmentSingleton.getInstance().addCommit(commitID, numberOfChanges);
//		break;
//		default:
//			break;
//		}
	}
}
