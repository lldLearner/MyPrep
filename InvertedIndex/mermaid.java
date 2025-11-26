flowchart TD

    Q[Search Query<br/>keywords: k1,k2,k3,k4] --> P[Query Planner]

    P -->|Parallel Fetch| F1[Fetch Posting List k1]
    P -->|Parallel Fetch| F2[Fetch Posting List k2]
    P -->|Parallel Fetch| F3[Fetch Posting List k3]
    P -->|Parallel Fetch| F4[Fetch Posting List k4]

    F1 --> INT1[Intersection Worker 1]
    F2 --> INT1

    F3 --> INT2[Intersection Worker 2]
    F4 --> INT2

    INT1 --> M[Merge Result 1]
    INT2 --> M

    M --> OUT[Final Result Set]


flowchart LR

    Q[Client Query] --> COORD[Coordinator Node]

    COORD --> S1[Shard 1<br/>Bitmap Index]
    COORD --> S2[Shard 2<br/>Bitmap Index]
    COORD --> S3[Shard 3<br/>Bitmap Index]
    COORD --> S4[Shard 4<br/>Bitmap Index]

    S1 --> RES1[Local Bitmap Result 1]
    S2 --> RES2[Local Bitmap Result 2]
    S3 --> RES3[Local Bitmap Result 3]
    S4 --> RES4[Local Bitmap Result 4]

    RES1 --> M[Merge via Bitmap AND]
    RES2 --> M
    RES3 --> M
    RES4 --> M

    M --> OUT[Final Result Set]
