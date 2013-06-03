package com.axelor.sn.service;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import com.axelor.auth.db.Group;
import com.axelor.db.*;
import com.axelor.meta.db.MetaMenu;

public class SNMetaService 
{
	String acknowledgment="";
	public String setRestrictions(MetaMenu menu,Set<Group> group)
	{
		try
		{
			EntityManager em=JPA.em();
			EntityTransaction tx=em.getTransaction();
			tx.begin();
			if(tx.isActive())
			{
				menu.setGroups(group);
				menu.merge();
				tx.commit();
				acknowledgment="Successfully Restricted Group(s)";
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}

		return acknowledgment;
	}
}
