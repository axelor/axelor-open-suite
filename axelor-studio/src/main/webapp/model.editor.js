/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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

	studio.controller('ModelController', ['$scope', '$builder', '_', '$routeParams', function($scope, $builder,  _, $routeParams) {
		var modelId = $routeParams.modelId;
		var postObj = {};
		var META_MODEL = "com.axelor.meta.db.MetaModel";
		var META_FIELD = "com.axelor.meta.db.MetaField";
		var fields = [];
		$builder.forms['default'] = [];
		$scope.form = $builder.forms['default'];
		$scope.input = [];
		$scope.defaultValue = {};
		$scope.models = [];

		var relationship = {
			"many-to-one" : "ManyToOne",
			"one-to-many" : "OneToMany",
			"many-to-many" : "ManyToMany"
		};
		
		var customiseIds = [];
		
		var typeNameMap = {
				"string":"String",
				"textArea":"String",
				"integer":"Integer",
				"boolean":"Boolean",
				"decimal":"BigDecimal",
				"long":"Long",
				"binary":"byte[]",
				"date":"LocalDate",
				"datetime":"DateTime"
		}
		
		$scope.$http.post($scope.absUrl + "ws/rest/" + META_MODEL +"/search", {fields:["name"], data:{}}).success(function(response){
			
			_.each(response.data, function(model){
				$scope.models.push(model.name);
			});
			
			$scope.$http.get($scope.absUrl + "ws/rest/" + META_MODEL + "/" + modelId, postObj).success(function(response){
				$scope.record = response.data[0];
				var fieldIds = _.pluck($scope.record["metaFields"], "id");
				if(fieldIds.length > 0){
					postObj = {
						"offset": 0,
						"sortBy": ["sequence"],
						"data": {
							"_domain": "self.id in (" + fieldIds + ") AND self.customised = true",
							"_archived": true
						}
					}
				}
    	    	
				$scope.$http.post($scope.absUrl + "ws/rest/" + META_FIELD + "/search", postObj).success(function(response){
					if(response.status == 0){
						_.each(response.data, addField);
					}
					else{
						console.log(response.data);
					}
    	    	});
        	
			});
    	
		});
      
      
		function addField(field){
			
			customiseIds.push(field.id);
			
			var fieldData = {
			    id: field.id,
			    component: field.fieldType,
			    label: field.label,
			    fieldName: field.name,
			    readonly: field.readonly,
			    hidden: field.hidden,
			    track: field.track,
			    required: field.required,
			    version: field.version,
			    options:[]
			};
			
			switch(field.fieldType){
	    		case 'decimal':
		    		fieldData["defaultDecimal"] = Number(field.defaultDecimal);
		    		fieldData["min"] = Number(field.decimalMin);
		    		fieldData["max"] = Number(field.decimalMax);
		    		break;
	    		case 'integer': 
	    			fieldData["min"] = Number(field.integerMin);
	    			fieldData["max"] = Number(field.integerMax);
	    			fieldData["defaultInteger"] = Number(field.defaultInteger);
	    			break;
	    		case 'string':
	    			fieldData["min"] = Number(field.integerMin);
	    			fieldData["max"] = Number(field.integerMax);
	    			fieldData["defaultString"] = field.defaultString;
	    			fieldData["nameColumn"] = field.nameColumn;
	    			if(field.large){
	    				fieldData.component= "textArea";
	    			}
	    			break;
	    		case 'boolean':
	    			fieldData["defaultBoolean"] = field.defaultBoolean;
	    			break;
			}	
    	
			if(['many-to-one','many-to-many','one-to-many'].indexOf(field.fieldType) > -1){
				fieldData["options"] = $scope.models;
				if(field.typeName){
					fieldData["refModel"] = field.typeName;
					fieldData["mappedBy"] = field.mappedBy;
				};
			};
			
			if(field.metaSelect){
				fieldData["metaSelect"] = field.metaSelect;
			}
    	
			var textbox = $builder.addFormObject('default', fieldData);
			$scope.defaultValue[textbox.id] = field.defaultString;
		};
		
      
		function updateRecord(field){

			var data = {
				"sequence": field.index,
				"label": field.label,
				"name": field.fieldName,
				"fieldType": field.component,
				"readonly": field.readonly,
				"hidden": field.hidden,
				"required": field.required,
				"track": field.track,
				"customised":true,
				"relationship": relationship[field.component],
				"typeName":typeNameMap[field.component]
			};
			
			switch(field.component){
				case 'decimal':
					data["defaultDecimal"] = field.defaultDecimal;
					data["decimalMin"] = field.min;
					data["decimalMax"] = field.max;
			    	break;
				case 'integer': 
					data["integerMin"] = field.min;
					data["integerMax"] = field.max;
					data["defaultInteger"] = field.defaultInteger;
					break;
				case 'string':
					data["integerMin"] = field.min;
					data["integerMax"] = field.max;
					data["defaultString"] = field.defaultString;
					data["nameColumn"] = field.nameColumn;
					break;
				case 'boolean':
					data["defaultBoolean"] = field.defaultBoolean;
					break;
				case 'textArea':
					data["fieldType"] = "string";
					data["large"] = true;
					break;
			}	
    	  
			if(field.id){
				data["id"] = field.id;
				data["version"] = field.version; 
			}else{
				data["metaModel"] = {"id":modelId};
			}
			
			data["metaSelect"] =  field.metaSelect
    	
			if(field.refModel){
				data["typeName"] = field.refModel;
				data["mappedBy"] = field.mappedBy;
			}
    	
			$scope.record["metaFields"].push(data);
		};
		
		$scope.updateMappedByFields = function(scope){
			
			scope.mappedByFields = [""];
			
			postObj = {
					"offset": 0,
					"sortBy": ["sequence"],
					"fields": ["name"],
					"data": {
						"_domain": "self.relationship = 'ManyToOne' and self.metaModel.name = '" 
									+ scope.refModel + "' and " + "self.typeName = '" + $scope.record.name + "'",
						"_archived": true
					}
			}
			 
			return $scope.$http.post($scope.absUrl + "ws/rest/" + META_FIELD + "/search", postObj).success(function(response){
				if(response.status == 0){
					_.each(response.data, function(field){
						scope.mappedByFields.push(field.name);
					});
					return scope.mappedByFields;
					
				}
				else{
					console.log(response.data);
				}
	    	});
			
		};
		
		return $scope.submit = function() {
			$scope.record = {
					"id":$scope.record.id,
					"edited":true,
					"customised":true,
        			"version":$scope.record.version,
        			"metaFields": _.filter($scope.record["metaFields"], function(field){return !_.contains(customiseIds, field.id);})
        	};
			$(".popover").hide();
        	_.each($scope.form, updateRecord);
        	 
        	$scope.$http.post($scope.absUrl + "ws/rest/" + META_MODEL, {"data":$scope.record}).success(function(response){
        		if(response.status == '0'){
        			$scope.$route.reload();
        		}else{
        			return alert(response.data.message);
        		}
          	 }).error(function() {
                 return alert("Error in save");
          	 });
		};
	}]);

}).call(this);
