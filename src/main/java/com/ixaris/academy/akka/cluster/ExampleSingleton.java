package com.ixaris.academy.akka.cluster;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageOrBuilder;

import com.ixaris.academy.akka.utils.ProtobufHelper;
import com.ixaris.demo.akka.cluster.AkkaClusterDemo.Ack;
import com.ixaris.demo.akka.cluster.AkkaClusterDemo.ExampleMessage;

import akka.actor.AbstractActor;
import akka.actor.AbstractActorWithTimers;
import scala.PartialFunction;
import scala.concurrent.duration.Duration;
import scala.runtime.BoxedUnit;

public class ExampleSingleton extends AbstractActorWithTimers {

    private static final Logger LOG = LoggerFactory.getLogger(ExampleSingleton.class);
    private static Object TICK_KEY = "TickKey";
    private static final class Tick { }

    public ExampleSingleton() {
//        getTimers().startPeriodicTimer(TICK_KEY, new Tick(),
//                                        Duration.create(1000, TimeUnit.MILLISECONDS));
    }

    @Override
    public void preStart() throws Exception {
        System.out.println("\n\n\n\nStarting singleton!! self = " + getSelf() + "\n\n\n\n");
    }

//    @Override
//    public void aroundReceive(final PartialFunction<Object, BoxedUnit> receive, final Object msg) {
//        if (msg instanceof MessageOrBuilder) {
//            try {
//                LOG.info("Received: {}", ProtobufHelper.toJson((MessageOrBuilder) msg));
//            } catch (final InvalidProtocolBufferException e) {
//                LOG.warn("Invalid message", e);
//            }
//        } else {
//            LOG.info("Received message: {}", msg);
//        }
//        super.aroundReceive(receive, msg);
//    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(ExampleMessage.class, m -> {
                System.out.println("\n\n\n\nReceived Message " + m.getMessage() + "\n\n\n\n");
                getSender().tell(Ack.getDefaultInstance(), getSelf());
            })
            .match(Tick.class, m -> {
                System.out.println("\n\n\n\nTicking!! self = " + getSelf() + "\n\n\n\n");
            })
            .matchAny(((AbstractActor)this)::unhandled)
            .build();
    }


}
