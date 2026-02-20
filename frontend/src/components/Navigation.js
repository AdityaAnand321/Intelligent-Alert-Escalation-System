import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import '../styles/Navigation.css';

export default function Navigation() {
    const navigate = useNavigate();
    const location = useLocation();
    const username = localStorage.getItem('username');

    const handleLogout = () => {
        if (window.confirm('Are you sure you want to logout?')) {
            localStorage.removeItem('token');
            localStorage.removeItem('username');
            navigate('/login');
        }
    };

    const isActive = (path) => location.pathname === path;

    return (
        <nav className="navbar">
            <div className="navbar-brand">
                <h1>🚨 Alert System</h1>
            </div>

            {username && (
                <div className="navbar-menu">
                    <ul>
                        <li>
                            <a 
                                className={isActive('/dashboard') ? 'active' : ''}
                                onClick={() => navigate('/dashboard')}
                            >
                                Dashboard
                            </a>
                        </li>
                        <li>
                            <a 
                                className={isActive('/alerts') ? 'active' : ''}
                                onClick={() => navigate('/alerts')}
                            >
                                Alerts
                            </a>
                        </li>
                        <li>
                            <a 
                                className={isActive('/users') ? 'active' : ''}
                                onClick={() => navigate('/users')}
                            >
                                Users
                            </a>
                        </li>
                    </ul>
                </div>
            )}

            <div className="navbar-user">
                {username && (
                    <>
                        <span className="username">👤 {username}</span>
                        <button className="btn-logout" onClick={handleLogout}>Logout</button>
                    </>
                )}
            </div>
        </nav>
    );
}
