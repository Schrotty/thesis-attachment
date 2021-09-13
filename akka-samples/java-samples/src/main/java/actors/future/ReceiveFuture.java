package actors.future;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.concurrent.*;

public class ReceiveFuture extends AbstractBehavior<Integer> {
    public CompletableFuture<Integer> calculate() {
        return CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(3 * 1000); }
            catch(InterruptedException ignored) {}
            return 42;
        });
    }

    public ReceiveFuture(ActorContext<Integer> context) {
        super(context);

        System.out.println("Starting...");
        context.pipeToSelf(calculate(), (o, e) -> e == null ? o : -1);
    }

    public static Behavior<Integer> create() {
        return Behaviors.setup(ReceiveFuture::new);
    }

    public static void main(String[] args) {
        ActorSystem.create(ReceiveFuture.create(), "receive-future");
    }

    @Override
    public Receive<Integer> createReceive() {
        return newReceiveBuilder().onMessage(Integer.class, (message) -> {
            System.out.println(message);
            return Behaviors.stopped();
        }).build();
    }
}
