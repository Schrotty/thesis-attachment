package actors.stop;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class Worker extends AbstractBehavior<String> {

    public Worker(ActorContext<String> context, String message) {
        super(context);
        System.out.println(message);
    }

    public static Behavior<String> create(String message) {
        return Behaviors.setup(context -> new Worker(context, message));
    }

    @Override
    public Receive<String> createReceive() {
        return newReceiveBuilder().onMessage(String.class, (message) -> Behaviors.same()).build();
    }
}