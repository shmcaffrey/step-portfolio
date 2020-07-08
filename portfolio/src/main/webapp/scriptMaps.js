class Place {
    constructor(markerIn, contentIn) {
        this.marker = markerIn;
        this.content = contentIn;
    }
}

var chelseaContent;
var hagueContent;
var cMarker;
var hMarker;

function initMap() {

    var darkMode = new google.maps.StyledMapType(
        [
            { elementType: "geometry", stylers: [{ color: "#242f3e" }] },
            { elementType: "labels.text.stroke", stylers: [{ color: "#242f3e" }] },
            { elementType: "labels.text.fill", stylers: [{ color: "#746855" }] },
            {
                featureType: "administrative.locality",
                elementType: "labels.text.fill",
                stylers: [{ color: "#d59563" }]
            },
            {
                featureType: "poi",
                elementType: "labels.text.fill",
                stylers: [{ color: "#d59563" }]
            },
            {
                featureType: "poi.park",
                elementType: "geometry",
                stylers: [{ color: "#263c3f" }]
            },
            {
                featureType: "poi.park",
                elementType: "labels.text.fill",
                stylers: [{ color: "#6b9a76" }]
            },
            {
                featureType: "road",
                elementType: "geometry",
                stylers: [{ color: "#38414e" }]
            },
            {
                featureType: "road",
                elementType: "geometry.stroke",
                stylers: [{ color: "#212a37" }]
            },
            {
                featureType: "road",
                elementType: "labels.text.fill",
                stylers: [{ color: "#9ca5b3" }]
            },
            {
                featureType: "road.highway",
                elementType: "geometry",
                stylers: [{ color: "#746855" }]
            },
            {
                featureType: "road.highway",
                elementType: "geometry.stroke",
                stylers: [{ color: "#1f2835" }]
            },
            {
                featureType: "road.highway",
                elementType: "labels.text.fill",
                stylers: [{ color: "#f3d19c" }]
            },
            {
                featureType: "transit",
                elementType: "geometry",
                stylers: [{ color: "#2f3948" }]
            },
            {
                featureType: "transit.station",
                elementType: "labels.text.fill",
                stylers: [{ color: "#d59563" }]
            },
            {
                featureType: "water",
                elementType: "geometry",
                stylers: [{ color: "#17263c" }]
            },
            {
                featureType: "water",
                elementType: "labels.text.fill",
                stylers: [{ color: "#515c6d" }]
            },
            {
                featureType: "water",
                elementType: "labels.text.stroke",
                stylers: [{ color: "#17263c" }]
            }
        ], { name: "Dark Mode" });

    var chelseaLatLng = { lat: 42.318, lng: -84.020 };
    var hagueLatLng = { lat: 52.0705, lng: 4.3007 };

    var chelseaString = "<h1>Chelsea, MI</h1>" + 
        "<div><p>Chelsea is a city in Washtenaw County in the U.S." +
        " state of Michigan. The population was 4,944 at the 2010 census.</p></div>" +
        "<p>Attribution: Chelsea, " +
        '<a href="https://en.wikipedia.org/wiki/Chelsea,_Michigan">' +
        "https://en.wikipedia.org/wiki/Chelsea,_Michigan</a> (last visited July 07, 2020).</p>" +
        "</div></div>";

    var hagueString = "<h1>The Hague, NL</h1>" + 
        "<div><p>The Hague is a city on the North Sea coast" +
        " of the western Netherlands. Its Gothic-style Binnenhof" +
        " (or Inner Court) complex is the seat of the Dutch parliament," +
        " and 16th-century Noordeinde Palace is the king’s workplace." +
        " The city is also home to the U.N.’s International Court of" +
        " Justice, headquartered in the Peace Palace, and the International"
        " Criminal Court.</p></div>" +
        "<p>Attribution: The Hague, " +
        '<a href="https://www.google.com/search?q=the+hague&rlz=1CAERIM_enUS906US906&oq=the+&aqs=chrome.0.69i59l3j69i57j46j69i61l2j69i65.1020j0j7&sourceid=chrome&ie=UTF-8">' +
        "https://www.google.com</a> (last visited July 07, 2020).</p>" +
        "</div></div>";

    var map = new google.maps.Map(
        document.getElementById("map"), {
        center: chelseaLatLng,
        zoom: 8,
        gestureHandling: "cooperative",
        mapTypeControlOptions: {
            mapTypeIds: ["roadmap", "satellite", "hybrid", "terrain",
                "dark_mode"]
        }
    });

    cMarker = new google.maps.Marker({
        position: chelseaLatLng,
        map: map,
        title: "Hometown",
        animation: google.maps.Animation.DROP,
    });

    hMarker = new google.maps.Marker({
        position: hagueLatLng,
        map: map,
        title: "Au Pair Town",
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

    cMarker.addListener("click", function() {
        bounce(cMarker, chelseaContent);
    });
    hMarker.addListener("click", function() {
        bounce(hMarker, hagueContent);
    });
    map.mapTypes.set("dark_mode", darkMode);
    map.mapTypeId("dark_mode");
    map.setTilt(45);
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

// TODO: Add ability to have user add their own marker and content
