/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.project.db.repo;

import com.axelor.apps.project.db.Project;
import com.axelor.db.JPA;
import com.axelor.team.db.Team;
import com.axelor.team.db.repo.TeamRepository;
import java.util.List;

public class TeamProjectRepository extends TeamRepository {
  @Override
  public Team save(Team team) {
    List<Project> projects =
        JPA.all(Project.class)
            .filter("self.team = :team AND self.synchronize = true")
            .bind("team", team)
            .fetch();
    projects
        .stream()
        .peek(Project::clearMembersUserSet)
        .peek(p -> team.getMembers().forEach(p::addMembersUserSetItem))
        .forEach(ProjectManagementRepository::setAllProjectMembersUserSet);
    return super.save(team);
  }
}
