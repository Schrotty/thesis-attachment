package actors.ask;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.time.Duration;

public class AskFromActor extends AbstractBehavior<String> {

    @Override
    public Receive<String> createReceive() {
        return newReceiveBuilder().onMessage(String.class, (message) -> {
            System.out.println(message);

            return Behaviors.stopped();
        }).build();
    }

    public AskFromActor(ActorContext<String> context) {
        super(context);

        ActorRef<GreetingActor.Greet> g = context.spawn(GreetingActor.create(), "dummy-greeter");

        final Duration timeout = Duration.ofSeconds(3);
        context.ask(GreetingActor.Greet.class, g, timeout, req ->
                new GreetingActor.Greet("Danny", context.getSelf()), (response, throwable) ->
                    throwable != null && response != null ? response.name : "Failed!");
    }

    public static Behavior<String> create() {
        return Behaviors.setup(AskFromActor::new);
    }

    public static void main(String[] args) {
        ActorSystem.create(AskFromActor.create(), "ask-from-another-actor");
    }
}
