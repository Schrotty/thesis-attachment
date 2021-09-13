package actors.watch;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class Worker extends AbstractBehavior<String> {

    public Worker(ActorContext<String> context) throws InterruptedException {
        super(context);

        Thread.sleep((long)(Math.random() * 1000));
        getContext().getSelf().tell("actors/stop");
    }

    public static Behavior<String> create() {
        return Behaviors.setup(Worker::new);
    }

    @Override
    public Receive<String> createReceive() {
        return newReceiveBuilder().onMessage(String.class, (message) -> Behaviors.stopped()).build();
    }
}