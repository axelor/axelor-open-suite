/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
(function(){

var ui = angular.module('axelor.ui');


function CustomContextPadProvider(contextPad, modeling, elementFactory, connect, create, popupMenu, canvas, rules) {

    contextPad.registerProvider(this);

    this._contextPad = contextPad;

    this._modeling = modeling;

    this._elementFactory = elementFactory;
    this._connect = connect;
    this._create = create;
    this._popupMenu = popupMenu;
    this._canvas = canvas;
    this._rules = rules;
}

CustomContextPadProvider.$inject = [
    'contextPad',
    'modeling',
    'elementFactory',
    'connect',
    'create',
    'popupMenu',
    'canvas',
    'rules'
];

CustomContextPadProvider.prototype.onElementEdit = function(e) {

};

CustomContextPadProvider.prototype.getContextPadEntries = function (element) {
    var contextPad = this._contextPad,
        modeling = this._modeling,

        elementFactory = this._elementFactory,
        connect = this._connect,
        create = this._create,
        popupMenu = this._popupMenu,
        canvas = this._canvas,
        rules = this._rules;

    var actions = {};

    if (element.type === 'label') {
        return actions;
    }

    var businessObject = element.businessObject;

    var assign = _.extend;
    var isArray = _.isArray;

    function is(element, type) {
        return element.$instanceOf(type);
    }

    function isAny(element, types) {
        for (var i = 0; i < types.length; i++) {
            if (is(element, types[i])) {
                return true;
            }
        }
        return false;
    }

    function startConnect(event, element, autoActivate) {
        connect.start(event, element, autoActivate);
    }

    function removeElement(e) {
        modeling.removeElements([element]);
    }
    
    function appendAction(type, className, title, options) {
        if (typeof title !== 'string') {
            options = title;
            title = 'Append ' + type.replace(/^bpmn\:/, '');
        }

        function appendListener(event, element) {
            var shape = elementFactory.createShape(assign({ type: type }, options));
            create.start(event, shape, element);
        }

        return {
            group: 'model',
            className: className,
            title: title,
            action: {
                dragstart: appendListener,
                click: appendListener
            }
        };
    }

    if (is(businessObject, 'bpmn:FlowNode')) {
        if (!is(businessObject, 'bpmn:EndEvent')) {
            assign(actions, {
                'append.end-event': appendAction('bpmn:EndEvent', 'bpmn-icon-end-event-none', _t('Append end node')),
                'append.append-task': appendAction('bpmn:Task', 'bpmn-icon-task', _t('Append intermediate node'))
            });
        }
    }

    if (isAny(businessObject, ['bpmn:FlowNode', 'bpmn:InteractionNode'])) {
        assign(actions, {
            'connect': {
                group: 'connect',
                className: 'bpmn-icon-connection-multi',
                title: _t('Connect using transition'),
                action: {
                    click: startConnect,
                    dragstart: startConnect
                }
            }
        });
    }
    assign(actions, {
        'delete': {
            group: 'edit',
            className: 'bpmn-icon-trash',
            title: 'Remove',
            action: {
                click: removeElement,
                dragstart: removeElement
            }
        },
        'edit': {
            group: 'edit',
            className: 'fa-pencil',
            title: 'Edit',
            action: {
                click: this.onElementEdit
            }
        }
    });
 
    return actions;
};

function CustomPaletteProvider(palette, create, elementFactory, spaceTool, lassoTool, handTool) {

    this._palette = palette;
    this._create = create;
    this._elementFactory = elementFactory;
    this._spaceTool = spaceTool;
    this._lassoTool = lassoTool;
    this._handTool = handTool;

    palette.registerProvider(this);
}

CustomPaletteProvider.$inject = [
    'palette',
    'create',
    'elementFactory',
    'spaceTool',
    'lassoTool',
    'handTool'
];

CustomPaletteProvider.prototype.getPaletteEntries = function (element) {

    var actions = {},
        create = this._create,
        elementFactory = this._elementFactory,
        spaceTool = this._spaceTool,
        lassoTool = this._lassoTool,
        handTool = this._handTool;

    var assign = _.extend;

    function createAction(type, group, className, title, options) {

        function createListener(event) {
            var shape = elementFactory.createShape(assign({ type: type }, options));
            if (options) {
                shape.businessObject.di.isExpanded = options.isExpanded;
            }
            create.start(event, shape);
        }

        var shortType = type.replace(/^bpmn\:/, '');

        return {
            group: group,
            className: className,
            title: title || 'Create ' + shortType,
            action: {
                dragstart: createListener,
                click: createListener
            }
        };
    }

    function createParticipant(event, collapsed) {
        create.start(event, elementFactory.createParticipantShape(collapsed));
    }

    assign(actions, {
        'hand-tool': {
            group: 'tools',
            className: 'bpmn-icon-hand-tool',
            title: _t('Activate the hand tool'),
            action: {
                click: function (event) {
                    handTool.activateHand(event);
                }
            }
        },
        'tool-separator': {
            group: 'tools',
            separator: true
        },
        'create.start-event': createAction(
            'bpmn:StartEvent', 'event', 'bpmn-icon-start-event-none', _t('Create start node')
        ),
        'create.end-event': createAction(
            'bpmn:EndEvent', 'event', 'bpmn-icon-end-event-none', _t('Create end node')
        ),
        'create.task': createAction(
            'bpmn:Task', 'activity', 'bpmn-icon-task', _t('Create intermediate node')
        )
    });

    return actions;
};

function newDiragram(rec) {
	var record = rec || {};
	return '<?xml version="1.0" encoding="UTF-8"?>' +
	'<definitions xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" ' +
		'xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL" ' +
		'xmlns:x="http://axelor.com" ' +
		'xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" ' +
		'xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" ' +
		'targetNamespace="http://bpmn.io/schema/bpmn" ' +
		'id="Definitions_1">' +
		'<process id="Process_1" name="'+ record.name +'" x:id="'+ record.id +'" isExecutable="false">' +
		'</process>' +
		'<bpmndi:BPMNDiagram id="BPMNDiagram_1">' +
		'<bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_1">' +
		'</bpmndi:BPMNPlane>' +
		'</bpmndi:BPMNDiagram>' +
	'</definitions>';
}

ui.formInput('BpmnEditor', {

	css: "bpmn-editor",

	controller: ['$scope', '$element', 'ActionService', function ($scope, $element, ActionService) {

		CustomContextPadProvider.prototype.onElementEdit = function(e, element) {
			if(element.type === "bpmn:SequenceFlow") {
				return onTransitionEdit(element);
			}
			else {
				return onNodeEdit(element);
			}
		}

		function onNodeEdit(element) {

			var child = $scope.$new();
			var id = element.id;
			child.getContext = function() {
				return {
					_model: "com.axelor.studio.db.WkfNode",
					xmlId: id,
					wkf: {
						id: $scope.record.id
					}
				};
			};
			var handler = ActionService.handler(child, $element, {
				action: "com.axelor.studio.web.WkfController:onNodeEdit"
			});

			handler.handle().then(function () {

			});
		}

		function onTransitionEdit(element) {

			var child = $scope.$new();
			var id = element.id;
			child.getContext = function() {
				return {
					_model: "com.axelor.studio.db.WkfTransition",
					xmlId: id,
					wkf: {
						id: $scope.record.id
					}
				};
			};
			var handler = ActionService.handler(child, $element, {
				action: "com.axelor.studio.web.WkfController:onTransitionEdit"
			});
			
			handler.handle().then(function () {

			});
		}
	}],

	link: function(scope, element, attrs, model) {
		var canvas = element.find('.bpmn-canvas');

		var overrideModule = {
			paletteProvider: [ 'type', CustomPaletteProvider ],
			contextPadProvider: [ 'type', CustomContextPadProvider ]
		};

		// initialize bpmn modeler
		var modeler = new BpmnJS({
			container: canvas[0],
			additionalModules: [overrideModule]
		});

		var last = null;

		function doSave() {
			last = scope.record;
			modeler.saveXML({ format: true }, function(err, xml) {
				scope.setValue(xml, true);
			});
		}

		function doLoad(xml) {
			modeler.importXML(xml, function(err) {});
		}

		model.$parsers.unshift(function(value) {
			
			var invalidElements = _.filter(modeler.definitions.rootElements[0].flowElements, function(e) {
				return !e.name;
			});
			
			var elements = _.filter(modeler.definitions.rootElements[0].flowElements, function(e) {
				return e.$type != "bpmn:SequenceFlow" && e.$type != "bpmn:StartEvent";
			});

			var start = _.filter(modeler.definitions.rootElements[0].flowElements, function(e) {
	              return e.$type == "bpmn:StartEvent" && (e.outgoing || []).length > 0;
	          });

			var valid = _.all(elements, function(e) {
				return (e.incoming || []).length > 0 && start.length == 1 && invalidElements.length == 0;
			});
			
			return valid ? value : null;
		});

		model.$render = function () {
			if (last === scope.record) {
				return;
			}
			last = scope.record;
			var xml = model.$viewValue || newDiragram(scope.record);
			scope.waitForActions(function () {
				return doLoad(xml);
			}, 100);
		}
		
		scope.$watch("record.$bpmnDefault", function() {
			var xml = scope.record.$bpmnDefault;
			scope.waitForActions(function () {
				return doLoad(xml);
			}, 100);
		})
		
		modeler.on(['shape.added', 'connection.added',
					'shape.removed', 'connection.removed',
					'shape.changed', 'connection.changed'], function (e) {
			scope.$timeout(doSave);
		});

		element.on('$destroy', function () {
			modeler.destroy();
		});

		// make container resizable
		element.resizable({
			handles: 's',
			stop: function () {
				$('body').css('cursor', '');
			}
		});
	},
	template_readonly: null,
	template_editable: null,
	template:
		"<div>" +
			"<div class='bpmn-canvas'></div>" +
		"</div>"
});

})(this);