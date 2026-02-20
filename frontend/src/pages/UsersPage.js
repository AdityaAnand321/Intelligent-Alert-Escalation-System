import React, { useState, useEffect } from 'react';
import { authAPI } from '../api';
import '../styles/Users.css';

export default function UsersPage() {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [showForm, setShowForm] = useState(false);
    
    const [newUser, setNewUser] = useState({
        username: '',
        password: ''
    });

    useEffect(() => {
        fetchUsers();
    }, []);

    const fetchUsers = async () => {
        try {
            setLoading(true);
            const response = await authAPI.getAllUsers();
            setUsers(response.data || []);
        } catch (err) {
            setError('Failed to load users');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const handleAddUser = async (e) => {
        e.preventDefault();
        try {
            await authAPI.register(newUser.username, newUser.password);
            setNewUser({ username: '', password: '' });
            setShowForm(false);
            fetchUsers();
            alert('User created successfully!');
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to create user');
        }
    };

    if (loading) return <div className="users-container"><p>Loading...</p></div>;

    return (
        <div className="users-container">
            <div className="users-header">
                <h1>Users</h1>
                <button 
                    className="btn-primary"
                    onClick={() => setShowForm(!showForm)}
                >
                    {showForm ? 'Cancel' : 'Add User'}
                </button>
            </div>

            {error && <div className="error-message">{error}</div>}

            {showForm && (
                <form className="user-form" onSubmit={handleAddUser}>
                    <h3>Create New User</h3>
                    
                    <div className="form-group">
                        <label>Username</label>
                        <input
                            type="text"
                            value={newUser.username}
                            onChange={(e) => setNewUser({...newUser, username: e.target.value})}
                            placeholder="Enter username"
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label>Password</label>
                        <input
                            type="password"
                            value={newUser.password}
                            onChange={(e) => setNewUser({...newUser, password: e.target.value})}
                            placeholder="Enter password"
                            required
                        />
                    </div>

                    <button type="submit" className="btn-primary">Create User</button>
                </form>
            )}

            <div className="users-list">
                {users.length > 0 ? (
                    <table className="users-table">
                        <thead>
                            <tr>
                                <th>Username</th>
                                <th>Role</th>
                                <th>Created At</th>
                            </tr>
                        </thead>
                        <tbody>
                            {users.map(user => (
                                <tr key={user.id}>
                                    <td>{user.username}</td>
                                    <td>{user.role || 'USER'}</td>
                                    <td>{new Date(user.createdAt).toLocaleDateString()}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                ) : (
                    <p>No users found</p>
                )}
            </div>
        </div>
    );
}
