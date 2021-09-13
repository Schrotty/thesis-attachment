package actors.mailbox;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

class Worker extends AbstractBehavior<String> {

    private int messages = 0;
    private final ActorRef<String> actorRef;

    public Worker(ActorContext<String> context, ActorRef<String> actorRef) {
        super(context);
        this.actorRef = actorRef;
    }

    public static Behavior<String> create(ActorRef<String> actorRef) {
        return Behaviors.setup(context -> new Worker(context, actorRef));
    }

    @Override
    public Receive<String> createReceive() {
        return newReceiveBuilder().onMessage(String.class, (message) -> {
            messages++;
            return Behaviors.same();
        }).onSignal(PostStop.class, signal -> {
            actorRef.tell(String.format("[%s] Received messages: %d", getContext().getSelf().path().toString(), messages));
            return Behaviors.same();
        }).build();
    }
}