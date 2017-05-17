package com.ixaris.academy.akka.sharding;

import java.util.Scanner;

import com.ixaris.demo.akkasharding.AkkaShardingDemo.CounterOp;
import com.ixaris.demo.akkasharding.AkkaShardingDemo.CounterOp.Type;
import com.ixaris.demo.akkasharding.AkkaShardingDemo.GetCounter;

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

public class ShardingDemoApplication {

    public static final String ACTOR_SYSTEM_NAME = "sharding-demo";

    public static void main(final String[] args) throws Exception {
        
        final String counterShardRegion = "Counter";

        final ActorSystem actorSystem = ActorSystem.create(ACTOR_SYSTEM_NAME);
        final Cluster cluster = Cluster.get(actorSystem);
        cluster.join(new Address("akka.tcp", ACTOR_SYSTEM_NAME, "127.0.0.1", 2552));
        
        final ClusterShardingSettings settings = ClusterShardingSettings.create(actorSystem);
        
        final ClusterSharding clusterSharding = ClusterSharding.get(actorSystem);
        final ShardRegion.MessageExtractor messageExtractor = new CounterMessageExtractor();
        final ActorRef counterShardRegionRef = clusterSharding.start(counterShardRegion, Props.create(Counter.class), settings, messageExtractor);

        if (!System.getenv().containsKey("BIND_PORT")) {
            actorSystem.actorOf(Props.create(TestCounterActor.class, () -> new TestCounterActor(counterShardRegionRef)));
            Thread.sleep(5000L);
        }

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {

                System.out.println("Please enter your next command:");
                final String input = scanner.nextLine();

                if (input.startsWith("quit")) {
                    break;
                }

                final String[] parts = input.split(" ");
                if (input.startsWith("inc")) {
                    final long counterId = Long.parseLong(parts[1]);

                    final Object result = PatternsCS.ask(counterShardRegionRef, CounterOp.newBuilder().setId(counterId).setType(Type.INCREMENT).build(), 5000)
                        .toCompletableFuture()
                        .get();

                    System.out.println("Response: " + result);
                } else if (input.startsWith("dec")) {
                    final long counterId = Long.parseLong(parts[1]);

                    final Object result = PatternsCS.ask(counterShardRegionRef, CounterOp.newBuilder().setId(counterId).setType(Type.DECREMENT).build(), 5000)
                        .toCompletableFuture()
                        .get();

                    System.out.println("Response: " + result);
                } else if (input.startsWith("get")) {
                    final long counterId = Long.parseLong(parts[1]);

                    final Object result = PatternsCS.ask(counterShardRegionRef, GetCounter.newBuilder().setId(counterId).build(), 5000).toCompletableFuture().get();

                    System.out.println("Response: " + result);
                }
            }
        }

        System.out.println("Terminating actor system");
        actorSystem.terminate();
        Await.result(actorSystem.whenTerminated(), Duration.Inf());
    }
    
}
