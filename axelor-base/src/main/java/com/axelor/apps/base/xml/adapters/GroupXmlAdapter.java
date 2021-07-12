package com.axelor.apps.base.xml.adapters;

import com.axelor.auth.db.Group;
import com.axelor.auth.db.repo.GroupRepository;
import com.google.inject.Inject;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class GroupXmlAdapter extends XmlAdapter<String, Group> {

  private GroupRepository groupRepository;
  
  public GroupXmlAdapter() {}

  @Inject
  public GroupXmlAdapter(GroupRepository groupRepository) {
    this.groupRepository = groupRepository;
  }

  @Override
  public String marshal(Group group) throws Exception {

    return group.getCode();
  }

  @Override
  public Group unmarshal(String code) throws Exception {

    return groupRepository.findByCode(code);
  }
}
