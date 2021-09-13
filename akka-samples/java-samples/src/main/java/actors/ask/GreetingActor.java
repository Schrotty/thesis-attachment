package actors.ask;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class GreetingActor extends AbstractBehavior<GreetingActor.Greet> {
    static final class Greet {
        public String name;
        public ActorRef<String> replyTo;

        Greet(String name, ActorRef<String> replyTo) {
            this.name = name;
            this.replyTo = replyTo;
        }
    }

    public GreetingActor(ActorContext<GreetingActor.Greet> context) {
        super(context);
    }

    public static Behavior<GreetingActor.Greet> create() {
        return Behaviors.setup(GreetingActor::new);
    }

    @Override
    public Receive<GreetingActor.Greet> createReceive() {
        return newReceiveBuilder().onMessage(Greet.class, (message) -> {
            message.replyTo.tell(String.format("Oh Hi %s!", message.name));
            return Behaviors.stopped();
        }).build();
    }
}
