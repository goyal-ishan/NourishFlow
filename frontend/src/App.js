import React, { useState, useEffect } from "react";
import client from "./api/axiosClient";
import LiveMap from "./LiveMap";
import "./App.css";
import { API_BASE } from './config';
console.log('🚨 Current API_BASE in Production:', API_BASE);

function App() {
  const [activeTab, setActiveTab] = useState("dashboard");
  const [matches, setMatches] = useState([]);
  const [error, setError] = useState(false);

  useEffect(() => {
    const fetchLiveTelemetry = () => {
      client.get('/api/matches')
        .then((response) => {
          const data = response.data;
          
          // 🛡️ Ensure matches is ALWAYS an array even if Spring Boot wraps it
          if (Array.isArray(data)) {
            setMatches(data);
          } else if (data && Array.isArray(data.content)) {
            setMatches(data.content); // Handles Spring Pageable objects
          } else {
            setMatches([]);
          }
          
          setError(false);
        })
        .catch((err) => {
          console.error("Error fetching geospatial telemetry:", err);
          setError(true);
        });
    };

    fetchLiveTelemetry();
    const interval = setInterval(fetchLiveTelemetry, 5000);
    return () => clearInterval(interval);
  }, []); 

  // 🛡️ Safeguard calculations against non-array values
  const safeMatches = Array.isArray(matches) ? matches : [];
  const totalLbs = safeMatches.reduce((sum, match) => sum + (match.quantityLbs || 0), 0);
  const uniqueCharities = new Set(safeMatches.map((match) => match.charityName)).size;

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
                Failed to connect to Spring Boot backend. Ensure Render service is live.
              </p>
            ) : (
              <>
                <div style={{ 
                  backgroundColor: "#ffffff", 
                  padding: "16px", 
                  borderRadius: "16px", 
                  boxShadow: "0 4px 6px rgba(0,0,0,0.05)",
                  marginBottom: "24px" 
                }}>
                  <h4 style={{ margin: "0 0 12px 0", color: "#334155" }}>📍 Real-Time Spatial Distribution Map (Prayagraj)</h4>
                  <LiveMap matches={safeMatches} />
                </div>

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
                      {safeMatches.map((match, index) => (
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
                <h1>{safeMatches.length}</h1>
              </div>
              <div className="stat-card">
                <h3>Active Charities Engaged</h3>
                <h1>{uniqueCharities}</h1>
              </div>
            </div>
          </div>
        )}

        {/* TAB 3: About */}
        {activeTab === "about" && (
          <div>
            <h3>How It Works</h3>
            <div style={{ background: "white", padding: "20px", borderRadius: "8px" }}>
              <h4>1. The Generator (Spring Boot)</h4>
              <p>Simulates real-time IoT temperature and inventory data.</p>
              <h4>2. The Broker (Apache Kafka)</h4>
              <p>Streams high-volume data streams instantly.</p>
              <h4>3. The Logic Engine (Java)</h4>
              <p>Evaluates food expiry and matches items to charities.</p>
              <h4>4. The Database (PostgreSQL)</h4>
              <p>Stores an immutable ledger of all routed inventory.</p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default App;