package com.ixaris.academy.akka.sharding;

import java.util.Scanner;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageOrBuilder;

import com.ixaris.academy.akka.utils.ProtobufHelper;
import com.ixaris.demo.akkasharding.AkkaShardingDemo.CounterOp;
import com.ixaris.demo.akkasharding.AkkaShardingDemo.CounterOp.Type;
import com.ixaris.demo.akkasharding.AkkaShardingDemo.GetCounter;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Address;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.sharding.ClusterSharding;
import akka.cluster.sharding.ClusterShardingSettings;
import akka.cluster.sharding.ShardRegion;
import akka.pattern.PatternsCS;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

public class ShardingDemo {
    
    public static final String ACTOR_SYSTEM_NAME = "sharding-demo";
    public static final int SEED_PORT = 2552;

    public static void main(final String[] args) throws Exception {

        final Config config = ConfigFactory.parseString("cluster.seed-nodes = [\"akka.tcp://" + ACTOR_SYSTEM_NAME + "@127.0.0.1:" + SEED_PORT + "\"]")
            .withFallback(ConfigFactory.load());
        final ActorSystem actorSystem = ActorSystem.create(ACTOR_SYSTEM_NAME, config);
        final Cluster cluster = Cluster.get(actorSystem);
        cluster.join(new Address("akka.tcp", ACTOR_SYSTEM_NAME, "127.0.0.1", SEED_PORT));
        
        final ClusterShardingSettings settings = ClusterShardingSettings.create(actorSystem);
        
        final ClusterSharding clusterSharding = ClusterSharding.get(actorSystem);
        final ShardRegion.MessageExtractor messageExtractor = new CounterMessageExtractor();
        final String counterShardRegion = "Counter";
        final ActorRef counterShardRegionRef = clusterSharding.start(counterShardRegion, Props.create(Counter.class), settings, messageExtractor);

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                
                System.out.println();
                System.out.println("Please enter your next command:");
                final String input = scanner.nextLine();
                
                if (input.startsWith("quit")) {
                    break;
                }
                
                final String[] parts = input.split(" ");
                final Object result;
                if (input.startsWith("inc")) {
                    final long counterId = Long.parseLong(parts[1]);
                    
                    result = PatternsCS.ask(counterShardRegionRef, CounterOp.newBuilder().setId(counterId).setType(Type.INCREMENT).build(), 5000)
                        .toCompletableFuture()
                        .get();
                    
                } else if (input.startsWith("dec")) {
                    final long counterId = Long.parseLong(parts[1]);
                    
                    result = PatternsCS.ask(counterShardRegionRef, CounterOp.newBuilder().setId(counterId).setType(Type.DECREMENT).build(), 5000)
                        .toCompletableFuture()
                        .get();
                    
                } else if (input.startsWith("get")) {
                    final long counterId = Long.parseLong(parts[1]);
                    
                    result = PatternsCS.ask(counterShardRegionRef, GetCounter.newBuilder().setId(counterId).build(), 5000).toCompletableFuture().get();
                } else if (input.startsWith("testData")) {

                    final ActorRef testActor = actorSystem.actorOf(Props.create(TestCounterActor.class, () -> new TestCounterActor(counterShardRegionRef)));

                    while (!testActor.isTerminated()) {
                        Thread.sleep(500L);
                    }

                    result = "DONE";
                } else {
                    System.out.println("Unknown input!  Use commands of the form inc|dec|get <counter_id>");
                    continue;
                }

                System.out.println();
                System.out.println();
                System.out.println();
                System.out.println("Response: " + pretifyResult(result));
                System.out.println();
                System.out.println();
                System.out.println();
            }
        }
        
        System.out.println("Terminating actor system");
        actorSystem.terminate();
        Await.result(actorSystem.whenTerminated(), Duration.Inf());
    }

    private static String pretifyResult(final Object result) {
        if (result instanceof MessageOrBuilder) {
            try {
                return ProtobufHelper.toJson((MessageOrBuilder) result);
            } catch (final InvalidProtocolBufferException e) {
                return result.toString();
            }
        } else {
            return result.toString();
        }
    }

}
