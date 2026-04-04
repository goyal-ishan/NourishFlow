import React, { useState, useEffect } from "react";
import "./App.css";

function App() {
  // State variables to hold our UI logic and API data
  const [activeTab, setActiveTab] = useState("dashboard");
  const [matches, setMatches] = useState([]);
  const [error, setError] = useState(false);

  // useEffect runs exactly once when the component first loads
  useEffect(() => {
    fetch("http://localhost:8080/api/matches")
      .then((response) => {
        if (!response.ok) throw new Error("Network response was not ok");
        return response.json();
      })
      .then((data) => setMatches(data))
      .catch((err) => {
        console.error("Error fetching data:", err);
        setError(true);
      });
  }, []); // The empty array means "only run on mount"

  // Calculate analytics directly from the state
  const totalLbs = matches.reduce((sum, match) => sum + match.quantityLbs, 0);
  const uniqueCharities = new Set(matches.map((match) => match.charityName))
    .size;

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
                Failed to connect to Spring Boot backend.
              </p>
            ) : (
              // ... (keep the rest of your code the same)

              <table>
                <thead>
                  <tr>
                    <th>#</th> {/* NEW: Index Column Header */}
                    <th>Origin Store</th>
                    <th>Item Name</th>
                    <th>Quantity</th>
                    <th>Matched Charity</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {/* We add 'index' as the second argument in .map(). 
       React gives us the position of the item in the list automatically!
    */}
                  {matches.map((match, index) => (
                    <tr key={match.id}>
                      <td>{index + 1}</td>{" "}
                      {/* NEW: Display 1, 2, 3... instead of DB ID */}
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

              // ... (keep the rest of your code the same)
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
                matching items to the nearest suitable charity.
              </p>
              <h4>4. The Database (PostgreSQL / Docker)</h4>
              <p>
                Stores an immutable ledger of all successfully routed inventory.
              </p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default App;
