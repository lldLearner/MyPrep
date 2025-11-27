Part I — Database basics + Sharding (what to say)
1. Schema & primary key (quick)

Table = collection of rows (records).

Row = sequence of columns (fields).

Column = typed field (int, varchar, uuid, blob...).

Primary key = minimal set of columns that uniquely identify a row. Could be single column (id) or composite (user_id, order_id).

Primary keys are used for lookups, indexing, and sharding decisions.

Interview line: “A primary key identifies a single row; for distributed storage we use the primary key to compute a shard key so rows for the same key go to the same machine.”

2. How to map a row → host (sharding strategies)

High level: produce a shard identifier from the row -> choose node.

A. Hash-based sharding (most common)

Compute hash(primaryKeyBytes) % N → node id.

Pros: easy uniform distribution, simple rebalancing with consistent hashing.

Cons: range queries across shard key hard; rebalancing can cause movement (mitigate with consistent hashing / virtual nodes).

B. Range-based sharding

Use a sortable key (e.g., composite values concatenated in order) and place contiguous ranges on nodes.

Pros: range scans are cheap and often localized.

Cons: hot-keys/ranges cause imbalance; rebalancing tricky.

C. Composite / hybrid

Use high-cardinality prefix for range, then hash suffix; or use consistent hashing on composite key.

D. Routing / directory service

A small metadata service maps ranges/hashes → nodes (e.g., metadata table or configuration service). Clients or routers consult it.

3. Where codec fits

The codec turns a composite primary key (heterogeneous typed values) into a single stable rowKey (byte[] or string).

That rowKey is used by hashing or range mapping.

Important properties for the rowKey:

Deterministic & reversible (decodable to original parts).

Preserve sort-order if range-sharding is used (encoding must be lexicographically comparable to original ordering).

Compact and collision-free (not required to be collision-free if hashed later, but avoid accidental collisions if used as identifier).

Efficient to encode/decode.

Part II — Codec design (options, chosen approach, tradeoffs)
Goals for a good codec

encode(pkParts) -> byte[] rowKey

decode(rowKey) -> pkParts

Support types: int32, int64, UUID, string, byte[] (and extensible)

Optionally produce shardKey (e.g., hashed value) for routing

Support lexicographic ordering if needed (for range sharding)

2 main encoding approaches (pros/cons)
A — Length-prefixed binary encoding (recommended)

Format per field:
[typeId(1 byte)] [len(varint or 4B)] [raw bytes]
Concatenate fields.

Pros:

No escaping, exact decoding.

Efficient and compact.

Can include type markers → safe parsing.

Easy to support binary types and preserve order with type-specific normalization.

Cons:

Slight binary complexity (but straightforward).

B — Delimiter + escape encoding (textual)

Format: field1 + DELIM + field2 + DELIM ..., with DELIM escaped inside payload.

Pros:

Human readable if using strings.

Simple conceptually.

Cons:

Must escape delimiter (error-prone).

Less efficient for binary/UUID.

Hard to guarantee lexicographic order for numeric types.

C — Fixed-width / order-preserving numeric encoding

If you need lexicographic ordering equal to numeric ordering, encode numbers in big-endian fixed-width or use zigzag + varint with order-fix. For signed ints, offset by bias or use two’s complement order-preserving conversion.

Extra: If you need range sharding (lexicographic compare), you must encode numeric types so their binary byte order equals natural order:

For unsigned integers: big-endian fixed width works.

For signed integers: add a fixed offset (bias) so values map to unsigned space preserving order or use order-preserving variable-length encoding.

Strings: normalized (UTF-8) — lexicographic order works.

Implementation plan I’ll show you

Java class RowKeyCodec with:

byte[] encode(Object... parts)

List<Object> decode(byte[] rowKey) with type markers

Helper encoders for: INT32, INT64, UUID, STRING, BYTES

long shardFromKey(byte[] rowKey) using e.g. Murmur3 or SHA-256 truncated (we’ll use built-in MurmurHash3-like via MessageDigest SHA-256 for portability; or xxhash if available)

Optionally a method encodeForRangeSharding(...) which encodes ints in big-endian fixed-width for ordering.

I’ll give complete Java code you can paste & run.
