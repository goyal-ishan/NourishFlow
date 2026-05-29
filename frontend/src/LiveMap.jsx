import React from 'react';
import { MapContainer, TileLayer, Marker, Polyline, Popup } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

// 🐛 FIX: This resolves the Leaflet default icon "missing/broken image" bug in React
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png';
import markerIcon from 'leaflet/dist/images/marker-icon.png';
import markerShadow from 'leaflet/dist/images/marker-shadow.png';

delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconUrl: markerIcon,
  iconRetinaUrl: markerIcon2x,
  shadowUrl: markerShadow,
});

// Optional: Create a custom color icon for the Charity Destination to differentiate from the Store
const charityIcon = new L.Icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png',
  shadowUrl: markerShadow,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41]
});

const LiveMap = ({ matches }) => {
  // 📍 Center the map view over Prayagraj/Allahabad area (Matches your Backend coordinates!)
  const position = [25.4358, 81.8463];

  return (
    // ⚠️ CRITICAL: Leaflet maps require an explicit CSS height or they will render at 0px!
    <MapContainer 
      center={position} 
      zoom={13} 
      style={{ height: "550px", width: "100%", borderRadius: "12px" }}
    >
      {/* TileLayer loads the visual OpenStreetMap design */}
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />

      {/* Loop through the matches array streaming from Spring Boot */}
      {matches.map((match) => {
        // Validation check: Make sure coordinates exist before trying to draw them
        if (!match.sourceLat || !match.sourceLng || !match.destLat || !match.destLng) {
          return null; 
        }

        const storeCoords = [match.sourceLat, match.sourceLng];
        const charityCoords = [match.destLat, match.destLng];

        return (
          <React.Fragment key={match.id}>
            {/* 🏢 Store Marker (Standard Blue Pin) */}
            <Marker position={storeCoords}>
              <Popup>
                <strong>🏬 {match.originStore}</strong><br />
                📦 Dispatching: {match.itemName}<br />
                ⚖️ Weight: {match.quantityLbs} Lbs
              </Popup>
            </Marker>

            {/* 🤝 Charity Marker (Custom Green Pin) */}
            <Marker position={charityCoords} icon={charityIcon}>
              <Popup>
                <strong>❤️ {match.charityName}</strong><br />
                ✨ Receiving: {match.itemName}<br />
                ⏳ Days Left: {match.expiryDaysAtMatch} Days
              </Popup>
            </Marker>

            {/* 🛣️ Dynamic Dotted Line connecting Store -> Charity */}
            <Polyline 
              positions={[storeCoords, charityCoords]} 
              color="#3b82f6" // Nice professional blue line
              weight={4}
              dashArray="8, 12" // Makes the line cleanly dotted/dashed to simulate routing
              opacity={0.8}
            />
          </React.Fragment>
        );
      })}
    </MapContainer>
  );
};

export default LiveMap;