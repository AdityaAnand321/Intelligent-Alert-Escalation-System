import React, { useState, useEffect } from 'react';
import { dashboardAPI } from '../api';
import '../styles/Dashboard.css';

export default function DashboardPage() {
    const eventsPageSize = 10;
    const [summary, setSummary] = useState({});
    const [topOffenders, setTopOffenders] = useState([]);
    const [recentEvents, setRecentEvents] = useState([]);
    const [eventsPage, setEventsPage] = useState(1);
    const [recentAutoClosed, setRecentAutoClosed] = useState([]);
    const [autoClosedDriverFilter, setAutoClosedDriverFilter] = useState('');
    const [trend, setTrend] = useState([]);
    const [rules, setRules] = useState({});
    const [autoClosedHours, setAutoClosedHours] = useState(24);
    const [trendDays, setTrendDays] = useState(7);
    const [trendMode, setTrendMode] = useState('daily');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        fetchDashboardData();
    }, [autoClosedHours, trendDays]);

    const getWeekLabel = (dateText) => {
        if (!dateText) return 'N/A';
        const date = new Date(dateText);
        if (Number.isNaN(date.getTime())) return dateText;
        const first = new Date(Date.UTC(date.getUTCFullYear(), 0, 1));
        const dayMs = 24 * 60 * 60 * 1000;
        const dayNumber = Math.floor((date - first) / dayMs) + 1;
        const weekNumber = Math.ceil(dayNumber / 7);
        return `${date.getUTCFullYear()}-W${String(weekNumber).padStart(2, '0')}`;
    };

    const buildTrendPoints = (rawTrend) => {
        if (!Array.isArray(rawTrend)) return [];
        if (trendMode === 'daily') {
            return rawTrend;
        }

        const weeklyMap = new Map();
        rawTrend.forEach((point) => {
            if (!point || !point.date) return;
            const key = getWeekLabel(point.date);
            const existing = weeklyMap.get(key) || {
                date: key,
                totalAlerts: 0,
                escalations: 0,
                autoClosures: 0
            };

            existing.totalAlerts += Number(point.totalAlerts || 0);
            existing.escalations += Number(point.escalations || 0);
            existing.autoClosures += Number(point.autoClosures || 0);
            weeklyMap.set(key, existing);
        });

        return Array.from(weeklyMap.values());
    };

    const renderTrendPath = (points, key, width, height, padding) => {
        if (!Array.isArray(points) || points.length === 0) return '';
        const maxValue = Math.max(1, ...points.map((p) => Number(p[key] || 0)));
        const usableWidth = width - (2 * padding);
        const usableHeight = height - (2 * padding);

        return points.map((point, index) => {
            const x = padding + ((points.length === 1 ? 0 : index / (points.length - 1)) * usableWidth);
            const y = padding + (usableHeight - ((Number(point[key] || 0) / maxValue) * usableHeight));
            return `${index === 0 ? 'M' : 'L'} ${x} ${y}`;
        }).join(' ');
    };

    const fetchDashboardData = async () => {
        try {
            setLoading(true);
            const [summaryRes, offendersRes, eventsRes, autoClosedRes, trendRes, rulesRes] = await Promise.all([
                dashboardAPI.getSummary(),
                dashboardAPI.getTopOffenders(5),
                dashboardAPI.getRecentEvents(100),
                dashboardAPI.getRecentAutoClosed(autoClosedHours),
                dashboardAPI.getTrend(trendDays),
                dashboardAPI.getActiveRules()
            ]);

            const severityCounts = summaryRes.data?.severityCounts || {};
            setSummary({
                criticalAlerts: Number(severityCounts.CRITICAL || 0),
                warningAlerts: Number(severityCounts.WARNING || 0),
                infoAlerts: Number(severityCounts.INFO || 0)
            });
            setTopOffenders(Array.isArray(offendersRes.data) ? offendersRes.data : []);
            setRecentEvents(Array.isArray(eventsRes.data) ? eventsRes.data : []);
            setEventsPage(1);
            setRecentAutoClosed((Array.isArray(autoClosedRes.data) ? autoClosedRes.data : []).map((alert) => ({
                ...alert,
                id: alert?.id || alert?.alertId || ''
            })));
            setTrend(Array.isArray(trendRes.data) ? trendRes.data : []);
            setRules(rulesRes.data || {});
        } catch (err) {
            setError('Failed to load dashboard data');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const trendPoints = buildTrendPoints(trend);
    const eventsTotalPages = Math.max(1, Math.ceil(recentEvents.length / eventsPageSize));
    const pagedRecentEvents = recentEvents.slice(
        (eventsPage - 1) * eventsPageSize,
        eventsPage * eventsPageSize
    );
    const filteredAutoClosed = recentAutoClosed.filter((alert) => {
        const search = autoClosedDriverFilter.trim().toLowerCase();
        if (!search) return true;
        const driverId = String(alert?.driverId || alert?.metadata?.driverId || '').toLowerCase();
        return driverId.includes(search);
    });
    const chartWidth = 640;
    const chartHeight = 240;
    const chartPadding = 30;

    if (loading) return <div className="dashboard-container"><p>Loading...</p></div>;

    return (
        <div className="dashboard-container">
            <h1>Dashboard</h1>
            
            {error && <div className="error-message">{error}</div>}

            {summary && (
                <div className="stats-grid">
                    <div className="stat-card">
                        <h3>Critical</h3>
                        <p className="stat-value" style={{text:'10'}}>{summary.criticalAlerts || 0}</p>
                    </div>
                    <div className="stat-card">
                        <h3>Warning</h3>
                        <p className="stat-value">{summary.warningAlerts || 0}</p>
                    </div>
                    <div className="stat-card">
                        <h3>Info</h3>
                        <p className="stat-value">{summary.infoAlerts || 0}</p>
                    </div>
                </div>
            )}

            <div className="dashboard-grid">
                <div className="dashboard-section">
                    <h2>Top Offenders</h2>
                    {topOffenders && topOffenders.length > 0 ? (
                        <ul className="offenders-list">
                            {topOffenders.map((offender, idx) => (
                                <li key={idx}>
                                    <span className="driver-id">{offender?.driverId || 'Unknown'}</span>
                                    <span className="alert-count">{offender?.openAlerts || 0} alerts</span>
                                </li>
                            ))}
                        </ul>
                    ) : (
                        <p>No offender data</p>
                    )}
                </div>
            </div>

            <div className="dashboard-grid dashboard-grid-full">
                <div className="dashboard-section">
                    <h2>Recent Lifecycle Events</h2>
                    {recentEvents && recentEvents.length > 0 ? (
                        <>
                            <table className="alerts-table">
                                <thead>
                                    <tr>
                                        <th>Time</th>
                                        <th>Driver ID</th>
                                        <th>Source</th>
                                        <th>Event</th>
                                        <th>State</th>
                                        <th>Reason</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {pagedRecentEvents.map((event, idx) => (
                                        <tr key={`${event?.alertId || idx}-${idx}`}>
                                            <td>{event?.timestamp ? new Date(event.timestamp).toLocaleString() : '-'}</td>
                                            <td>{event?.driverId || '-'}</td>
                                            <td>{event?.sourceType || '-'}</td>
                                            <td>{event?.eventType || '-'}</td>
                                            <td>{event?.toState || '-'}</td>
                                            <td>{event?.reason || '-'}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                            <div className="events-pagination">
                                <button
                                    onClick={() => setEventsPage((prev) => Math.max(1, prev - 1))}
                                    disabled={eventsPage === 1}
                                >
                                    Previous
                                </button>
                                <span>Page {eventsPage} / {eventsTotalPages}</span>
                                <button
                                    onClick={() => setEventsPage((prev) => Math.min(eventsTotalPages, prev + 1))}
                                    disabled={eventsPage >= eventsTotalPages}
                                >
                                    Next
                                </button>
                            </div>
                        </>
                    ) : (
                        <p>No events found</p>
                    )}
                </div>

                <div className="dashboard-section">
                    <div className="section-header-row">
                        <h2>Recent Auto-Closed Alerts</h2>
                        <div className="autoclose-controls">
                            <div className="hours-toggle">
                                <button
                                    className={autoClosedHours === 24 ? 'active' : ''}
                                    onClick={() => setAutoClosedHours(24)}
                                >
                                    Last 24h
                                </button>
                                <button
                                    className={autoClosedHours === 168 ? 'active' : ''}
                                    onClick={() => setAutoClosedHours(168)}
                                >
                                    Last 7d
                                </button>
                            </div>
                            <input
                                className="driver-filter-input"
                                type="text"
                                placeholder="Filter by driver ID"
                                value={autoClosedDriverFilter}
                                onChange={(e) => setAutoClosedDriverFilter(e.target.value)}
                            />
                        </div>
                    </div>
                    {filteredAutoClosed && filteredAutoClosed.length > 0 ? (
                        <table className="alerts-table">
                            <thead>
                                <tr>
                                    <th>Alert ID</th>
                                    <th>Driver ID</th>
                                    <th>Source</th>
                                    <th>Closed At</th>
                                    <th>Reason</th>
                                </tr>
                            </thead>
                            <tbody>
                                {filteredAutoClosed.map((alert, idx) => (
                                    <tr key={alert?.id || idx}>
                                        <td>{alert?.id ? String(alert.id).slice(0, 10) : 'N/A'}...</td>
                                        <td>{alert?.driverId || alert?.metadata?.driverId || '-'}</td>
                                        <td>{alert?.sourceType || '-'}</td>
                                        <td>{alert?.updatedAt ? new Date(alert.updatedAt).toLocaleString() : '-'}</td>
                                        <td>{alert?.autoCloseReason || '-'}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    ) : (
                        <p>No auto-closed alerts for selected window/filter</p>
                    )}
                </div>
            </div>

            <div className="dashboard-grid dashboard-grid-full">
                <div className="dashboard-section">
                    <div className="section-header-row">
                        <h2>Trend Over Time</h2>
                        <div className="trend-controls">
                            <select value={trendDays} onChange={(e) => setTrendDays(Number(e.target.value))}>
                                <option value={7}>Last 7 Days</option>
                                <option value={30}>Last 30 Days</option>
                            </select>
                            <select value={trendMode} onChange={(e) => setTrendMode(e.target.value)}>
                                <option value="daily">Daily</option>
                                <option value="weekly">Weekly</option>
                            </select>
                        </div>
                    </div>
                    {trendPoints && trendPoints.length > 0 ? (
                        <div className="trend-chart-wrap">
                            <svg viewBox={`0 0 ${chartWidth} ${chartHeight}`} className="trend-chart" role="img" aria-label="Alerts trend">
                                <path d={renderTrendPath(trendPoints, 'totalAlerts', chartWidth, chartHeight, chartPadding)} className="line-total" />
                                <path d={renderTrendPath(trendPoints, 'escalations', chartWidth, chartHeight, chartPadding)} className="line-escalations" />
                                <path d={renderTrendPath(trendPoints, 'autoClosures', chartWidth, chartHeight, chartPadding)} className="line-autoclose" />
                            </svg>
                            <div className="trend-legend">
                                <span><i className="legend-dot legend-total" /> Total Alerts</span>
                                <span><i className="legend-dot legend-escalations" /> Escalations</span>
                                <span><i className="legend-dot legend-autoclose" /> Auto-Closures</span>
                            </div>
                        </div>
                    ) : (
                        <p>No trend data</p>
                    )}
                </div>

                <div className="dashboard-section">
                    <h2>Active Rule Configuration</h2>
                    {rules && Object.keys(rules).length > 0 ? (
                        <table className="alerts-table">
                            <thead>
                                <tr>
                                    <th>Rule</th>
                                    <th>Escalate Count</th>
                                    <th>Window (mins)</th>
                                    <th>Auto-Close If</th>
                                </tr>
                            </thead>
                            <tbody>
                                {Object.entries(rules).map(([name, rule]) => (
                                    <tr key={name}>
                                        <td>{name}</td>
                                        <td>{rule?.escalateIfCount ?? '-'}</td>
                                        <td>{rule?.windowMins ?? '-'}</td>
                                        <td>{rule?.autoCloseIf ?? '-'}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    ) : (
                        <p>No active rule configuration found</p>
                    )}
                </div>
            </div>
        </div>
    );
}
