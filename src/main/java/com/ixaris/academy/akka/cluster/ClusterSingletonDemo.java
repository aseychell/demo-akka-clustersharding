package com.ixaris.academy.akka.cluster;

import java.util.Scanner;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageOrBuilder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import com.ixaris.academy.akka.utils.ProtobufHelper;
import com.ixaris.demo.akka.cluster.AkkaClusterDemo.ExampleMessage;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Address;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.singleton.ClusterSingletonManager;
import akka.cluster.singleton.ClusterSingletonManagerSettings;
import akka.cluster.singleton.ClusterSingletonProxy;
import akka.cluster.singleton.ClusterSingletonProxySettings;
import akka.pattern.PatternsCS;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

public class ClusterSingletonDemo {
    
    private static final String ACTOR_SYSTEM_NAME = "cluster-singleton-demo";
    private static final int SEED_PORT = 2552;

    private ClusterSingletonDemo() { }

    public static void main(final String[] args) throws Exception {

        final Config config = ConfigFactory.parseString("cluster.seed-nodes = [\"akka.tcp://" + ACTOR_SYSTEM_NAME + "@127.0.0.1:" + SEED_PORT + "\"]")
            .withFallback(ConfigFactory.load());
        final ActorSystem actorSystem = ActorSystem.create(ACTOR_SYSTEM_NAME, config);
        final Cluster cluster = Cluster.get(actorSystem);
        cluster.join(new Address("akka.tcp", ACTOR_SYSTEM_NAME, "127.0.0.1", SEED_PORT));

        final ClusterSingletonManagerSettings clusterSingletonManagerSettings = ClusterSingletonManagerSettings.create(actorSystem);

        actorSystem.actorOf(
            ClusterSingletonManager.props(
                Props.create(ExampleSingleton.class, () -> new ExampleSingleton()),
                PoisonPill.getInstance(), // Termination message
                clusterSingletonManagerSettings),
            "counterSingleton");

        final ClusterSingletonProxySettings proxySettings = ClusterSingletonProxySettings.create(actorSystem);
        final ActorRef proxy = actorSystem.actorOf(ClusterSingletonProxy.props("/user/counterSingleton", proxySettings), "counterProxy");

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
                if (input.startsWith("msg")) {

                    final ExampleMessage exampleMessage = ExampleMessage.newBuilder().setMessage(parts[1]).build();

                    result = PatternsCS.ask(proxy, exampleMessage, 5000)
                        .toCompletableFuture()
                        .get();

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
