/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
    var query = window.location.search.substring(1);
    query = decodeURIComponent(query);
    function loadGoogleMapAPIJS(key, callback) {
		if (!key) {
			throw 'Google Maps API key is missing.';
		}
    var url = 'https://maps.googleapis.com/maps/api/js?v=3&libraries=places&key=' + key;
        var script = document.createElement('script');
        script.src = url;
        script.onload = callback;
        document.body.appendChild(script);
    }
    function getQueryVariable(variable) {
        var vars = query.split("&");
        for (var i = 0; i < vars.length; i++) {
            var pair = vars[i].split("=");
            if (pair[0] == variable) {
                return pair[1];
            }
        }
        return (false);
    }
    function loadMap() {
        var mapElement = document.getElementById('map');
        var sbElement = document.getElementById('pac-input');
        var loadingImage = document.getElementById('loadingImage');
        var minZoom = 3;
        mapElement.style.visibility = "hidden";
        loadingImage.style.display = "inline";
        var options = {
            zoom: minZoom,
            center: new google.maps.LatLng(20.8948062, 1.6760691),
            mapTypeId: google.maps.MapTypeId.ROADMAP
        };
        var map = new google.maps.Map(document.getElementById('map'), options);
        google.maps.event.addListenerOnce(map, 'bounds_changed', function() {
            // Creating an array that will store the markers
            var markers = [];
            // To show Object details on click of Pinpoint
            var infowindow = new google.maps.InfoWindow();
            var bounds = new google.maps.LatLngBounds();
            var url = getAppHome() + "/ws/map/" + getQueryVariable("object");
            var id = getQueryVariable("id");
            if (id) {
                url += "/" + id;
            }
            var requestP = $.ajax({
                type: "GET",
                contentType: 'application/json',
                url: url
            });
            requestP.done(function(result) {
                if (result.status == 0) {
                    for (var i = 0; i < result.data.length; i++) {
                        var d = result.data[i];
                        var icon = 'http://thydzik.com/thydzikGoogleMap/markerlink.php?text=' + d['pinChar'];
                        var latlng = new google.maps.LatLng(d['latit'], d['longit']);
                        var title = d['fullName'] ? d['fullName'].trim() : '';
                        var address = d['address'] ? d['address'] + '<br/>' : '';
                        var fixedPhone = d['fixedPhone'] ? "<i class='fa fa-phone'></i>&nbsp;" + d['fixedPhone'] + '<br/>' : '';
                        var emailAddress = d['emailAddress'] ? "<i class='fa fa-envelope'></i>&nbsp;" + d['emailAddress'] : '';
                        var content = "<b>" + title + "</b><br/>" + address + fixedPhone + emailAddress;
                        var iconcolor;
                        switch (d['pinColor']) {
                            case 'blue':
                                iconcolor = '5680FC';
                                break;
                            case 'gray':
                                iconcolor = 'A8A8A8 ';
                                break;
                            case 'orange':
                                iconcolor = 'EF9D3F';
                                break;
                            case 'yellow':
                                iconcolor = 'FCF356';
                                break;
                            case 'purple':
                                iconcolor = '7D54FC';
                                break;
                            case 'pink':
                                iconcolor = 'E14E9D';
                                break;
                            case 'brown':
                                iconcolor = '9E7151';
                                break;
                            default:
                                iconcolor = 'FC6355';
                                break;
                        }
                        icon = icon + '&color=' + iconcolor;
                        var marker = new google.maps.Marker({
                            position: latlng,
                            map: map,
                            title: title.split('<br/>')[0],
                            icon: icon
                        });
                        google.maps.event.addListener(marker, 'click', (function(content) {
                            return function() {
                                infowindow.setContent(content);
                                infowindow.open(map, this);
                            };
                        })(content));
                        markers.push(marker);
                        bounds.extend(marker.getPosition());
                    } //end loop

                    // Create the search box and link it to the UI element.
                    var input = document.getElementById('pac-input');
                    var searchBox = new google.maps.places.SearchBox(input);
                    map.controls[google.maps.ControlPosition.TOP_RIGHT].push(input);

                    // Bias the SearchBox results towards current map's viewport.
                    map.addListener('bounds_changed', function() {
                        searchBox.setBounds(map.getBounds());
                    });


                    var smarkers = [];
                    // Listen for the event fired when the user selects a prediction and retrieve
                    // more details for that place.
                    searchBox.addListener('places_changed', function() {
                        var places = searchBox.getPlaces();

                        if (places.length == 0) {
                            return;
                        }

                        // Clear out the old markers.
                        smarkers.forEach(function(marker) {
                            marker.setMap(null);
                        });
                        smarkers = [];

                        // For each place, get the icon, name and location.
                        var bounds = new google.maps.LatLngBounds();
                        places.forEach(function(place) {
                            if (!place.geometry) {
                                console.log("Returned place contains no geometry");
                                return;
                            }
                            var icon = {
                                url: place.icon,
                                size: new google.maps.Size(71, 71),
                                origin: new google.maps.Point(0, 0),
                                anchor: new google.maps.Point(17, 34),
                                scaledSize: new google.maps.Size(25, 25)
                            };

                            // Create a marker for each place.
                            smarkers.push(new google.maps.Marker({
                                map: map,
                                icon: icon,
                                title: place.name,
                                position: place.geometry.location
                            }));

                            if (place.geometry.viewport) {
                                // Only geocodes have viewport.
                                bounds.union(place.geometry.viewport);
                            } else {
                                bounds.extend(place.geometry.location);
                            }
                        });
                        map.fitBounds(bounds);
                    });

                    addYourLocationButton(map, markers, bounds);

                    if (markers.length < 2) {
                        var latLng;
                        if (markers.length) {
                            latLng = markers[0].getPosition();
                        } else {
                            latLng = result.company ? new google.maps.LatLng(result.company) : options.center;
                        }
                        map.setCenter(latLng);
                        map.setZoom(10);
                    } else {
                        map.fitBounds(bounds);
                        if (map.getZoom() <= minZoom) {
                            if (map.getZoom() < minZoom) {
                                map.setZoom(minZoom);
                            }
                            map.setCenter(options.center);
                        }
                    }
                } else {
                    window.location = "error.html?msg=" + result.errorMsg;
                }
            });
            requestP.fail(function(jqXHR, textStatus) {
                alert("Request failed: " + textStatus);
            });
            requestP.complete(function() {
                mapElement.style.visibility = "visible";
                sbElement.style.display = "inline";
                sbElement.style.marginTop = "15px";
                sbElement.style.marginRight = "15px";
                loadingImage.style.display = "none";
            });
            var markerclusterer = new MarkerClusterer(map, markers);
        });
    }
    window.onload = function() {
		try {
	        loadGoogleMapAPIJS(getQueryVariable('key'), function() {
	            loadMap();
	        });
		} catch (err) {
			alert(err);
		}
    }
})();
