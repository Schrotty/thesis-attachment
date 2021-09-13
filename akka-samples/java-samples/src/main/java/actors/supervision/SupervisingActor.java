package actors.supervision;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.time.Duration;

public class SupervisingActor extends AbstractBehavior<String> {

    public SupervisingActor(ActorContext<String> context) {
        super(context);
    }

    public static Behavior<String> create() {
        return Behaviors.supervise(SupervisedActor.create())
                .onFailure(IllegalStateException.class, SupervisorStrategy.restart().withLimit(3, Duration.ofSeconds(3)));
    }

    public static void main(String[] args) {
        ActorSystem.create(SupervisingActor.create(), "supervision");
    }

    @Override
    public Receive<String> createReceive() {
        return newReceiveBuilder().build();
    }
}
