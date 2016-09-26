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
		template: "<div class=\"line\"> " +
				  "<div class=\"left\"><i class=\"fa fa-font fa-2x\" aria-hidden=\"true\"></i></div>" + 
				  "<div class=\"right\">" +
				  "<label for=\"{{formName+index}}\" ng-class=\"{'fb-required':required}\">{{label}}</label>" +
				  "<input type=\"text\" id=\"string\"/>" +
				  "</div>" +
				  "</div>",
		popoverTemplateUrl: 'partials/PopoverTemplate.html'
	});
    	
	$builderProvider.registerComponent('textArea', {
		group: 'Basic',
		label: 'Text',
		template: "<div class=\"line\"> " +
				  "<div class=\"left\"><i class=\"fa fa-align-left fa-2x\" aria-hidden=\"true\"></i></div>" + 
				  "<div class=\"right\">" +
				  "<label for=\"{{formName+index}}\" ng-class=\"{'fb-required':required}\">{{label}}</label>" +
				  "<input type=\"text\" id=\"text\"/>" +
				  "</div>" +
				  "</div>",
		popoverTemplateUrl: 'partials/PopoverTemplate.html'
	});
	
	$builderProvider.registerComponent('integer', {
		group: 'Basic',
		label: 'Integer',
		template: "<div class=\"line\"> " +
				  "<div class=\"left nb\"><p class=\"nb-font\">0</p></div>" + 
				  "<div class=\"right nb\">" +
				  "<label for=\"{{formName+index}}\" ng-class=\"{'fb-required':required}\">{{label}}</label>" +
				  "<input type=\"number\" id=\"integer\"/>" +
				  "</div>" +
				  "</div>",
		popoverTemplateUrl: 'partials/PopoverTemplate.html'
	});
	
	$builderProvider.registerComponent('decimal', {
		group: 'Basic',
		label: 'Decimal',
		template: "<div class=\"line\"> " +
				  "<div class=\"left nb\"><p class=\"nb-font\">0,</p></div>" + 
				  "<div class=\"right nb\">" +
				  "<label for=\"{{formName+index}}\" ng-class=\"{'fb-required':required}\">{{label}}</label>" +
				  "<input type=\"number\" id=\"integer\"/>" +
				  "</div>" +
				  "</div>",
		popoverTemplateUrl: 'partials/PopoverTemplate.html'
	});
	
	$builderProvider.registerComponent('boolean', {
		group: 'Basic',
		label: 'Boolean',
		template: "<div class=\"line\"> " +
				  "<div class=\"left\"><i class=\"fa fa-check fa-2x\" aria-hidden=\"true\"></i></div>" + 
				  "<div class=\"right\">" +
				  "<i class=\"fa fa-check-square-o fa-2x bool\" aria-hidden=\"true\"></i>" +
				  "<label for=\"{{formName+index}}\" class=\"bool\">{{label}}</label>" +
				  "<input type=\"hidden\" id=\"boolean\"/>" +
				  "</div>" +
				  "</div>",
		popoverTemplateUrl: 'partials/PopoverTemplate.html'
	});
	
	$builderProvider.registerComponent('many-to-one', {
		group: 'Relational',
		label: 'Many To One',
		template: "<div class=\"line\"> " +
				  "<div class=\"left\"><i class=\"fa fa-search fa-2x\" aria-hidden=\"true\"></i></div>" + 
				  "<div class=\"right\">" +
				  "<label for=\"{{formName+index}}\" ng-class=\"{'fb-required':required}\">{{label}}" +
				  "<i class=\"fa fa-search\" aria-hidden=\"true\"> </i>" +
				  "</label>" +
				  "<input type=\"text\" id=\"many-to-one\"/>" +
				  "</div>" +
				  "</div>",

		popoverTemplateUrl: 'partials/PopoverTemplate.html'
	});
	
	$builderProvider.registerComponent('one-to-many', {
		group: 'Relational',
		label: 'One To Many',
		template: "<div class=\"line one-table\"> " +
				  "<div class=\"left\"><i class=\"fa fa-table fa-2x\" aria-hidden=\"true\"></i></div>" + 
				  "<div class=\"right\">" +
				  "<table><tr><th>{{label}} <i class=\"fa fa-plus\" aria-hidden=\"true\"> </i></th></tr><tr><td></td></tr></table>" + 
				  "</div>" +
				  "</div>",
		popoverTemplateUrl: 'partials/PopoverTemplate.html'
	});
	
	$builderProvider.registerComponent('many-to-many', {
		group: 'Relational',
		label: 'Many To Many',
		template: "<div class=\"line one-table\"> " +
				  "<div class=\"left\"><i class=\"fa fa-table fa-2x\" aria-hidden=\"true\"></i></div>" + 
				  "<div class=\"right\">" +
				  "<table><tr><th>{{label}} <i class=\"fa fa-search fa-s\" aria-hidden=\"true\"> </i></th></tr><tr><td></td></tr></table>" + 
				  "</div>" +
				  "</div>",
		popoverTemplateUrl: 'partials/PopoverTemplate.html'
	});
	
	$builderProvider.registerComponent('date', {
		group: 'Date',
		label: 'Date',
		template: "<div class=\"line\"> " +
				  "<div class=\"left\"><i class=\"fa fa-calendar fa-2x\" aria-hidden=\"true\"></i></div>" + 
				  "<div class=\"right\">" +
				  "<label for=\"{{formName+index}}\" ng-class=\"{'fb-required':required}\">{{label}}" +
				  "<i class=\"fa fa-calendar-o\" aria-hidden=\"true\"></i>" + 
				  "</label>" +
				  "<input type=\"text\" id=\"date\"/>" +
				  "</div>" +
				  "</div>",
		popoverTemplateUrl: 'partials/PopoverTemplate.html'
	});
	
	$builderProvider.registerComponent('datetime', {
		group: 'Date',
		label: 'Datetime',
		template: "<div class=\"line\"> " +
				  "<div class=\"left\"><i class=\"fa fa-calendar fa-2x\" aria-hidden=\"true\"></i></div>" + 
				  "<div class=\"right\">" +
				  "<label for=\"{{formName+index}}\" ng-class=\"{'fb-required':required}\">{{label}}" +
				  "<i class=\"fa fa-calendar-o\" aria-hidden=\"true\"></i>" + 
				  "</label>" +
				  "<input type=\"text\" id=\"datetime\"/>" +
				  "</div>" +
				  "</div>",
		popoverTemplateUrl: 'partials/PopoverTemplate.html'
	});
   
	
	//View components
	
	$builderProvider.registerComponent('button', {
		group: 'Panel',
		label: 'Button',
		isView: true,
		template: "<button ng-model=\"button\" class=\"btn-view\"><i class=\"fa fa-play\" aria-hidden=\"true\"></i> {{label ? label : 'Button'}}</button>",
		popoverTemplateUrl: 'partials/PopoverTemplate.html'
	});
	
	$builderProvider.registerComponent('onSave', {
		group: 'Panel',
		label: 'On Save',
		isView: true,
		template: "<button ng-model=\"onSave\" class=\"btn-view\"><i class=\"fa fa-check\" aria-hidden=\"true\"></i> On Save</button>",
		popoverTemplateUrl: 'partials/PopoverTemplate.html'
	});
	
	$builderProvider.registerComponent('toolbar', {
		group: 'Panel',
		label: 'Toolbar',
		isView: true,
		template: "<div class=\"view-component\"><i class=\"fa fa-wrench\" ng-model=\"toolbar\"></i> Toolbar</div>",
		popoverTemplateUrl: 'partials/PopoverTemplate.html'
	});
	
	$builderProvider.registerComponent('panel', {
		group: 'Panel',
		label: 'Panel',
		isView: true,
		template: "<div class=\"view-component\"><i class=\"fa fa-columns\" ng-model=\"panel\"></i> {{label ? label : 'Panel'}}</div>",
		popoverTemplateUrl: 'partials/PopoverTemplate.html'
	});
	
	$builderProvider.registerComponent('panelSide', {
		group: 'Panel',
		label: 'Side Panel',
		isView: true,
		template: "<div class=\"view-component\"><i class=\"fa fa-th-large\" ng-model=\"panelSide\"></i> {{label ? label : 'Side panel'}}</div>",
		popoverTemplateUrl: 'partials/PopoverTemplate.html'
	});
	
	$builderProvider.registerComponent('notebook', {
		group: 'Panel',
		label: 'Notebook',
		isView: true,
		template: "<div class=\"view-component\"><i class=\"fa fa-file-text-o\" ng-model=\"notebook\"></i> Notebook</div>",
		popoverTemplateUrl: 'partials/PopoverTemplate.html'
	});
	
	$builderProvider.registerComponent('panelTab', {
		group: 'Panel',
		label: 'Tab',
		isView: true,
		template: "<div class=\"view-component\"><i class=\"fa fa-folder\" ng-model=\"panelTab\"></i> {{label ? label : 'Tab'}}</div>",
		popoverTemplateUrl: 'partials/PopoverTemplate.html'
	});
	
     
    }
  ]);
  
  
}).call(this);
