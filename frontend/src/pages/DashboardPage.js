import React, { useState, useEffect } from 'react';
import { dashboardAPI, alertsAPI } from '../api';
import '../styles/Dashboard.css';

export default function DashboardPage() {
    const [summary, setSummary] = useState(null);
    const [topOffenders, setTopOffenders] = useState([]);
    const [recentAlerts, setRecentAlerts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        fetchDashboardData();
    }, []);

    const fetchDashboardData = async () => {
        try {
            setLoading(true);
            const [summaryRes, offendersRes, alertsRes] = await Promise.all([
                dashboardAPI.getSummary(),
                dashboardAPI.getTopOffenders(5),
                alertsAPI.getAllAlerts()
            ]);

            setSummary(summaryRes.data);
            setTopOffenders(offendersRes.data || []);
            setRecentAlerts(alertsRes.data.slice(0, 5) || []);
        } catch (err) {
            setError('Failed to load dashboard data');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    if (loading) return <div className="dashboard-container"><p>Loading...</p></div>;

    return (
        <div className="dashboard-container">
            <h1>Dashboard</h1>
            
            {error && <div className="error-message">{error}</div>}

            {summary && (
                <div className="stats-grid">
                    <div className="stat-card">
                        <h3>Total Alerts</h3>
                        <p className="stat-value">{summary.totalAlerts || 0}</p>
                    </div>
                    <div className="stat-card critical">
                        <h3>Critical</h3>
                        <p className="stat-value">{summary.criticalAlerts || 0}</p>
                    </div>
                    <div className="stat-card">
                        <h3>Escalated</h3>
                        <p className="stat-value">{summary.escalatedAlerts || 0}</p>
                    </div>
                    <div className="stat-card">
                        <h3>Resolved</h3>
                        <p className="stat-value">{summary.resolvedAlerts || 0}</p>
                    </div>
                </div>
            )}

            <div className="dashboard-grid">
                <div className="dashboard-section">
                    <h2>Recent Alerts</h2>
                    {recentAlerts.length > 0 ? (
                        <table className="alerts-table">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Source</th>
                                    <th>Severity</th>
                                    <th>Status</th>
                                    <th>Created</th>
                                </tr>
                            </thead>
                            <tbody>
                                {recentAlerts.map(alert => (
                                    <tr key={alert.id}>
                                        <td>{alert.id.substring(0, 8)}...</td>
                                        <td>{alert.sourceType}</td>
                                        <td><span className={`severity ${alert.severity.toLowerCase()}`}>{alert.severity}</span></td>
                                        <td>{alert.status}</td>
                                        <td>{new Date(alert.createdAt).toLocaleDateString()}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    ) : (
                        <p>No recent alerts</p>
                    )}
                </div>

                <div className="dashboard-section">
                    <h2>Top Offenders</h2>
                    {topOffenders.length > 0 ? (
                        <ul className="offenders-list">
                            {topOffenders.map((offender, idx) => (
                                <li key={idx}>
                                    <span className="driver-id">{offender.driverId}</span>
                                    <span className="alert-count">{offender.alertCount} alerts</span>
                                </li>
                            ))}
                        </ul>
                    ) : (
                        <p>No offender data</p>
                    )}
                </div>
            </div>
        </div>
    );
}
