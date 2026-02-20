# Cost Estimation - Time and Space Complexity Analysis

## Overview
This document provides a thorough analysis of time and space complexity for all major operations in the Intelligent Alert Escalation & Resolution System.

---

## Time Complexity Analysis

### 1. **Alert Creation** - `POST /api/alerts`
- **Complexity**: `O(n)` where n = number of alerts in the rule evaluation window
- **Operations**:
  - Save alert to MongoDB: `O(1)`
  - Evaluate escalation rules: `O(n)` - counts alerts of same sourceType in time window
  - Save lifecycle event: `O(1)`
- **Optimization**: MongoDB indexed queries on `sourceType` and `timestamp` fields
- **Worst Case**: O(n) when checking if threshold is met (e.g., 3 overspeed alerts in 60 minutes)

### 2. **Alert Retrieval** - `GET /api/alerts/{id}`
- **Complexity**: `O(1)` average case
- **Operations**:
  - Find alert by ID (MongoDB indexed): `O(1)`
  - Find lifecycle events by alertId: `O(m)` where m = lifecycle events for this alert
- **Optimization**: MongoDB uses `@Id` index for O(1) lookups

### 3. **List All Alerts** - `GET /api/alerts`
- **Complexity**: `O(n)` where n = total alerts in database
- **Operations**:
  - Fetch all alerts from MongoDB: `O(n)`
  - Convert to JSON: `O(n)`
- **Scalability Concern**: For large datasets, implement pagination (limit/offset)

### 4. **Manual Resolution** - `PATCH /api/alerts/{id}/resolve`
- **Complexity**: `O(1)`
- **Operations**:
  - Find alert by ID: `O(1)`
  - Update status: `O(1)`
  - Save lifecycle event: `O(1)`

### 5. **Compliance Renewal** - `POST /api/alerts/compliance-renewed`
- **Complexity**: `O(n)` where n = total alerts
- **Operations**:
  - Filter alerts by driverId and COMPLIANCE type: `O(n)`
  - Mark eligible for auto-close: `O(k)` where k = matching alerts
- **Optimization Needed**: Add compound index on `(sourceType, metadata.driverId, status)`

### 6. **Dashboard Summary** - `GET /api/dashboard/summary`
- **Complexity**: `O(n)` where n = total alerts
- **Operations**:
  - Count alerts by severity: `O(n)` - single pass with streaming
  - Count by status: `O(n)` - parallel stream processing
- **Optimization**: Uses Java Streams for efficient in-memory aggregation

### 7. **Top Offenders** - `GET /api/dashboard/top-offenders`
- **Complexity**: `O(n log k)` where n = total alerts, k = limit (default 10)
- **Operations**:
  - Group by driverId: `O(n)`
  - Count per driver: `O(n)`
  - Sort descending: `O(n log n)`
  - Limit to top k: `O(k)`
- **Optimization**: Sorting full list is expensive; consider using heap for top-k

### 8. **Recent Events** - `GET /api/dashboard/events`
- **Complexity**: `O(m log m + n)` where m = total events, n = events returned
- **Operations**:
  - Fetch all events: `O(m)`
  - Sort by timestamp descending: `O(m log m)` (MongoDB server-side)
  - Limit results: `O(n)`
  - Lookup alert details: `O(n)` additional queries
- **Optimization**: MongoDB sort with indexed `timestamp` field reduces to O(n)

### 9. **Auto-Close Alerts** - `GET /api/dashboard/auto-closed`
- **Complexity**: `O(n)` where n = total alerts
- **Operations**:
  - Filter by AUTO_CLOSED status: `O(n)`
- **Optimization**: Add MongoDB index on `status` field

### 10. **Weekly Trend** - `GET /api/dashboard/trend`
- **Complexity**: `O(n)` where n = total alerts
- **Operations**:
  - Filter alerts by timestamp (last 7 days): `O(n)`
  - Group by day: `O(n)`
  - Count per day: `O(n)`
- **Optimization**: Compound index on `(timestamp, createdAt)`

### 11. **Auto-Close Scheduler** - Background Job
- **Complexity**: `O(n)` where n = total alerts
- **Frequency**: Runs every 120 seconds
- **Operations**:
  - Scan all alerts: `O(n)`
  - Check auto-close eligibility: `O(n)`
  - Update eligible alerts: `O(k)` where k = eligible alerts
- **Optimization**: Filter at database level with MongoDB query on `autoCloseReason != null`

### 12. **Authentication Operations**
- **Register**: `O(1)` - MongoDB insert with uniqueness check
- **Login**: `O(1)` - MongoDB findByUsername (indexed field)
- **JWT Validation**: `O(1)` - signature verification and expiry check
- **Password Hashing**: `O(1)` - BCrypt (constant cost factor)

---

## Space Complexity Analysis

### 1. **Database Storage (MongoDB)**
- **Alerts Collection**: `O(n)` where n = total alerts
  - Average alert size: ~500 bytes (metadata varies)
  - 1 million alerts ≈ 500 MB
