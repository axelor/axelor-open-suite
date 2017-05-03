package com.axelor.studio.service.data;

import java.util.Map;

import com.axelor.studio.db.WkfNode;
import com.axelor.studio.db.WkfTransition;

public class ImportScript {
	
	
	public Object updateTransitionNode(Object bean, Map<String, Object> values){
		
		WkfTransition transition = (WkfTransition)bean;
		
		WkfNode source = transition.getSource();
		if(source != null){
			source.addOutgoing(transition);
		}
		
		WkfNode target = transition.getTarget();
		if(target != null){
			target.addIncomming(transition);
		}
		
		
		return transition;
	}
}