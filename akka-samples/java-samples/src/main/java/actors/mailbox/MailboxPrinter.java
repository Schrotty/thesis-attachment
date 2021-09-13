package actors.mailbox;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.MailboxSelector;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.stream.IntStream;

class MailboxPrinter extends AbstractBehavior<String> {

    private final ActorRef<String> limitedWorker;
    private final ActorRef<String> unlimitedWorker;

    @Override
    public Receive<String> createReceive() {
        return newReceiveBuilder().onMessage(String.class, (message) -> {
            System.out.println(message);
            return Behaviors.same();
        }).build();
    }

    public MailboxPrinter(ActorContext<String> context) {
        super(context);

        limitedWorker = getContext().spawn(Worker.create(getContext().getSelf()), "limited-worker", MailboxSelector.bounded(5));
        unlimitedWorker = getContext().spawn(Worker.create(getContext().getSelf()), "unlimited-worker");

        IntStream.range(0, 512).forEach(i -> {
            limitedWorker.tell("");
            unlimitedWorker.tell("");
        });

        getContext().stop(limitedWorker);
        getContext().stop(unlimitedWorker);
    }

    public static Behavior<String> create() {
        return Behaviors.setup(MailboxPrinter::new);
    }

    public static void main(String[] args) {
        ActorSystem.create(MailboxPrinter.create(), "mailbox-printer");
    }
}