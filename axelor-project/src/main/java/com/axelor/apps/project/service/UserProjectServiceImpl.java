package com.axelor.apps.project.service;

import com.axelor.apps.project.db.Project;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class UserProjectServiceImpl implements UserProjectService {

  protected UserRepository userRepository;

  @Inject
  public UserProjectServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void setActiveProject(User user, Project project) {
    user.setActiveProject(project);
    userRepository.save(user);
  }
}
