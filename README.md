# Demo using Akka Cluster Sharding

This demo is modeled around number counters, each represented as a different actor with it's own state and ID.  The counters are sharded and distributed over the cluster.

This demo also makes use of google protocol buffers (protobuf) to serialize messages between actors.


# Building

1. Make sure you have maven install.
2. Execute `mvn clean install`

# Running

Use the scripts `start-seed.bat` and `start-normal-node.bat` in that order to start nodes. (Feel free to contribute a UNIX version of these files)

After startup is complete, you can use the following lines in the command line:

- `inc <counter_id>` to increment a counter with the specified id
- `dec <counter_id>` to decrement a counter with the specified id
- `get <counter_id>` to query a counter with the specified id
- `testData` to issue a number of random requests on random counters
