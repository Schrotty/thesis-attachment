package actors.watch;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

class WatchPrinter extends AbstractBehavior<String> {

    @Override
    public Receive<String> createReceive() {
        return newReceiveBuilder().onMessage(String.class, (message) -> {
            System.out.println(message);
            return Behaviors.same();
        }).onSignal(Terminated.class, (ref) -> {
            System.out.println("Done!");
            return Behaviors.same();
        }).build();
    }

    public WatchPrinter(ActorContext<String> context) {
        super(context);

        getContext().watch(getContext().spawnAnonymous(Worker.create()));
        getContext().watchWith(getContext().spawnAnonymous(Worker.create()), "Worker finished!");
    }

    public static Behavior<String> create() {
        return Behaviors.setup(WatchPrinter::new);
    }

    public static void main(String[] args) {
        ActorSystem.create(WatchPrinter.create(), "watch-printer");
    }
}