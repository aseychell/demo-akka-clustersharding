package com.ixaris.academy.akka.sharding;

import static java.util.concurrent.TimeUnit.*;

import java.util.LinkedList;
import java.util.List;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageOrBuilder;
import com.ixaris.demo.akkasharding.AkkaShardingDemo;
import com.ixaris.demo.akkasharding.AkkaShardingDemo.CounterChangedEvent;
import com.ixaris.demo.akkasharding.AkkaShardingDemo.CounterOp;
import com.ixaris.demo.akkasharding.AkkaShardingDemo.CounterOp.Type;
import com.ixaris.demo.akkasharding.AkkaShardingDemo.GetCounter;

import akka.actor.AbstractLoggingActor;
import akka.actor.PoisonPill;
import akka.actor.ReceiveTimeout;
import akka.cluster.sharding.ShardRegion;
import scala.PartialFunction;
import scala.concurrent.duration.Duration;
import scala.runtime.BoxedUnit;

public class Counter extends AbstractLoggingActor {

    private Long id;
    private int count = 0;
    private final List<CounterChangedEvent> journal = new LinkedList<>();

    @Override
    public void preStart() throws Exception {
        super.preStart();
        context().setReceiveTimeout(Duration.create(120, SECONDS));
        this.id = Long.valueOf(getSelf().path().name());
    }

    @Override
    public void aroundReceive(final PartialFunction<Object, BoxedUnit> receive, final Object msg) {
        if (msg instanceof MessageOrBuilder) {
            try {
                log().info("Received: " + ProtobufHelper.toJson((MessageOrBuilder) msg));
            } catch (final InvalidProtocolBufferException e) {
                log().warning("Invalid message", e);
            }
        } else {
            log().info("Received message: " + msg);
        }
        super.aroundReceive(receive, msg);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(GetCounter.class, getCounter -> {
                sender().tell(AkkaShardingDemo.Counter.newBuilder().setId(id).setValue(count).build(), self());
            })
            .match(CounterOp.class, op -> op.getType() == Type.INCREMENT, counterOp -> {
                final CounterChangedEvent event = CounterChangedEvent.newBuilder().setDelta(+1).build();
                journal.add(event);
                updateState(event);
                sender().tell("ok", self());
            })
            .match(CounterOp.class, op -> op.getType() == Type.DECREMENT, counterOp -> {
                final CounterChangedEvent event = CounterChangedEvent.newBuilder().setDelta(-1).build();
                journal.add(event);
                updateState(event);
                sender().tell("ok", self());
            })
            .match(ReceiveTimeout.class, receiveTimeout -> {
                getContext().parent().tell(new ShardRegion.Passivate(PoisonPill.getInstance()), getSelf());
            })
            .matchAny(this::unhandled)
            .build();
    }

    private void updateState(final CounterChangedEvent event) {
        count += event.getDelta();
    }

}
