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
	angular.module('axelor.studio', ['underscore', 'builder', 'builder.components', 'ngRoute'])
	
	.config(function($routeProvider) {
		  $routeProvider
		   .when('/Model/:modelId', {
		    templateUrl: 'partials/Model.html',
		    controller: 'ModelController'
		  })
		  .when('/View/:viewId', {
		    templateUrl: 'partials/View.html',
		    controller: 'ViewController'
		  });
		  
	})
	
	.controller('StudioController', ['$location', '$http','$scope', '$builder', '_', '$route', '$routeParams', function($location, $http, $scope, $builder,  _, $route, $routeParams) {
		$scope.$route = $route;
		$scope.$http = $http;
		$scope.$location = $location;
		$scope.absUrl = $location.absUrl().split("studio/#")[0];
		$scope.metaSelectList = [];
		$scope.getSelectionList = function(scope){
			
			if($scope.metaSelectList.length == 0){
				
				postObj = {
						"offset": 0,
						"sortBy": ["name"],
						"fields": ["name"],
						"data": {}
				}
				 
				$scope.$http.post($scope.absUrl + "ws/rest/com.axelor.meta.db.MetaSelect/search", postObj).success(function(response){
					if(response.status == 0){
						$scope.metaSelectList = response.data;
						scope.selectionList  = $scope.metaSelectList;
					}
		    	});
			
			}
			else{
				scope.selectionList = $scope.metaSelectList;
			}
			
		};
		
		$scope.toCamelCase = function(name) {
			  return name.replace(/(?:^\w|[A-Z]|\b\w|\s+)/g, function(match, index) {
				    if (+match === 0) return "";
				    return index == 0 ? match.toLowerCase() : match.toUpperCase();
			  });
		};
		
	}]);
	

}).call(this);
