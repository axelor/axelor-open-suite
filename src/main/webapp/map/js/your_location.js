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
function addYourLocationButton(map, markers, bounds) {
  var minZoom = 3;
  var zoom = 13;

  var marker = new google.maps.Marker({
    clickable: false,
    icon: new google.maps.MarkerImage('//maps.gstatic.com/mapfiles/mobile/mobileimgs2.png',
        new google.maps.Size(22, 22),
        new google.maps.Point(0, 18),
        new google.maps.Point(11, 11)),
    shadow: null,
    zIndex: 999,
    map: map
  });

  var me;

  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(function (pos) {
      me = new google.maps.LatLng(pos.coords.latitude, pos.coords.longitude);
      marker.setPosition(me);

      markers.push(marker);
      bounds.extend(me);

      if (markers.length < 2) {
        if (markers.length) {
          map.setCenter(markers[0].getPosition());
        }
        map.setZoom(zoom);
      } else {
        map.fitBounds(bounds);
        if (map.getZoom() <= minZoom) {
          if (map.getZoom() < minZoom) {
            map.setZoom(minZoom);
          }
        }
      }

    }, function (error) {
      alert(error.message);
    });
  }

  var controlDiv = document.createElement('div');

  var firstChild = document.createElement('button');
  firstChild.style.backgroundColor = '#fff';
  firstChild.style.border = 'none';
  firstChild.style.outline = 'none';
  firstChild.style.width = '28px';
  firstChild.style.height = '28px';
  firstChild.style.borderRadius = '2px';
  firstChild.style.boxShadow = '0 1px 4px rgba(0,0,0,0.3)';
  firstChild.style.cursor = 'pointer';
  firstChild.style.marginRight = '10px';
  firstChild.style.padding = '0px';
  firstChild.style.paddingBottom = '10px';
  firstChild.title = 'Your Location';
  controlDiv.appendChild(firstChild);

  var secondChild = document.createElement('div');
  secondChild.style.margin = '5px';
  secondChild.style.width = '18px';
  secondChild.style.height = '18px';
  secondChild.style.backgroundImage = 'url(https://maps.gstatic.com/tactile/mylocation/mylocation-sprite-1x.png)';
  secondChild.style.backgroundSize = '180px 18px';
  secondChild.style.backgroundPosition = '0px 0px';
  secondChild.style.backgroundRepeat = 'no-repeat';
  secondChild.id = 'your_location_img';
  firstChild.appendChild(secondChild);

  google.maps.event.addListener(map, 'dragend', function () {
    $('#your_location_img').css('background-position', '0px 0px');
  });

  firstChild.addEventListener('click', function () {
    var imgX = '0';
    var animationInterval = setInterval(function () {
      if (imgX == '-18') {
        imgX = '0';
      } else {
        imgX = '-18';
      }
      $('#your_location_img').css('background-position', imgX + 'px 0px');
    }, 500);

    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(function (pos) {
        me = new google.maps.LatLng(pos.coords.latitude, pos.coords.longitude);

        marker.setPosition(me);
        map.setCenter(me);
        map.setZoom(zoom);
        clearInterval(animationInterval);
        $('#your_location_img').css('background-position', '-144px 0px');
      }, function (error) {
        clearInterval(animationInterval);
        alert(error.message);
      });
    } else {
      clearInterval(animationInterval);
      $('#your_location_img').css('background-position', '0px 0px');
    }
  });

  controlDiv.index = 1;
  map.controls[google.maps.ControlPosition.RIGHT_BOTTOM].push(controlDiv);

  return marker;
}
