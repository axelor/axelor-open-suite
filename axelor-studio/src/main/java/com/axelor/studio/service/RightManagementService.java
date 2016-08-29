/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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
package com.axelor.studio.service;

import java.util.Iterator;
import java.util.List;

import com.axelor.auth.db.Group;
import com.axelor.auth.db.Permission;
import com.axelor.auth.db.Role;
import com.axelor.auth.db.repo.GroupRepository;
import com.axelor.auth.db.repo.PermissionRepository;
import com.axelor.auth.db.repo.RoleRepository;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.MetaPermission;
import com.axelor.meta.db.MetaPermissionRule;
import com.axelor.meta.db.repo.MetaPermissionRepository;
import com.axelor.meta.db.repo.MetaPermissionRuleRepository;
import com.axelor.studio.db.RightManagement;
import com.axelor.studio.db.repo.RightManagementRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

/**
 * This service class create Permissions and MetaPermissions from
 * RightManagement objects. MetaModel and MetaField contains o2m to
 * RightManagement for model and field permission.
 * 
 * @author axelor
 *
 */
public class RightManagementService {

	@Inject
	private GroupRepository groupRepo;

	@Inject
	private RoleRepository roleRepo;

	@Inject
	private PermissionRepository permissionRepo;

	@Inject
	private MetaPermissionRepository metaPermissionRepo;

	@Inject
	private RightManagementRepository rightManagementRepo;

	@Inject
	private MetaPermissionRuleRepository ruleRepo;

	/**
	 * Method fetch all edited RightManagement and create/update existing
	 * permissions.
	 */
	public void updateRights() {

		List<RightManagement> modelPermissions = rightManagementRepo.all()
				.filter("self.edited = true and self.metaModel is not null")
				.fetch();
		List<RightManagement> fieldPermissions = rightManagementRepo.all()
				.filter("self.edited = true and self.metaField is not null")
				.fetch();

		updateModelPermissions(modelPermissions.iterator());

		updateFieldPermissions(fieldPermissions.iterator());

		List<RightManagement> allRights = modelPermissions;
		allRights.addAll(fieldPermissions);

		updateEdited(allRights);

	}

	/**
	 * Method update 'edited' boolean from RightManagement. This method get
	 * called at end of processing.
	 * 
	 * @param allRights
	 */
	@Transactional
	public void updateEdited(List<RightManagement> allRights) {

		for (RightManagement rightManagement : allRights) {
			rightManagement.setEdited(false);
			rightManagementRepo.save(rightManagement);
		}
	}

	/**
	 * Method create Permission for model from given RightManagement
	 * 
	 * @param iterator
	 *            RightManagement Iterator
	 */
	@Transactional
	public void updateModelPermissions(Iterator<RightManagement> iterator) {

		if (!iterator.hasNext()) {
			return;
		}

		RightManagement rightManagement = iterator.next();
		Group group = rightManagement.getAuthGroup();
		Role role = rightManagement.getAuthRole();
		MetaModel model = rightManagement.getMetaModel();
		
		String permissionName = rightManagement.getName();
		Permission permission = permissionRepo.all()
				.filter("self.name = ?1", permissionName).fetchOne();

		if (permission == null) {
			permission = new Permission(permissionName);
			permission.setObject(model.getFullName());
			if (group != null) {
				group.addPermission(permission);
				groupRepo.save(group);
			} else {
				role.addPermission(permission);
				roleRepo.save(role);
			}
		}

		permission.setCanCreate(rightManagement.getCanCreate());
		permission.setCanRead(rightManagement.getCanRead());
		permission.setCanWrite(rightManagement.getCanWrite());
		permission.setCanExport(rightManagement.getCanExport());
		permission.setCanRemove(rightManagement.getCanRemove());
		permission.setCondition(rightManagement.getCondition());
		permission.setConditionParams(rightManagement.getConditionParams());

		permission = permissionRepo.save(permission);

		updateModelPermissions(iterator);

	}

	/**
	 * It create field permission (MetaPermission) from given RightManagement.
	 * 
	 * @param iterator
	 *            RightManagement iterator.
	 */
	@Transactional
	public void updateFieldPermissions(Iterator<RightManagement> iterator) {

		if (!iterator.hasNext()) {
			return;
		}

		RightManagement rightMgmt = iterator.next();
		Group group = rightMgmt.getAuthGroup();
		Role role = rightMgmt.getAuthRole();
		MetaField field = rightMgmt.getMetaField();
		MetaModel model = field.getMetaModel();
		String fieldName = field.getName();
		String permissionName = rightMgmt.getName();
		
		MetaPermission metaPermission = metaPermissionRepo.all()
				.filter("self.name = ?1", permissionName).fetchOne();

		if (metaPermission == null) {
			metaPermission = new MetaPermission(permissionName);
			metaPermission.setObject(model.getFullName());
			metaPermission = metaPermissionRepo.save(metaPermission);
			if (group != null) {
				group.addMetaPermission(metaPermission);
				groupRepo.save(group);
			} else {
				role.addMetaPermission(metaPermission);
				roleRepo.save(role);
			}

		}

		MetaPermissionRule rule = ruleRepo
				.all()
				.filter("self.metaPermission.name = ?1 and self.field = ?2",
						permissionName, fieldName).fetchOne();

		if (rule == null) {
			rule = new MetaPermissionRule();
			rule.setField(fieldName);
			rule.setMetaPermission(metaPermission);
		}

		rule.setCanRead(rightMgmt.getCanRead());
		rule.setCanWrite(rightMgmt.getCanWrite());
		rule.setCanExport(rightMgmt.getCanExport());
		rule.setHideIf(rightMgmt.getHideIf());
		rule.setReadonlyIf(rightMgmt.getReadonlyIf());

		ruleRepo.save(rule);

		updateFieldPermissions(iterator);

	}

}
