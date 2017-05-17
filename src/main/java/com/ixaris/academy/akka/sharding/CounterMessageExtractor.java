package com.ixaris.academy.akka.sharding;


import com.ixaris.demo.akkasharding.AkkaShardingDemo.CounterOp;
import com.ixaris.demo.akkasharding.AkkaShardingDemo.GetCounter;

import akka.cluster.sharding.ShardRegion;

@SuppressWarnings("OverlyStrongTypeCast")
public class CounterMessageExtractor implements ShardRegion.MessageExtractor {
    
    @Override
    public String entityId(final Object message) {
        if (message instanceof GetCounter) {
            return String.valueOf(((GetCounter) message).getId());
        } else if (message instanceof CounterOp) {
            return String.valueOf(((CounterOp) message).getId());
        } else {
            return null;
        }
    }
    
    @Override
    // Used for Unwrapping .. only if needed. This is what is sent to the entity actor
    public Object entityMessage(final Object message) {
        return message;
    }
    
    @Override
    public String shardId(final Object message) {
        final int numberOfShards = 100;
        if (message instanceof GetCounter) {
            final long id = ((GetCounter) message).getId();
            return String.valueOf(id % numberOfShards);
        } else if (message instanceof CounterOp) {
            final long id = ((CounterOp) message).getId();
            return String.valueOf(id % numberOfShards);
        } else {
            return null;
        }
    }
}
