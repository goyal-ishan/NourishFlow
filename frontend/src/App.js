import React, { useState, useEffect } from "react";
import LiveMap from "./LiveMap"; // 🗺️ Imported your new Leaflet map tracking layer
import "./App.css";

function App() {
  // State variables to hold our UI logic and API data
  const [activeTab, setActiveTab] = useState("dashboard");
  const [matches, setMatches] = useState([]);
  const [error, setError] = useState(false);

  // Updated: Automatically polls your Spring Boot API to update live coordinates
  useEffect(() => {
    const fetchLiveTelemetry = () => {
      fetch("http://localhost:8080/api/matches")
        .then((response) => {
          if (!response.ok) throw new Error("Network response was not ok");
          return response.json();
        })
        .then((data) => {
          setMatches(data);
          setError(false);
        })
        .catch((err) => {
          console.error("Error fetching geospatial telemetry:", err);
          setError(true);
        });
    };

    // Initial load immediately
    fetchLiveTelemetry();

    // Polls every 5 seconds to match the Kafka producer's schedule speed!
    const interval = setInterval(fetchLiveTelemetry, 5000);
    return () => clearInterval(interval);
  }, []); 

  // Calculate analytics directly from the state
  const totalLbs = matches.reduce((sum, match) => sum + match.quantityLbs, 0);
  const uniqueCharities = new Set(matches.map((match) => match.charityName)).size;

  return (
    <div>
      {/* Navigation Bar */}
      <div className="navbar">
        <h2>❄️ Cold Chain Rescue</h2>
        <button
          className={`nav-btn ${activeTab === "dashboard" ? "active" : ""}`}
          onClick={() => setActiveTab("dashboard")}
        >
          Live Matches
        </button>
        <button
          className={`nav-btn ${activeTab === "analytics" ? "active" : ""}`}
          onClick={() => setActiveTab("analytics")}
        >
          Impact Analytics
        </button>
        <button
          className={`nav-btn ${activeTab === "about" ? "active" : ""}`}
          onClick={() => setActiveTab("about")}
        >
          System Architecture
        </button>
      </div>

      <div className="container">
        {/* TAB 1: Dashboard */}
        {activeTab === "dashboard" && (
          <div>
            <h3>Live Routing Dashboard</h3>
            {error ? (
              <p style={{ color: "red" }}>
                Failed to connect to Spring Boot backend. Ensure Docker and the Java application are running.
              </p>
            ) : (
              <>
                {/* 🗺️ NEW: Integrated Live Map Layout right onto the dashboard screen */}
                <div style={{ 
                  backgroundColor: "#ffffff", 
                  padding: "16px", 
                  borderRadius: "16px", 
                  boxShadow: "0 4px 6px rgba(0,0,0,0.05)",
                  marginBottom: "24px" 
                }}>
                  <h4 style={{ margin: "0 0 12px 0", color: "#334155" }}>📍 Real-Time Spatial Distribution Map (Prayagraj)</h4>
                  <LiveMap matches={matches} />
                </div>

                {/* Live History Data Ledger Table */}
                <div style={{ backgroundColor: "white", padding: "20px", borderRadius: "12px", boxShadow: "0 4px 6px rgba(0,0,0,0.05)" }}>
                  <h4 style={{ margin: "0 0 12px 0", color: "#334155" }}>📋 Active Distribution Ledger</h4>
                  <table>
                    <thead>
                      <tr>
                        <th>#</th>
                        <th>Origin Store</th>
                        <th>Item Name</th>
                        <th>Quantity</th>
                        <th>Matched Charity</th>
                        <th>Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      {matches.map((match, index) => (
                        <tr key={match.id || index}>
                          <td>{index + 1}</td>
                          <td>
                            <strong>{match.originStore}</strong>
                          </td>
                          <td>{match.itemName}</td>
                          <td>{match.quantityLbs} lbs</td>
                          <td>🏢 {match.charityName}</td>
                          <td>
                            <span className="status">Routed</span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </>
            )}
          </div>
        )}

        {/* TAB 2: Analytics */}
        {activeTab === "analytics" && (
          <div>
            <h3>Platform Impact</h3>
            <div className="stats-grid">
              <div className="stat-card">
                <h3>Total Food Saved (lbs)</h3>
                <h1>{totalLbs}</h1>
              </div>
              <div className="stat-card">
                <h3>Successful Deliveries</h3>
                <h1>{matches.length}</h1>
              </div>
              <div className="stat-card">
                <h3>Active Charities Engaged</h3>
                <h1>{uniqueCharities}</h1>
              </div>
            </div>
            <p>
              This data is calculated in real-time from the PostgreSQL Match
              History database.
            </p>
          </div>
        )}

        {/* TAB 3: About */}
        {activeTab === "about" && (
          <div>
            <h3>How It Works</h3>
            <div
              style={{
                background: "white",
                padding: "20px",
                borderRadius: "8px",
                boxShadow: "0 4px 6px rgba(0,0,0,0.05)",
              }}
            >
              <h4>1. The Generator (Spring Boot)</h4>
              <p>
                Simulates real-time IoT temperature and inventory data from
                local grocery stores.
              </p>
              <h4>2. The Broker (Apache Kafka)</h4>
              <p>
                Streams high-volume data streams instantly, acting as the
                central nervous system.
              </p>
              <h4>3. The Logic Engine (Java)</h4>
              <p>
                Evaluates food expiry and refrigeration requirements, actively
                matching items to the nearest suitable charity and generating geospatial routes.
              </p>
              <h4>4. The Database (PostgreSQL / Docker)</h4>
              <p>
                Stores an immutable ledger of all successfully routed inventory including transit coordinate parameters.
              </p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default App;