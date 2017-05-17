package com.ixaris.academy.akka.sharding;

import java.util.Random;

import com.ixaris.demo.akkasharding.AkkaShardingDemo.CounterOp;
import com.ixaris.demo.akkasharding.AkkaShardingDemo.CounterOp.Type;
import com.ixaris.demo.akkasharding.AkkaShardingDemo.GetCounter;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;

public class TestCounterActor extends AbstractActor {

    private final ActorRef counterShardRegion;

    public TestCounterActor(final ActorRef shardRegion) {
        this.counterShardRegion = shardRegion;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().matchAny(o -> {
            System.out.println("Received Message " + o);
        }).build();
    }

    Random rand = new Random();

    @Override
    public void preStart() throws Exception {

        for (int i = 0; i < 1000; i++) {
            final int counterId = rand.nextInt(10);
            switch (rand.nextInt(3)) {
                case 0:
                    counterShardRegion.tell(GetCounter.newBuilder().setId(counterId).build(), self());
                    break;
                case 1:
                    counterShardRegion.tell(CounterOp.newBuilder().setId(counterId).setType(Type.INCREMENT).build(), self());
                    break;
                case 2:
                    counterShardRegion.tell(CounterOp.newBuilder().setId(counterId).setType(Type.DECREMENT).build(), self());
                    break;
            }
        }

        for (int i = 0; i < 10; i++) {
            counterShardRegion.tell(GetCounter.newBuilder().setId(i).build(), self());
        }

        context().stop(self());
    }

}
