/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2015 Axelor (<http://axelor.com>).
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
(function() {
	
	var studio = angular.module('axelor.studio');
	
    studio.controller('ViewController', ['$scope', '$builder', '_', '$routeParams', function($scope, $builder, _, $routeParams) {
    	
    	var viewId = $routeParams.viewId,
    		
    		META_FIELD = 'com.axelor.meta.db.MetaField',
		    VIEW_BUILDER = 'com.axelor.studio.db.ViewBuilder',
		    VIEW_ITEM = 'com.axelor.studio.db.ViewItem',
		    VIEW_PANEL = 'com.axelor.studio.db.ViewPanel',
		    
		    fields = [],
		    relationalTypes = ['many-to-one','many-to-many','one-to-many'],
		    widgetValues = ['normal','RadioSelect','NavSelect','multi-select'],
		    
    	    typeNameMap = {
				'String':'string',
				'Integer':'integer',
				'Boolean':'boolean',
				'BigDecimal':'decimal',
				'Long':'long',
				'byte[]':'binary',
				'LocalDate':'date',
				'LocalDateTime':'datetime',
				'DateTime':'datetime'
		    },
		    
	    	defaultMap = {
    			'integer':'defaultInteger', 
				'boolean':'defaultBoolean', 
				'decimal':'defaultDecimal', 
				'string':'defaultString',
				'many-to-one':'defaultString',
				'date': 'defaultString',
				'datetime': 'defaultString'
			};
		
    	$builder.forms['viewForm'] = [];
		$builder.forms['fieldForm'] = [];
		$scope.form = $builder.forms['viewForm'];
		$scope.input = [];
		$scope.defaultValue = {};
		$scope.isView = true;
		
		function addField(field){
			var fieldData = {
			    component: field.fieldType,
			    label: field.label,
			    fieldName: field.name,
			    readonly:field.readonly,
			    hidden:field.hidden,
			    required:field.required,
			    metaSelect:field.metaSelect,
			    selection:field.metaSelect != null,
			    refModel: field.typeName,
			    options:[],
			    metaFieldId: field.id,
			    editable: false
			};
			
			var fieldType = field.fieldType;
			
			switch(fieldType){
	    		case 'decimal':
		    		fieldData['defaultDecimal'] = Number(field.defaultDecimal);
		    		fieldData['min'] = Number(field.decimalMin);
		    		fieldData['max'] = Number(field.decimalMax);
		    		break;
	    		case 'integer': 
	    			fieldData['min'] = Number(field.integerMin);
	    			fieldData['max'] = Number(field.integerMax);
	    			fieldData['defaultInteger'] = Number(field.defaultInteger);
	    			break;
	    		case 'string':
	    			fieldData['min'] = Number(field.integerMin);
	    			fieldData['max'] = Number(field.integerMax);
	    			fieldData['defaultString'] = field.defaultString;
	    			if(field.large){
	    				fieldData.component= 'textArea';
	    			}
	    			break;
	    		case 'boolean':
	    			fieldData['defaultBoolean'] = field.defaultBoolean;
	    			break;
	    		default:
	    			if(_.contains(relationalTypes,fieldType)){
	    				if(field.metaModel){
	    					fieldData['metaModel'] = field.metaModel.name;
	    				};
	    			};
	    			break;
			}
			
			if (fieldData.component == null) {
				fieldData.component = typeNameMap[field.typeName]; 
				if (fieldData.component == null) {
					fieldData.component = "string";
				}
			}
			
			var textbox = $builder.addFormObject('fieldForm', fieldData);  
			
			$scope.defaultValue[textbox.id] = field.defaultString;
		};
		
		function addItem(item){
			
			var viewItemData = {
				id: item.id,
			    component: item.large ? 'textArea' : item.fieldType,
			    label: item.title ? item.title : item.label ,
			    widget: item.widget,
			    metaSelect: item.metaSelect,
			    selection: item.selection,
			    fieldName: item.name,
			    version:item.version,
			    viewPanel: item.viewPanel ? item.viewPanel.id : null,
			    required: item.required,
			    requiredIf: item.requiredIf,
			    hidden: item.hidden,
			    hideIf: item.hideIf,
			    readonly: item.readonly,
			    readonlyIf: item.readonlyIf,
			    domainCondition: item.domainCondition,
			    onChange: item.onChange,
			    isView: true,
			    htmlWidget: item.htmlWidget,
			    progressBar: item.progressBar,
			    options: [],
			    icon: item.icon,
			    promptMsg: item.promptMsg,
			    onClick: item.onClick,
			    refModel: item.refModel,
			    panelTop: item.panelTop,
			    metaFieldId: item.metaFieldId,
			    hideTitle: item.hideTitle
			};
			
			if(item.typeSelect === 1){
				viewItemData['component'] = 'button'; 
			}
			
			if(item.defaultValue){
				switch(viewItemData.component){
					case 'integer':
						viewItemData['defaultInteger'] = parseInt(item.defaultValue);
						break;
					case 'decimal':
						viewItemData['defaultDecimal'] = parseFloat(item.defaultValue);
						break;
					case 'boolean':
						viewItemData['defaultBoolean']  =  item.defaultValue == 'true';
						break;
					default:
						viewItemData['defaultString'] = item.defaultValue;
				};
			}
			
			if (!viewItemData.component) {
				viewItemData["component"] = "string";
			}
			
			$builder.addFormObject('viewForm', viewItemData);
		
		};
		
		function initializeView(){
			
			var onSave = $scope.record.onSave;
			
			if(onSave){
				$builder.addFormObject('viewForm', {
				    id: 'onSave',
				    component: 'onSave', 
				    onSave: onSave,
				    isView:true
				});
			}
			
			$scope.metaFields = _.reject($scope.metaFields, 
				function(metaField){
					var viewField = _.find($scope.viewItems, 
						function(viewItem){
							return viewItem.typeSelect == 0 && viewItem.name == metaField.name;
					});
					
					if(viewField){
						viewField.large = metaField.large;
						viewField.label = metaField.label;
						viewField.selection = metaField.metaSelect != null;
						viewField.refModel = metaField.typeName;
						viewField.metaFieldId = metaField.id;
						return true;
					}
					return false;
			});
			
			_.each($scope.metaFields, addField);
			
			if($scope.toolbarItems){
				$builder.addFormObject('viewForm', {
				    id: 'toolbar',
				    component: 'toolbar',
				    label: 'Toolbar',
				    isView:true,
				    options:[]
				});
				_.each($scope.toolbarItems, addItem);
			}
			
			_.each($scope.viewPanels, function(panel){
				
				var component = 'panel';
				
				if(panel.isNotebook) { 
					component = 'notebook';
				}
				else if(panel.isPanelTab) {
					component = 'panelTab';
				}
				else if(panel.viewBuilderSideBar){
					component = 'panelSide';
				}
				$builder.addFormObject('viewForm', {
				    id: panel.id,
				    component: component,
				    label: panel.title,
				    fieldName: panel.name,
				    panelLevel: panel.panelLevel,
				    version:panel.version,
				    place:panel.place.toString(),
				    isView:true,
				    options:[]});
				
				var panelItems = _.filter($scope.viewItems, function(viewItem){return viewItem.viewPanel.id == panel.id;});
				
				_.each(panelItems, addItem);
			});
		};
		
		var getViewItems = function(viewPanelIds){
			$scope.$http.post($scope.absUrl + 'ws/rest/' + VIEW_ITEM + '/search',{
				'offset': 0,
				'sortBy': ['sequence'],
				'data': {
					'_domain': 'self.viewPanel in (' + viewPanelIds + ')',
					'_archived': true
				}
			}).success(function(response){
				if(response.status == 0){
					$scope.viewItems = response.data;
					initializeView();
				}
				else{
					console.log(response.data);
				}
			});
		};
		
		var getViewPanels = function(){
			
			var viewPanelIds = _.pluck($scope.record.viewPanelList, 'id');
			viewPanelIds = viewPanelIds.concat(_.pluck($scope.record.viewSidePanelList, 'id'));
			if(viewPanelIds && viewPanelIds.length > 0){
				$scope.$http.post($scope.absUrl + 'ws/rest/' + VIEW_PANEL + '/search',{
					'offset': 0,
					'sortBy': ['panelLevel'],
					'data': {
						'_domain': 'self.id in (' + viewPanelIds + ')',
						'_archived': true
					}
				}).success(function(response){
					if(response.status == 0){
						$scope.viewPanels = response.data;
						getViewItems(viewPanelIds);
					}
					else{
						console.log(response.data);
					}
				});
			}
			else{
				initializeView();
			}
		};
		
		var getToolbar = function(){
			
			$scope.$http.post($scope.absUrl + 'ws/rest/' + VIEW_ITEM + '/search',{
				'offset': 0,
				'sortBy': ['sequence'],
				'data': {
					'_domain': 'self.viewBuilderToolbar.id = ' + $scope.record.id,
					'_archived': true
				}
			}).success(function(response){
				if(response.status == 0){
					$scope.toolbarItems = response.data;
					getViewPanels();
				}
				else{
					console.log(response.data);
				}
			});
			
		};
		
		$scope.loadActions = function(action, process){
			
			$scope.$http.post($scope.absUrl + 'ws/action/',{
				action : "com.axelor.studio.web.ViewBuilderController:getActions",
				data : {
					context : {
						'action' : action,
						'_model' : VIEW_BUILDER
					}
				},
				model : VIEW_BUILDER
			}).success(function(response){
				var actions = []
				if(response.status == 0){
					actions = response.data[0].values.actions;
				}
				else{
					console.log(response.data);
				}
				process(actions);
			});
			
		};
		
		$scope.$http.get($scope.absUrl + 'ws/rest/' + VIEW_BUILDER + '/' + viewId, {}).success(function(response){
			
			if(response.status != 0 || response.data == null){
				$scope.$location.path('/');
				return;
			}
			
			$scope.record = response.data[0];
			
			if($scope.record && $scope.record.metaModel){
				var modelId = $scope.record.metaModel.id
				var postObj = {
					'offset': 0,
					'sortBy': ['sequence'],
					'data': {
						'_domain': "self.name != 'id'  and self.metaModel.id = " + modelId,
						'_archived': true
					}
				}
				$scope.$http.post($scope.absUrl + 'ws/rest/' + META_FIELD + '/search', postObj).success(function(response){
					if(response.status == 0){
						$scope.metaFields = response.data;
						getToolbar();
					}
					else{
						console.log(response.data);
					}
				});
			}
		});
		
		return $scope.submit = function() {
			$('.popover').hide();
			$scope.record = {
					'id':$scope.record.id,
        			'version':$scope.record.version,
        			'edited': true,
        			'recorded': false,
        			'onSave':null,
        			'viewPanelList': [],
        			'viewSidePanelList': []
        	};
			
			var getPanelLevel = function(parentLevel){
					if(parentLevel && parentLevel.length == 1){
						return (parseInt(parentLevel)+1).toString();
					}
					var levels = parentLevel.split('.');
					levels[levels.length-1] = (parseInt(levels[levels.length-1])+1).toString();
					return levels.join('.');
			};
			
			var currentPanel, notebook, toolbar;
			
			_.each($scope.form, function(formObject){
				var data = {
					'name': formObject.fieldName,
					'title': formObject.label,	
					'index': formObject.index,
					'viewItemList': []
				};
				
				if (formObject.id) {
					data['id'] = formObject.id;
				}
				else if(formObject.refModel == 'MetaFile' && !formObject.widget) {
					formObject.widget = 'binary-link';
				}

				if(formObject.version != null){
					data['version'] = formObject.version;
				}
				
				switch(formObject.component){
					case 'panel':
						toolbar = false;
						data['panelLevel'] = '0';
						if(notebook){
							data['panelLevel'] = getPanelLevel(notebook.panelLevel);
						}
						else if(currentPanel && currentPanel.panelLevel){
							data['panelLevel'] = getPanelLevel(currentPanel.panelLevel);
						}
						data['place'] = formObject.place;
						notebook = null;
						currentPanel = data;
						$scope.record.viewPanelList.push(data);
						break;
					case 'panelSide':
						toolbar = false;
						data['panelLevel'] = '0';
						if(notebook){
							data['panelLevel'] = getPanelLevel(notebook.panelLevel);
						}
						else if(currentPanel && currentPanel.panelLevel){
							data['panelLevel'] = getPanelLevel(currentPanel.panelLevel);
						}
						data['place'] = formObject.place;
						notebook = null;
						currentPanel = data;
						$scope.record.viewSidePanelList.push(data);
						break;
					case 'toolbar':
						toolbar = true;
						if(!$scope.record.toolbar){
							$scope.record.toolbar = [];
						}
						break;
					case 'panelTab':
						toolbar = false;
						if(notebook){
							if(currentPanel && currentPanel.panelLevel){
								data['panelLevel'] = getPanelLevel(currentPanel.panelLevel);
							}
							else{
								data['panelLevel'] = notebook.panelLevel+'.0';
							}
						}
						data['isPanelTab'] = true;
						data['place'] = formObject.place;
						currentPanel = data;
						$scope.record.viewPanelList.push(data);
						break;
					case 'notebook':
						toolbar = false;
						panelTab = null;
						data['panelLevel'] = 0;
						data['title'] = null;
						if (currentPanel){
							data['panelLevel'] = getPanelLevel(currentPanel.panelLevel.split('.')[0]);
						}
						data['isNotebook'] = true;
						notebook = data;
						currentPanel = null;
						data['place'] = formObject.place;
						$scope.record.viewPanelList.push(data);
						break;
					case 'onSave':
						$scope.record.onSave = formObject.onSave ? formObject.onSave.replace(/(^\s*,)|(,\s*$)/g, '') : null;
						break;
					default:
						if(currentPanel || toolbar){
							data['fieldType'] = formObject.component == 'textArea' ? 'string' : formObject.component,
							data['readonly'] = formObject.readonly;
							data['hidden'] = formObject.hidden;
							data['required'] = formObject.required;
							data['readonlyIf'] = formObject.readonlyIf;
							data['hideIf'] = formObject.hideIf;
							data['requiredIf'] = formObject.requiredIf;
							data['domainCondition'] = formObject.domainCondition;
							data['onChange'] = formObject.onChange ? formObject.onChange.replace(/(^\s*,)|(,\s*$)/g, '') : null;
							data['progressBar'] = formObject.progressBar,
							data['htmlWidget'] = formObject.htmlWidget,
							data['widget'] = formObject.widget,
							data['icon'] =  formObject.icon,
							data['promptMsg'] = formObject.promptMsg,
							data['onClick'] = formObject.onClick ? formObject.onClick.replace(/(^\s*,)|(,\s*$)/g, '') : null;
							data['defaultValue'] = formObject[defaultMap[formObject.component]] ? formObject[defaultMap[formObject.component]].toString() : null;
							data['panelTop'] = formObject.panelTop;
							data['hideTitle'] = formObject.hideTitle;
							
							if(formObject.component == 'button'){
								data['fieldType'] = null;
								data['typeSelect'] = 1;
							}
							
							data['metaSelect']  = null;
							if(formObject.metaSelect){
								data['metaSelect'] = { id : formObject.metaSelect.id }
							}
							
							data["metaField"] = null;
							if (formObject.metaFieldId) {
								data["metaField"] = { id : formObject.metaFieldId }
							}
							
							if(toolbar){
								$scope.record.toolbar.push(data);
							}
							else{
								if(formObject.viewPanel != currentPanel.id){
									delete data['id'];
									delete data['version'];
								}
								data['sequence'] = formObject.index-currentPanel.index;
								currentPanel['viewItemList'].push(data);
							}
							
						}
						break;
				}
				
			});
			
			$scope.$http.post($scope.absUrl + 'ws/rest/' + VIEW_BUILDER, {'data':$scope.record}).success(function(response){
        		if(response.status == '0'){
        			$scope.$route.reload();
        		}else{
        			return alert(response.data.message);
        		}
          	 }).error(function() {
                 return alert('Error in save');
          	 });
		};
		
	}]);
	

}).call(this);
