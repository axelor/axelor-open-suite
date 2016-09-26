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
package com.axelor.studio.service.wkf;

import java.io.StringReader;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.axelor.auth.db.repo.RoleRepository;
import com.axelor.common.Inflector;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaField;
import com.axelor.studio.db.Wkf;
import com.axelor.studio.db.WkfNode;
import com.axelor.studio.db.WkfTransition;
import com.axelor.studio.db.repo.WkfNodeRepository;
import com.axelor.studio.db.repo.WkfRepository;
import com.axelor.studio.db.repo.WkfTransitionRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

/**
 * Service class handle bpmn xml processing of workflow. It create/remove
 * workflow nodes and transitions using bpmn xml. *
 * 
 * @author axelor
 *
 */
public class WkfDesignerService {
	
	private final Logger log = LoggerFactory.getLogger(WkfDesignerService.class);
	
	protected static final String WKF_STATUS = "wkfStatus";

	protected MetaField statusField = null;

	protected String dasherizeModel = null;

	protected String modelName = null;

	protected Inflector inflector;

	protected String defaultStatus = null;

	private Set<WkfNode> nodes = new LinkedHashSet<>();

	private Set<WkfTransition> transitions = new LinkedHashSet<>();

	private Wkf instance;
	
	private List<Integer> nodeSequences;
	
	@Inject
	protected RoleRepository roleRepo;

	

	/**
	 * Method parse xml doc to create workflow nodes from it. It set incoming
	 * and outgoing tranistions of node.
	 * 
	 * @param doc
	 */
	public void traverseXMLElement(Document doc) {
		
		nodeSequences = new ArrayList<Integer>();

		NodeList list = doc.getElementsByTagName("*");

		int nodeCount = 1;
		
		Map<String, WkfNode> nodeMap = getNodeMap();
		
		for (int i = 0; i < list.getLength(); i++) {

			Element element = (Element) list.item(i);

			if ((!element.getParentNode().getNodeName().equals("process"))
					|| element.getNodeName().equals("sequenceFlow")) {
				continue;
			}

			String elementName = element.getTagName();

			WkfNode node = nodeMap.get(element.getAttribute("id"));

			if (node == null) {
				node = new WkfNode();
				node.setXmlId(element.getAttribute("id"));
				node.setName(element.getAttribute("name"));
				node.setTitle(node.getName());
				node.setWkf(instance);
				while(nodeSequences.contains(nodeCount)) {
					nodeCount++;
				}
				node.setSequence(nodeCount);
				nodeCount++;
				if (elementName == "startEvent")
					node.setStartNode(true);
				else if (elementName == "endEvent")
					node.setEndNode(true);
			} else {
				node.setName(element.getAttribute("name"));
			}

			NodeList incomings = element.getElementsByTagName("incoming");

			for (int j = 0; j < incomings.getLength(); j++) {
				Element incElement = (Element) incomings.item(j);
				String innerText = incElement.getTextContent();

				for (WkfTransition trn : transitions) {
					if (trn.getXmlId().equals(innerText)) {
						Set<WkfTransition> existIncomings = node.getIncomming();

						if (existIncomings == null)
							existIncomings = new HashSet<>();

						existIncomings.add(trn);
						node.setIncomming(existIncomings);
						trn.setTarget(node);
					}
				}
			}

			NodeList outgoings = element.getElementsByTagName("outgoing");

			for (int j = 0; j < outgoings.getLength(); j++) {
				Element outElement = (Element) outgoings.item(j);
				String innerText = outElement.getTextContent();

				for (WkfTransition trn : transitions) {
					if (trn.getXmlId().equals(innerText)) {
						Set<WkfTransition> existOutgoings = node.getOutgoing();

						if (existOutgoings == null)
							existOutgoings = new HashSet<>();

						existOutgoings.add(trn);
						node.setOutgoing(existOutgoings);
						trn.setSource(node);
					}
				}
			}
			nodes.add(node);
		}
	}

	private Map<String, WkfNode> getNodeMap() {
		
		WkfNodeRepository wkfNodeRepository = Beans
				.get(WkfNodeRepository.class);
		
		Map<String, WkfNode> nodeMap = new HashMap<String, WkfNode>();
		if (instance != null) {
			List<WkfNode> nodes = wkfNodeRepository.all().filter("self.wkf.id = ?1", instance.getId()).fetch();
			for (WkfNode node : nodes) {
				nodeMap.put(node.getXmlId(), node);
			}
		}
		else {
			log.debug("Worklfow not saved");
		}
		
		return nodeMap;
	}

	/**
	 * Method fetch bpmn xml from workflow. It generate document from xml using
	 * dom parser. From dom document generates transitions and call method to
	 * creat nodes. *
	 * 
	 * @param instance
	 *            Workflow instance
	 * @throws Exception
	 */
	@Transactional
	public void processXml(Wkf instance) throws Exception {
		this.instance = instance;
		String bpmnXml = instance.getBpmnXml();
		if (bpmnXml != null) {
			DocumentBuilder db = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(bpmnXml));

			Document doc = db.parse(is);

			WkfRepository wkfRepository = Beans.get(WkfRepository.class);
			WkfTransitionRepository wkfTransitionRepository = Beans
					.get(WkfTransitionRepository.class);

			NodeList list;

			list = doc.getElementsByTagName("sequenceFlow");
			for (int i = 0; i < list.getLength(); i++) {
				Element element = (Element) list.item(i);

				WkfTransition transition = wkfTransitionRepository
						.all()
						.filter("self.wkf.id = ? and self.xmlId = ?",
								instance.getId(), element.getAttribute("id"))
						.fetchOne();

				if (transition == null) {
					transition = new WkfTransition();
					transition.setXmlId(element.getAttribute("id"));
					transition.setName(element.getAttribute("name"));
					transition.setWkf(instance);
				} else {
					transition.setName(element.getAttribute("name"));
				}

				transitions.add(transition);
			}

			List<WkfNode> allRemoveNodes = instance.getNodes();

			if (allRemoveNodes.size() > 0) {
				for (WkfNode tempNode : allRemoveNodes) {
					tempNode.getIncomming().clear();
					tempNode.getOutgoing().clear();
				}
			}

			traverseXMLElement(doc);

			instance.getTransitions().clear();
			instance.getNodes().clear();

			for (WkfTransition transition : transitions) {
				instance.addTransition(transition);
			}

			for (WkfNode node : nodes) {
				instance.addNode(node);
			}

			wkfRepository.save(instance);
		} else {
			return;
		}

	}

}
