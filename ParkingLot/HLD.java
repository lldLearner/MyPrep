✅ FOLLOW-UP 3: What changes would you make to productionalize and scale this solution?

This is the most important answer in Uber LLD.

Break it into 3 parts:
(A) Architecture changes
(B) Data & persistence
(C) Reliability, monitoring & scaling

⭐ (A) Architecture Upgrade for Production
1. Replace in-memory structures with a persistent DB

Use PostgreSQL or DynamoDB

Tables:

ParkingSpot (spotId, type, level, status)

Tickets (ticketId, vehiclePlate, spots, entryTime, exitTime)

ParkingLotConfig (levels, spots per level)

2. Introduce a ParkingService (REST API)

/park

/unpark

/find/{plate}

/status

3. Add Distributed Locking (to avoid double-booking)

Options:

Redis Redlock

Database row-level locks

Level-wise lock objects

4. Event-driven architecture

Publish VehicleParked and VehicleUnparked events

Downstream: billing service, analytics service, notifications

⭐ (B) Scalability Enhancements
1. Shard by level

Each level is an independent unit

Each can be handled by a separate microservice instance

Reduces contention

2. Use caching for availability

Cache free spots count per level in Redis

Fast reads for availability page

Write-through/invalidate on park/unpark

3. Optimize spot search

Use segment tree or ordered map to find consecutive free spots faster

This reduces bus search from O(N) → O(log N)

⭐ (C) Production Readiness: Monitoring & Reliability
1. Metrics

Occupancy per level

Average parking time

Average park() latency

Spot allocation failures

2. Alerts

If park latency > 200ms

If occupancy > 95% for 10 minutes

If double-booking detected

3. Logging

Structured logging (vehicle plate, level, spot list, timestamp)

Correlation IDs for each request

4. Failover

Service runs in multiple AZs

DB with multi-AZ replication

⭐ (D) Billing + Extensions

Add pricing engine (hourly/flat)

Add EV charging spots

Add handicapped priority

Add reservation API (“Hold a spot for 15 min”)
