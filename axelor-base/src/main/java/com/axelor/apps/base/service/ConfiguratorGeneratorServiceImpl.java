package com.axelor.apps.base.service;

import com.axelor.apps.base.db.ConfiguratorBinding;
import com.axelor.apps.base.db.ConfiguratorGenerator;
import com.axelor.apps.base.db.ConfiguratorGeneratorLine;
import com.axelor.apps.base.db.repo.ConfiguratorBindingRepository;
import com.axelor.studio.db.LinkScript;
import com.axelor.studio.db.LinkScriptArc;
import com.axelor.studio.db.repo.LinkScriptRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfiguratorGeneratorServiceImpl implements ConfiguratorGeneratorService {

  protected LinkScriptRepository linkScriptRepository;
  protected ConfiguratorBindingRepository configuratorBindingRepository;

  @Inject
  public ConfiguratorGeneratorServiceImpl(
      LinkScriptRepository linkScriptRepository,
      ConfiguratorBindingRepository configuratorBindingRepository) {
    this.linkScriptRepository = linkScriptRepository;
    this.configuratorBindingRepository = configuratorBindingRepository;
  }

  @Transactional
  @Override
  public void syncDependencies(ConfiguratorGenerator generator) {
    ConfiguratorBinding configuratorBinding =
        configuratorBindingRepository
            .all()
            .filter("self.metaModel = :metaModel")
            .bind("metaModel", generator.getMetaModel())
            .fetchOne();

    List<ConfiguratorGeneratorLine> configuratorGeneratorLineList =
        generator.getConfiguratorGeneratorLineList();

    if (configuratorGeneratorLineList == null) {
      return;
    }

    LinkScript mainLinkScript = configuratorBinding.getBusinessLogicLinkScript();

    List<LinkScriptArc> dependencyArcs = mainLinkScript.getDependencyArcs();

    // convert list to map to allow acces by name
    Map<String, LinkScriptArc> arcMap =
        dependencyArcs.stream().collect(Collectors.toMap(LinkScriptArc::getName, arc -> arc));
    Map<String, ConfiguratorGeneratorLine> lineMap =
        configuratorGeneratorLineList.stream()
            .collect(Collectors.toMap(line -> line.getMetaField().getName(), line -> line));

    // sync generator lines and linkScript dependencies
    for (Map.Entry<String, ConfiguratorGeneratorLine> entry : lineMap.entrySet()) {
      String name = entry.getKey();
      ConfiguratorGeneratorLine line = entry.getValue();

      if (arcMap.containsKey(name)) {
        // update linkScript if necessary
        LinkScriptArc arc = arcMap.get(name);
        if (!arc.getToLinkScript().equals(line.getLinkScript())) {
          arc.setToLinkScript(line.getLinkScript());
        }
      } else {
        // new arc to add
        LinkScriptArc linkScriptArc = new LinkScriptArc();
        linkScriptArc.setName(name);
        linkScriptArc.setToLinkScript(line.getLinkScript());
        mainLinkScript.addDependencyArc(linkScriptArc);
      }
    }

    // remove deleted generator lines
    dependencyArcs.removeIf(arc -> !lineMap.containsKey(arc.getName()));

    linkScriptRepository.save(mainLinkScript);
  }
}
