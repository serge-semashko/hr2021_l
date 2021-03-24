(function() // IIFE
  {
    'use strict';

    var mymap = L.map('mapid').setView([51.505, -0.09], 13),
      mapclickedCount = 0,
      markerclickedCount = 0,
      polylineclickedCount = 0,
      marker = L.marker([40, -120]).addTo(mymap),
      latlngs = [
        [45.51, -122.68],
        [37.77, -122.43],
        [34.04, -118.2]
      ],
      polyline = L.polyline(latlngs, {
        color: 'yellow'
      }).addTo(mymap);

    mymap.fitBounds(polyline.getBounds());

    mymap.on(
      'click',
      function() {
        document.querySelector('#mapclicked').textContent = 'Map clicked (the ' + String(++mapclickedCount) + '. time)!';
      });

    marker.on(
      'click',
      function() {
        document.querySelector('#markerclicked').textContent = 'Marker clicked (the ' + String(++markerclickedCount) + '. time)!';
      });

    polyline.on(
      'click',
      function(e) {
        document.querySelector('#polylineclicked').textContent = 'Polyline clicked (the ' + String(++polylineclickedCount) + '. time)!';

        L.DomEvent.stopPropagation(e);
      });
  }()
          );
