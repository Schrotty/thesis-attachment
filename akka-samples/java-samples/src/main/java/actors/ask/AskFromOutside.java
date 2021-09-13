package actors.ask;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

public class AskFromOutside extends AbstractBehavior<String> {

    @Override
    public Receive<String> createReceive() {
        return newReceiveBuilder().onMessage(String.class, (message) -> {
            System.out.println(message);

            return Behaviors.stopped();
        }).build();
    }

    public AskFromOutside(ActorContext<String> context) {
        super(context);

        ask(context.getSystem(), context.spawn(GreetingActor.create(), "dummy-greeter"));
    }

    public void ask(ActorSystem<Void> system, ActorRef<GreetingActor.Greet> actorRef) {
        CompletionStage<String> result =
                AskPattern.ask(actorRef, (ActorRef<String> replyTo) ->
                        new GreetingActor.Greet("Danny", replyTo), Duration.ofSeconds(3), system.scheduler());

        result.whenComplete((reply, failure) -> {
           if (failure == null) {
               system.unsafeUpcast().tell(reply);
           } else {
               system.unsafeUpcast().tell(String.format("Something went wrong! (%s)", failure));
           }
        });
    }

    public static Behavior<String> create() {
        return Behaviors.setup(AskFromOutside::new);
    }

    public static void main(String[] args) {
        ActorSystem.create(AskFromOutside.create(), "ask-from-outside");
    }
}
