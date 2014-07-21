/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.csv.script;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.axelor.auth.db.Group;
import com.axelor.auth.db.Permission;
import com.google.inject.persist.Transactional;


public class ImportPermission {
		
		@Transactional
		public Object importPermission(Object bean, Map values) {
			assert bean instanceof Permission;
	        try{
	            Permission permission = (Permission) bean;
				String groups = (String) values.get("group");
				if(permission.getId()!= null){
					if(groups != null && !groups.isEmpty()){
						for(Group group: Group.filter("code in ?1",Arrays.asList(groups.split("\\|"))).fetch()){
							Set<Permission> permissions = group.getPermissions();
							if(permissions == null)
								permissions = new HashSet<Permission>();
							permissions.add(Permission.find(permission.getId()));
							group.setPermissions(permissions);
							group.save();
						}
					}
				}
				return permission;
	        }catch(Exception e){
	            e.printStackTrace();
	        }
			return bean;
		}
		
}



