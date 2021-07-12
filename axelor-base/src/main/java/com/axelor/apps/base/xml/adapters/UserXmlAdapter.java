package com.axelor.apps.base.xml.adapters;

import com.axelor.auth.db.User;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class UserXmlAdapter extends XmlAdapter<String, User> {

  @Override
  public String marshal(User user) throws Exception {
    // TODO Auto-generated method stub
    return user.getCode();
  }

  @Override
  public User unmarshal(String v) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
}
