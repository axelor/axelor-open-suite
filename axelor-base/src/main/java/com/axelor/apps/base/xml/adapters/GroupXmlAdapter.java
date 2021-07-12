package com.axelor.apps.base.xml.adapters;

import com.axelor.auth.db.Group;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class GroupXmlAdapter extends XmlAdapter<String, Group> {

  @Override
  public String marshal(Group group) throws Exception {
    // TODO Auto-generated method stub
    return group.getCode();
  }

  @Override
  public Group unmarshal(String v) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
}
