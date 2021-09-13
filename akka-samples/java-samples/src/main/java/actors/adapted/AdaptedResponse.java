package actors.adapted;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.stream.IntStream;

public class AdaptedResponse extends AbstractBehavior<String> {

    @Override
    public Receive<String> createReceive() {
        return newReceiveBuilder().onSignal(Terminated.class, (message) -> Behaviors.stopped()).build();
    }

    public AdaptedResponse(ActorContext<String> context) {
        super(context);

        ActorRef<Manager.Command> manager = context.spawn(Manager.create(), "job-manager");
        context.watch(manager);

        IntStream.range(1, 11).asDoubleStream().forEach(task -> manager.tell(new Manager.Calculate(task)));
    }

    public static Behavior<String> create() {
        return Behaviors.setup(AdaptedResponse::new);
    }

    public static void main(String[] args) {
        ActorSystem.create(AdaptedResponse.create(), "adapted-response");
    }
}