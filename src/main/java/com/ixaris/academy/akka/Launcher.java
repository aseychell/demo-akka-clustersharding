package com.ixaris.academy.akka;

import com.ixaris.academy.akka.cluster.ClusterSingletonDemo;
import com.ixaris.academy.akka.sharding.ShardingDemo;

/**
 * @author <a href="mailto:aldrin.seychell@ixaris.com">aldrin.seychell</a>
 */
public class Launcher {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Please provide mode as an argument e.g. `java -jar akka-demo sharding`");
            System.exit(404);
        }
        switch (args[0]) {
            case "sharding":
                ShardingDemo.main(args);
                break;
            case "cluster" :
                ClusterSingletonDemo.main(args);
                break;
            default:
                System.out.printf("Mode [%s] not supported", args[0]);
                System.exit(400);
        }
    }
}
