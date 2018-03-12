package com.axelor.apps.project.db.repo;

import com.axelor.apps.project.db.Project;
import com.axelor.db.JPA;
import com.axelor.team.db.Team;
import com.axelor.team.db.repo.TeamRepository;

import java.util.List;

public class TeamProjectRepository extends TeamRepository {
    @Override
    public Team save(Team team) {
        List<Project> projects = JPA.all(Project.class).filter("self.team = :team AND self.synchronize = true")
                .bind("team", team).fetch();
        projects.stream().peek(Project::clearMembersUserSet)
                .peek(p -> team.getMembers().forEach(p::addMembersUserSetItem))
                .forEach(ProjectManagementRepository::setAllProjectMembersUserSet);
        return super.save(team);
    }
}
