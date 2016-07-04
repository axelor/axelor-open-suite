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
	angular.module('builder.components', ['builder']).config([
    '$builderProvider', function($builderProvider) {
    
	//Fields
	$builderProvider.registerComponent('string', {
		group: 'Basic',
		label: 'String',
//		validation: "['!name','!title']",
//		validationOptions: [{'rule':'!name','label':'Name is required'},{'rule':'!title','label':'Title is required'}],
		template: "<div class=\"form-group\"> " +
				  "<span for=\"{{formName+index}}\" class=\"col-sm-4 fa \" ng-class=\"{'fb-required':required}\">{{label}}</span> <div class=\"col-sm-8\"> " +
				  "<input type=\"text\" class=\"form-control\"/>" +
				  "</div>",
		popoverTemplateUrl: 'partials/PopoverTemplate.html'
	});
    	
	$builderProvider.registerComponent('textArea', {
		group: 'Basic',
		label: 'Text',
		template: "<div class=\"form-group\">" +
				  "<span for=\"{{formName+index}}\" class=\"col-sm-4 fa\" ng-class=\"{'fb-required':required}\">{{label}}</span> <div class=\"col-sm-8\"> " +
				  "<textarea type=\"text\" class=\"form-control\" rows=\"3\"/>"+
				  "</div>",
		popoverTemplateUrl: 'partials/PopoverTemplate.html'
	});
	
	$builderProvider.registerComponent('integer', {
		group: 'Basic',
		label: 'Integer',
		template: "<div class=\"form-group\">" +
		          "<span for=\"{{formName+index}}\" class=\"col-sm-4 fa\" ng-class=\"{'fb-required':required}\">{{label}}</span> <div class=\"col-sm-8\"> " +
				  "<span class=\"ui-spinner ui-widget ui-widget-content ui-corner-all\">" +
				  "<input type=\"text\" class=\"ng-scope studio-spinner-input\" \>" +
				  "<a class=\"ui-spinner-button ui-spinner-up ui-corner-tr\" tabindex=\"-1\"><span class=\"ui-icon ui-icon-triangle-1-n\"></span></a>" + 
				  "<a class=\"ui-spinner-button ui-spinner-down ui-corner-br\" tabindex=\"-1\"><span class=\"ui-icon ui-icon-triangle-1-s\"></span></a>" +
				  "</span>" +  
				  "</div>",
		popoverTemplateUrl: 'partials/PopoverTemplate.html'
	});
	
	$builderProvider.registerComponent('decimal', {
		group: 'Basic',
		label: 'Decimal',
		template: "<div class=\"form-group\">" +
		          "<span for=\"{{formName+index}}\" class=\"col-sm-4 fa\" ng-class=\"{'fb-required':required}\">{{label}}</span> <div class=\"col-sm-8\"> " +
				  "<span class=\"ui-spinner ui-widget ui-widget-content ui-corner-all\">" +
		    	  "<input type=\"text\" class=\"ng-scope studio-spinner-input\" \>" +
		    	  "<a class=\"ui-spinner-button ui-spinner-up ui-corner-tr\" tabindex=\"-1\"><span class=\"ui-icon ui-icon-triangle-1-n\"></span></a>" + 
		    	  "<a class=\"ui-spinner-button ui-spinner-down ui-corner-br\" tabindex=\"-1\"><span class=\"ui-icon ui-icon-triangle-1-s\"></span></a>" +
				  "</span>" +  
				  "</div>",
		popoverTemplateUrl: 'partials/PopoverTemplate.html'
	});
	
	$builderProvider.registerComponent('boolean', {
		group: 'Basic',
		label: 'Boolean',
		template: "<div class=\"form-group\">" +
		          "<span for=\"{{formName+index}}\" class=\"col-sm-4 fa\" ng-class=\"{'fb-required':required}\">{{label}}</span> <div class=\"col-sm-8\"> " +
				  "<input type=\"checkbox\" />" +
				  "</div>",
		popoverTemplateUrl: 'partials/PopoverTemplate.html'
	});
	
	$builderProvider.registerComponent('many-to-one', {
		group: 'Relational',
		label: 'Many To One',
		template: "<div class=\"form-group\">" +
		          "<span for=\"{{formName+index}}\" class=\"col-sm-4 fa\" ng-class=\"{'fb-required':required}\">{{label}}</span> <div class=\"col-sm-8\"> " +
				  "<div><i class=\"fa fa-search\"></i><input type=\"text\" class=\"form-control relational-green\"></div>" +
				  "</div>",
		popoverTemplateUrl: 'partials/PopoverTemplate.html'
	});
	
	$builderProvider.registerComponent('one-to-many', {
		group: 'Relational',
		label: 'One To Many',
		template: "<div class=\"form-group\">" +
		          "<span for=\"{{formName+index}}\" class=\"col-sm-4 fa\" ng-class=\"{'fb-required':required}\">{{label}}</span> <div class=\"col-sm-8\"> " +
				  "<div>" +
				  "<i class=\"fa fa-plus\"></i>" + 
			  	  "<table class=\"table\"><thead><tr><th>Field1</th><th>Field2</th></tr></thead><tr><td></td><td></td></tr><tr><td></td><td></td></tr></table>" + 
				  "</div>" +
				  "</div>",
		popoverTemplateUrl: 'partials/PopoverTemplate.html'
	});
	
	$builderProvider.registerComponent('many-to-many', {
		group: 'Relational',
		label: 'Many To Many',
		template: "<div class=\"form-group\">" +
		          "<span for=\"{{formName+index}}\" class=\"col-sm-4 fa\" ng-class=\"{'fb-required':required}\">{{label}}</span> <div class=\"col-sm-8\"> " +
				  "<div>" +
				  "<i class=\"fa fa-search\"></i>" + 
			  	  "<table class=\"table\"><thead><tr><th>Field1</th><th>Field2</th></tr></thead><tr><td></td><td></td></tr><tr><td></td><td></td></tr></table>" + 
				  "</div>" +
				  "</div>",
		popoverTemplateUrl: 'partials/PopoverTemplate.html'
	});
	
	$builderProvider.registerComponent('date', {
		group: 'Date',
		label: 'Date',
		template: "<div class=\"form-group\">" +
		          "<span for=\"{{formName+index}}\" class=\"col-sm-4 fa\" ng-class=\"{'fb-required':required}\">{{label}}</span> <div class=\"col-sm-8\"> " +
				  "<input type=\"date\" class=\"form-control date-blue\" />" +
				  "</div>",
		popoverTemplateUrl: 'partials/PopoverTemplate.html'
	});
	
	$builderProvider.registerComponent('datetime', {
		group: 'Date',
		label: 'Datetime',
		template: "<div class=\"form-group\">" +
		          "<span for=\"{{formName+index}}\" class=\"col-sm-4 fa\" ng-class=\"{'fb-required':required}\">{{label}}</span> <div class=\"col-sm-8\"> " +
				  "<input type=\"datetime-local\" class=\"form-control date-blue\" />" +
				  "</div>",
		popoverTemplateUrl: 'partials/PopoverTemplate.html'
	});
   
	
	//View components
	$builderProvider.registerComponent('toolbar', {
		group: 'Panel',
		label: 'Toolbar',
		template: "<i class=\"fa fa-wrench\" ng-model=\"toolbar\"> <b>Toolbar</b></i>",
		popoverTemplateUrl: 'partials/PopoverTemplate.html'
	});
	
	$builderProvider.registerComponent('onSave', {
		group: 'Panel',
		label: 'On Save',
		template: "<i class=\"fa fa-check\" ng-model=\"onSave\">  <b>On Save</b></i>",
		popoverTemplateUrl: 'partials/PopoverTemplate.html'
	});
	
	$builderProvider.registerComponent('panel', {
		group: 'Panel',
		label: 'Panel',
		template: "<i class=\"fa fa-columns\" ng-model=\"panel\"> <b>{{label ? label : 'Panel'}}</b></i>",
		popoverTemplateUrl: 'partials/PopoverTemplate.html'
	});
	
	$builderProvider.registerComponent('button', {
		group: 'Panel',
		label: 'Button',
		template: "<i class=\"fa fa-caret-square-o-right\" ng-model=\"button\"> {{label ? label : 'Button'}}</i>",
		popoverTemplateUrl: 'partials/PopoverTemplate.html'
	});
	
	$builderProvider.registerComponent('panelSide', {
		group: 'Panel',
		label: 'Side Panel',
		template: "<i class=\"fa fa-sign-in\" ng-model=\"panelSide\"> <b>{{label ? label : 'Side panel'}}</b></i>",
		popoverTemplateUrl: 'partials/PopoverTemplate.html'
	});
	
	$builderProvider.registerComponent('notebook', {
		group: 'Panel',
		label: 'Notebook',
		template: "<i class=\"fa fa-files-o\" ng-model=\"notebook\"> <b>Notebook</b></i>",
		popoverTemplateUrl: 'partials/PopoverTemplate.html'
	});
	
	$builderProvider.registerComponent('panelTab', {
		group: 'Panel',
		label: 'Tab',
		template: "<i class=\"fa fa-folder-o\" ng-model=\"panelTab\"> <b>{{label ? label : 'Tab'}}</b></i>",
		popoverTemplateUrl: 'partials/PopoverTemplate.html'
	});
	
     
    }
  ]);
  
  
}).call(this);
