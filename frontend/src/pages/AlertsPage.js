import React, { useState, useEffect } from 'react';
import { alertsAPI } from '../api';
import '../styles/Alerts.css';

export default function AlertsPage() {
    const [alerts, setAlerts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [showForm, setShowForm] = useState(false);
    const [selectedAlert, setSelectedAlert] = useState(null);
    const [selectedAlertHistory, setSelectedAlertHistory] = useState([]);
    const [detailsLoading, setDetailsLoading] = useState(false);
    
    const [formData, setFormData] = useState({
        sourceType: 'OVERSPEEDING',
        severity: 'INFO',
        driverId: '',
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
                formData.driverId,
                formData.metadata
            );
            setFormData({ sourceType: 'OVERSPEEDING', severity: 'INFO', driverId: '', metadata: {} });
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

    const handleSelectAlert = async (alert) => {
        if (selectedAlert?.alertId === alert.alertId) {
            setSelectedAlert(null);
            setSelectedAlertHistory([]);
            return;
        }

        setDetailsLoading(true);
        try {
            const response = await alertsAPI.getAlertById(alert.alertId);
            setSelectedAlert(response.data?.alert || alert);
            setSelectedAlertHistory(Array.isArray(response.data?.history) ? response.data.history : []);
        } catch (err) {
            setSelectedAlert(alert);
            setSelectedAlertHistory([]);
            setError('Failed to load alert history');
        } finally {
            setDetailsLoading(false);
        }
    };

    const getDocumentStatus = (alert) => {
        if (!alert || alert.sourceType !== 'COMPLIANCE') return '-';
        const documentValid = String(alert?.metadata?.document_valid || 'false').toLowerCase() === 'true';
        const expiryRaw = alert?.metadata?.document_expiry_date;
        if (expiryRaw) {
            const expiryDate = new Date(expiryRaw);
            if (!Number.isNaN(expiryDate.getTime())) {
                const isExpired = expiryDate.getTime() < Date.now();
                if (isExpired) return 'EXPIRED';
                return documentValid ? 'VALID' : 'PENDING';
            }
        }
        return documentValid ? 'VALID' : 'EXPIRED';
    };

    const getDocumentExpiryDate = (alert) => {
        if (!alert || alert.sourceType !== 'COMPLIANCE') return '-';
        const expiryRaw = alert?.metadata?.document_expiry_date;
        if (!expiryRaw) return '-';
        const expiryDate = new Date(expiryRaw);
        if (Number.isNaN(expiryDate.getTime())) return '-';
        return expiryDate.toLocaleDateString();
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
                        <select
                            value={formData.sourceType}
                            onChange={(e) => setFormData({...formData, sourceType: e.target.value})}
                            required
                        >
                            <option>OVERSPEEDING</option>
                            <option>COMPLIANCE</option>
                            <option>NEGATIVE_FEEDBACK</option>
                        </select>
                    </div>

                    <div className="form-group">
                        <label>Severity</label>
                        <select
                            value={formData.severity}
                            onChange={(e) => setFormData({...formData, severity: e.target.value})}
                        >
                            <option>INFO</option>
                            <option>WARNING</option>
                            <option>CRITICAL</option>
                        </select>
                    </div>

                    <div className="form-group">
                        <label>Driver ID</label>
                        <input
                            type="text"
                            value={formData.driverId}
                            onChange={(e) => setFormData({...formData, driverId: e.target.value})}
                            placeholder="Enter driver ID (e.g. DRV001)"
                            required
                        />
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
                                <th>Driver ID</th>
                                <th>Source</th>
                                <th>Document</th>
                                <th>Expiry Date</th>
                                <th>Severity</th>
                                <th>Status</th>
                                <th>Created</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {alerts.map(alert => (
                                <tr key={alert?.alertId || Math.random()}>
                                    <td 
                                        className="clickable-id"
                                        onClick={() => handleSelectAlert(alert)}
                                    >
                                        {alert?.alertId ? String(alert.alertId).substring(0, 12) : 'N/A'}...
                                    </td>
                                    <td>{alert?.driverId || alert?.metadata?.driverId || '-'}</td>
                                    <td>{alert?.sourceType || '-'}</td>
                                    <td>{getDocumentStatus(alert)}</td>
                                    <td>{getDocumentExpiryDate(alert)}</td>
                                    <td><span className={`severity ${(alert?.severity || '').toLowerCase()}`}>{alert?.severity || '-'}</span></td>
                                    <td>{alert?.status || '-'}</td>
                                    <td>{alert?.timestamp ? new Date(alert.timestamp).toLocaleDateString() : '-'}</td>
                                    <td>
                                        {alert?.status !== 'RESOLVED' && alert?.alertId && (
                                            <button
                                                className="btn-resolve"
                                                onClick={() => handleResolveAlert(alert.alertId)}
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
                        <button className="close-btn" onClick={() => { setSelectedAlert(null); setSelectedAlertHistory([]); }}>×</button>
                        <h2>Alert Details</h2>
                        <div className="details-grid">
                            <div><strong>ID:</strong> {selectedAlert.alertId}</div>
                            <div><strong>Source:</strong> {selectedAlert.sourceType}</div>
                            <div><strong>Driver ID:</strong> {selectedAlert.driverId || selectedAlert.metadata?.driverId || '-'}</div>
                            <div><strong>Document Status:</strong> {getDocumentStatus(selectedAlert)}</div>
                            <div><strong>Document Expiry:</strong> {getDocumentExpiryDate(selectedAlert)}</div>
                            <div><strong>Severity:</strong> {selectedAlert.severity}</div>
                            <div><strong>Status:</strong> {selectedAlert.status}</div>
                            <div><strong>Created:</strong> {selectedAlert.timestamp ? new Date(selectedAlert.timestamp).toLocaleString() : '-'}</div>
                            {selectedAlert.metadata && (
                                <div><strong>Metadata:</strong> {JSON.stringify(selectedAlert.metadata, null, 2)}</div>
                            )}
                            <div>
                                <strong>Lifecycle History:</strong>
                                {detailsLoading ? (
                                    <p>Loading history...</p>
                                ) : selectedAlertHistory.length > 0 ? (
                                    <ul>
                                        {selectedAlertHistory.map((event, idx) => (
                                            <li key={event?.eventId || idx}>
                                                {event?.timestamp ? new Date(event.timestamp).toLocaleString() : '-'} - {event?.eventType || '-'}
                                                {' '}({event?.fromStatus || '-'} {'→'} {event?.toStatus || '-'})
                                                {event?.reason ? ` - ${event.reason}` : ''}
                                            </li>
                                        ))}
                                    </ul>
                                ) : (
                                    <p>No lifecycle history found</p>
                                )}
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
