package com.axelor.apps.base.xml.adapters;

import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.google.inject.Inject;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class UserXmlAdapter extends XmlAdapter<String, User> {

  private UserRepository userRepository;
  public UserXmlAdapter() {}
  @Inject
  public UserXmlAdapter(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public String marshal(User user) throws Exception {

    return user.getCode();
  }

  @Override
  public User unmarshal(String code) throws Exception {

    return userRepository.findByCode(code);
  }
}
