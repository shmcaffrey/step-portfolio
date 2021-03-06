class Place {
  constructor(markerIn, contentIn) {
    this.marker = markerIn;
    this.content = contentIn;
  }
}

function initMap() {
  let chelseaContent;
  let hagueContent;
  let cMarker;
  let hMarker;

  let darkMode = new google.maps.StyledMapType(
    [
      { elementType: 'geometry', stylers: [{ color: '#242f3e' }] },
      { elementType: 'labels.text.stroke', stylers: [{ color: '#242f3e' }] },
      { elementType: 'labels.text.fill', stylers: [{ color: '#746855' }] },
      {
        featureType: 'administrative.locality',
        elementType: 'labels.text.fill',
        stylers: [{ color: '#d59563' }]
      },
      {
        featureType: 'poi',
        elementType: 'labels.text.fill',
        stylers: [{ color: '#d59563' }]
      },
      {
        featureType: 'poi.park',
        elementType: 'geometry',
        stylers: [{ color: '#263c3f' }]
      },
      {
        featureType: 'poi.park',
        elementType: 'labels.text.fill',
        stylers: [{ color: '#6b9a76' }]
      },
      {
        featureType: 'road',
        elementType: 'geometry',
        stylers: [{ color: '#38414e' }]
      },
      {
        featureType: 'road',
        elementType: 'geometry.stroke',
        stylers: [{ color: '#212a37' }]
      },
      {
        featureType: 'road',
        elementType: 'labels.text.fill',
        stylers: [{ color: '#9ca5b3' }]
      },
      {
        featureType: 'road.highway',
        elementType: 'geometry',
        stylers: [{ color: '#746855' }]
      },
      {
        featureType: 'road.highway',
        elementType: 'geometry.stroke',
        stylers: [{ color: '#1f2835' }]
      },
      {
        featureType: 'road.highway',
        elementType: 'labels.text.fill',
        stylers: [{ color: '#f3d19c' }]
      },
      {
        featureType: 'transit',
        elementType: 'geometry',
        stylers: [{ color: '#2f3948' }]
      },
      {
        featureType: 'transit.station',
        elementType: 'labels.text.fill',
        stylers: [{ color: '#d59563' }]
      },
      {
        featureType: 'water',
        elementType: 'geometry',
        stylers: [{ color: '#17263c' }]
      },
      {
        featureType: 'water',
        elementType: 'labels.text.fill',
        stylers: [{ color: '#515c6d' }]
      },
      {
        featureType: 'water',
        elementType: 'labels.text.stroke',
        stylers: [{ color: '#17263c' }]
      }
    ], { name: 'Dark Mode' });

  let chelseaLatLng = { lat: 42.318, lng: -84.020 };
  let hagueLatLng = { lat: 52.0705, lng: 4.3007 };


  let chelseaString = '<h1>Chelsea, MI</h1>' + 
    '<p>Chelsea is a city in Washtenaw County in the U.S.' +
    ' state of Michigan. The population was 4,944 at the 2010 census.</p>' +
    '<p>Attribution: Chelsea, ' +
    '<a href="https://en.wikipedia.org/wiki/Chelsea,_Michigan">' +
    'https://en.wikipedia.org/wiki/Chelsea,_Michigan</a> (last visited July 07, 2020).</p>';

  let hagueString = '<h1>The Hague, NL</h1>' + 
    '<p>The Hague is a city on the North Sea coast' +
    ' of the western Netherlands. Its Gothic-style Binnenhof' +
    ' (or Inner Court) complex is the seat of the Dutch parliament,' +
    ' and 16th-century Noordeinde Palace is the king’s workplace.</p>' +
    '<p>Attribution: The Hague, ' +
    '<a href="https://www.google.com/search?q=the+hague&rlz=1CAERIM_enUS906US906&oq=the+&aqs=chrome.0.69i59l3j69i57j46j69i61l2j69i65.1020j0j7&sourceid=chrome&ie=UTF-8">' +
    'https://www.google.com</a> (last visited July 07, 2020).</p>';

  let map = new google.maps.Map(
    document.getElementById('map'), {
    center: chelseaLatLng,
    zoom: 4,
    gestureHandling: 'cooperative',
    mapTypeControlOptions: {
        mapTypeIds: ['roadmap', 'satellite', 'hybrid', 'terrain',
            'dark_mode']
    }
  });

  cMarker = new google.maps.Marker({
    position: chelseaLatLng,
    map: map,
    title: 'Hometown',
    animation: google.maps.Animation.DROP,
  });

  hMarker = new google.maps.Marker({
    position: hagueLatLng,
    map: map,
    title: 'Au Pair Town',
    animation: google.maps.Animation.DROP,
  });

  chelseaContent = new google.maps.InfoWindow({
    content: chelseaString,
    pixelOffset:  new google.maps.Size(0, -20)
  });

  hagueContent = new google.maps.InfoWindow({
    content: hagueString,
    pixelOffset:  new google.maps.Size(0, -20)
  });

  cMarker.addListener('click', function() {
    bounce(cMarker, chelseaContent);
  });
  hMarker.addListener('click', function() {
    bounce(hMarker, hagueContent);
  });

  chelseaContent.addListener('closeclick', function() {
    bounce(cMarker, chelseaContent);
  });
  hagueContent.addListener('closeclick', function() {
    bounce(hMarker, hagueContent);
  });

  map.mapTypes.set('dark_mode', darkMode);
  map.setTilt(45);

  map.addListener('rightclick', function (e) {
    addNewMarker(e.latLng);
  });

  addUserMarkers(map);
}

function bounce(marker, infoWindow) {
  if (marker.getAnimation() !== null) {
    marker.setAnimation(null);
    infoWindow.close(map, marker);
  } else {
    marker.setAnimation(google.maps.Animation.BOUNCE);
    infoWindow.open(map, marker);
  }
}

function addUserMarkers(map) {
  fetch('/map-marker').then(response => response.json()).then((coords) => {
    coords.forEach((coord) => {
      createMarker(coord, map);
    })
  });
}

function addNewMarker(latLng) {
  let params = new URLSearchParams();
  params.append('lat', latLng.lat());
  params.append('lng', latLng.lng());
  const request = new Request(('/map-marker'), {method: 'POST', body: params});
  console.log('fetch completed')
  fetch(request).then(initMap());
}

function createMarker(coord, map) {
  console.log('adding marker: ' + coord.lat + ', ' + coord.lng);
  let marker = new google.maps.Marker({
    position: { lat: coord.lat, lng: coord.lng },
    map: map,
    animation: google.maps.Animation.DROP
  });
}