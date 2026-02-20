import React, { useState, useEffect } from 'react';
import { alertsAPI } from '../api';
import '../styles/Alerts.css';

export default function AlertsPage() {
    const [alerts, setAlerts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [showForm, setShowForm] = useState(false);
    const [selectedAlert, setSelectedAlert] = useState(null);
    
    const [formData, setFormData] = useState({
        sourceType: '',
        severity: 'MEDIUM',
        metadata: {}
    });

    useEffect(() => {
        fetchAlerts();
    }, []);

    const fetchAlerts = async () => {
        try {
            setLoading(true);
            const response = await alertsAPI.getAllAlerts();
            setAlerts(response.data || []);
        } catch (err) {
            setError('Failed to load alerts');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const handleCreateAlert = async (e) => {
        e.preventDefault();
        try {
            await alertsAPI.createAlert(
                formData.sourceType,
                formData.severity,
                formData.metadata
            );
            setFormData({ sourceType: '', severity: 'MEDIUM', metadata: {} });
            setShowForm(false);
            fetchAlerts();
            alert('Alert created successfully!');
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to create alert');
        }
    };

    const handleResolveAlert = async (alertId) => {
        if (window.confirm('Are you sure you want to resolve this alert?')) {
            try {
                await alertsAPI.resolveAlert(alertId);
                fetchAlerts();
                alert('Alert resolved successfully!');
            } catch (err) {
                setError('Failed to resolve alert');
            }
        }
    };

    if (loading) return <div className="alerts-container"><p>Loading...</p></div>;

    return (
        <div className="alerts-container">
            <div className="alerts-header">
                <h1>Alerts</h1>
                <button 
                    className="btn-primary"
                    onClick={() => setShowForm(!showForm)}
                >
                    {showForm ? 'Cancel' : 'Create Alert'}
                </button>
            </div>

            {error && <div className="error-message">{error}</div>}

            {showForm && (
                <form className="alert-form" onSubmit={handleCreateAlert}>
                    <h3>Create New Alert</h3>
                    
                    <div className="form-group">
                        <label>Source Type</label>
                        <input
                            type="text"
                            value={formData.sourceType}
                            onChange={(e) => setFormData({...formData, sourceType: e.target.value})}
                            placeholder="e.g., GPS, TELEMATICS"
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label>Severity</label>
                        <select
                            value={formData.severity}
                            onChange={(e) => setFormData({...formData, severity: e.target.value})}
                        >
                            <option>LOW</option>
                            <option>MEDIUM</option>
                            <option>HIGH</option>
                            <option>CRITICAL</option>
                        </select>
                    </div>

                    <div className="form-group">
                        <label>Metadata (JSON)</label>
                        <textarea
                            value={JSON.stringify(formData.metadata)}
                            onChange={(e) => {
                                try {
                                    setFormData({...formData, metadata: JSON.parse(e.target.value)});
                                } catch {}
                            }}
                            placeholder='{"driverId": "DRV001", "vehicleId": "VEH001"}'
                        />
                    </div>

                    <button type="submit" className="btn-primary">Create Alert</button>
                </form>
            )}

            <div className="alerts-list">
                {alerts.length > 0 ? (
                    <table className="alerts-table">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Source</th>
                                <th>Severity</th>
                                <th>Status</th>
                                <th>Created</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {alerts.map(alert => (
                                <tr key={alert.id}>
                                    <td 
                                        className="clickable-id"
                                        onClick={() => setSelectedAlert(selectedAlert?.id === alert.id ? null : alert)}
                                    >
                                        {alert.id.substring(0, 12)}...
                                    </td>
                                    <td>{alert.sourceType}</td>
                                    <td><span className={`severity ${alert.severity.toLowerCase()}`}>{alert.severity}</span></td>
                                    <td>{alert.status}</td>
                                    <td>{new Date(alert.createdAt).toLocaleDateString()}</td>
                                    <td>
                                        {alert.status !== 'RESOLVED' && (
                                            <button
                                                className="btn-resolve"
                                                onClick={() => handleResolveAlert(alert.id)}
                                            >
                                                Resolve
                                            </button>
                                        )}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                ) : (
                    <p>No alerts found</p>
                )}
            </div>

            {selectedAlert && (
                <div className="alert-details-modal">
                    <div className="modal-content">
                        <button className="close-btn" onClick={() => setSelectedAlert(null)}>×</button>
                        <h2>Alert Details</h2>
                        <div className="details-grid">
                            <div><strong>ID:</strong> {selectedAlert.id}</div>
                            <div><strong>Source:</strong> {selectedAlert.sourceType}</div>
                            <div><strong>Severity:</strong> {selectedAlert.severity}</div>
                            <div><strong>Status:</strong> {selectedAlert.status}</div>
                            <div><strong>Created:</strong> {new Date(selectedAlert.createdAt).toLocaleString()}</div>
                            {selectedAlert.metadata && (
                                <div><strong>Metadata:</strong> {JSON.stringify(selectedAlert.metadata, null, 2)}</div>
                            )}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
