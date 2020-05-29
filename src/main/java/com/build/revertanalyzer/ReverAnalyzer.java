package com.build.revertanalyzer;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.ITree;

public class ReverAnalyzer {
	
	
	public String getChangeFileList(Map<String, List<Action>> filechangemap)
	{
		 StringBuilder str = new StringBuilder();
		
		Iterator<Entry<String, List<Action>>> it = filechangemap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, List<Action>> entry = (Entry<String, List<Action>>) it.next();
			String key = entry.getKey();
			str.append(key);
			str.append(";");			
		}
		
		return str.toString();
	}

	public boolean isCodeRevereted(Map<String, List<Action>> failchangemap, Map<String, List<Action>> fixchangemap) {
		boolean reverted = false;

		Iterator<Entry<String, List<Action>>> it = failchangemap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, List<Action>> entry = (Entry<String, List<Action>>) it.next();
			String key = entry.getKey();

			// Same file change found
			if (fixchangemap.containsKey(key)) {
				List<Action> failchange = failchangemap.get(key);

				List<Action> fixchange = fixchangemap.get(key);

				if (isAnyFileReverted(failchange, fixchange)) {
					reverted = true;
					break;
				}

			}

		}

		return reverted;
	}

	public boolean isAnyFileReverted(List<Action> failchange, List<Action> fixchange) {
		boolean reverted = false;
		int index = 0;

		while (index < failchange.size()) {

			Action change = failchange.get(index);

			if (isChangeFoundInChangeList(change, fixchange)) {
				reverted = true;

				break;
			}

			index++;
		}

		return reverted;
	}

	public boolean isChangeFoundInChangeList(Action ac, List<Action> fixchanges) {
		boolean samechangefound = false;
		int index = 0;

		while (index < fixchanges.size()) {
			Action fixchange = fixchanges.get(index);

			if (isChangeFromLeavetoRootMatched(ac, fixchange)) {

				samechangefound = true;
				break;
			}

			index++;
		}

		return samechangefound;
	}

	public boolean isChangeFromLeavetoRootMatched(Action ac1, Action ac2) {
		boolean matchfound = false;

		if (ac1.getNode().getLabel().equals(ac2.getNode().getLabel())) {
			ITree node1 = ac1.getNode();
			ITree node2 = ac2.getNode();

			matchfound = true;

			while (node1.getParent() != null && node2.getParent() != null) {
				node1 = node1.getParent();
				node2 = node2.getParent();
				
				if(node1.getLabel()==null && node2.getLabel()!=null)
				{
					matchfound = false;
					break;
				}
				
				else if(node1.getLabel()!=null && node2.getLabel()==null)
				{
					matchfound = false;
					break;
				}

				if (node1.getLabel()!=null && node2.getLabel()!=null && node1.getLabel().equals(node2.getLabel())) {
					matchfound = true;
				} else {
					matchfound = false;
					break;
				}

			}
		}

		return matchfound;
	}
	
	
	public int getRevertedFileCount(Map<String, List<Action>> failchangemap, Map<String, List<Action>> fixchangemap) {

		int count = 0;

		Iterator<Entry<String, List<Action>>> it = failchangemap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, List<Action>> entry = (Entry<String, List<Action>>) it.next();
			String key = entry.getKey();

			// Same file change found
			if (fixchangemap.containsKey(key)) {
				count++;

			}
		}

		return count;
	}
	
	

}
