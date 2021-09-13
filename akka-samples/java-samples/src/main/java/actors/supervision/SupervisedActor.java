package actors.supervision;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class SupervisedActor extends AbstractBehavior<String> {

    public SupervisedActor(ActorContext<String> context) {
        super(context);

        System.out.println("Starting Actor...");

        error();
        System.out.println("Actor started!"); // unreachable
        Behaviors.stopped();
    }

    private void error() throws IllegalStateException {
        throw new IllegalStateException("something went wrong");
    }

    public static Behavior<String> create() {
        return Behaviors.setup(SupervisedActor::new);
    }

    @Override
    public Receive<String> createReceive() {
        return newReceiveBuilder().build();
    }
}