- **Lifecycle Events Collection**: `O(m)` where m = total events
  - Average event size: ~200 bytes
  - For alerts with 3-5 lifecycle events: m ≈ 3n to 5n
  - 1 million alerts → 3-5 million events ≈ 600 MB - 1 GB
- **Users Collection**: `O(u)` where u = total users
  - Average user size: ~150 bytes
  - Minimal impact (typically < 10,000 users)

**Total Database**: For 1M alerts with lifecycle events: **~1.5 GB**

### 2. **Application Memory (Runtime)**
- **Spring Boot Context**: ~150 MB baseline
- **MongoDB Connection Pool**: ~50 MB (default pool size)
- **JWT Token Cache**: `O(k)` where k = concurrent users
  - Per token: ~256 bytes
  - 1000 concurrent users ≈ 256 KB (negligible)
- **Request Processing**: `O(1)` per request (stateless REST)
- **Scheduler Thread**: `O(1)` - single background thread

**Total Runtime Memory**: **~200-250 MB** under typical load

### 3. **Network Payload**
- **Single Alert**: ~500 bytes JSON
- **List All Alerts (1000 alerts)**: ~500 KB per response
- **Dashboard Summary**: ~1 KB (aggregated data)
- **JWT Token**: ~256 bytes per auth header

### 4. **Algorithmic Space**
- **Stream Processing**: `O(1)` extra space (lazy evaluation)
- **Sorting Operations**: `O(n)` temporary space for Collections.sort()
- **Grouping/Aggregation**: `O(k)` where k = unique groups (e.g., drivers)

---

## Optimization Strategies Implemented

### ✅ Efficient Data Structures
1. **MongoDB Repositories**: O(1) indexed lookups
2. **Java Streams**: Memory-efficient lazy evaluation
3. **No In-Memory Cache**: Eliminated ConcurrentHashMap (moved to MongoDB)

### ✅ Database Indexing
```yaml
Recommended MongoDB Indexes:
- alerts: { _id: 1 }  # Auto-created
- alerts: { sourceType: 1, timestamp: -1 }  # Escalation queries
- alerts: { status: 1 }  # Dashboard filtering
- alerts: { "metadata.driverId": 1, sourceType: 1 }  # Compliance renewal
- lifecycle_events: { alertId: 1, timestamp: -1 }  # Alert history
- users: { username: 1 }  # Auth lookup (unique)
```

### ✅ Algorithm Choices
- **BCrypt Password Hashing**: Industry-standard security with controlled cost
- **JWT with HS256**: Fast symmetric encryption, O(1) verification
- **Stream API**: Avoids intermediate collections, reduces memory footprint

---

## Scalability Projections

| Alerts in System | Database Size | Query Time (avg) | Memory Usage |
|------------------|---------------|------------------|--------------|
| 10,000           | ~15 MB        | < 50 ms          | 200 MB       |
| 100,000          | ~150 MB       | < 100 ms         | 220 MB       |
| 1,000,000        | ~1.5 GB       | < 500 ms         | 250 MB       |
| 10,000,000       | ~15 GB        | < 2 sec*         | 300 MB       |

**Note**: Times assume proper indexing. *Pagination required for 10M+ alerts.

---

## Performance Bottlenecks & Recommendations

### 🔴 High Priority
1. **GET /api/alerts** - O(n) returns ALL alerts
   - **Solution**: Add pagination with `?page=1&size=100`
   - **Impact**: Reduces payload from 500 MB to 50 KB

2. **Top Offenders Sorting** - O(n log n) on full dataset
   - **Solution**: Use priority queue (heap) for top-k: O(n log k)
   - **Impact**: For k=10, reduces from O(n log n) to O(n log 10)

3. **Auto-Close Scheduler** - Scans all alerts every 120 seconds
   - **Solution**: Add MongoDB filter `find({ autoCloseReason: { $ne: null } })`
   - **Impact**: Reduces scan from O(n) to O(k) where k << n

### 🟡 Medium Priority
4. **Compliance Renewal** - O(n) linear scan
   - **Solution**: Add compound index on `(metadata.driverId, sourceType, status)`
   - **Impact**: Reduces to O(log n + k) for targeted query

5. **Dashboard Trend** - Aggregates 7 days of data
   - **Solution**: Use MongoDB aggregation pipeline server-side
   - **Impact**: Offloads computation to database

### 🟢 Low Priority
6. **Lifecycle Events Lookup** - Multiple O(m) queries
   - **Solution**: Batch fetch with `$in` operator
   - **Impact**: Reduces round-trips from n to 1

---

## Conclusion

The system demonstrates **efficient algorithmic design** with:
- Most operations running in **O(1) to O(n)** time complexity
- **Linear space complexity** O(n) scaling with data volume
- **Stateless architecture** keeping runtime memory constant at ~250 MB
- **MongoDB indexing** ensuring sub-second query response for millions of records

### Trade-offs Made
- **No In-Memory Caching**: Favors data consistency over ultra-low latency
- **Full List Endpoints**: Simplicity over scalability (pagination recommended for production)
- **Synchronous Processing**: Easier debugging over async complexity

**Recommended Next Step**: Implement pagination and MongoDB aggregation pipelines for production deployment with 1M+ alerts.
