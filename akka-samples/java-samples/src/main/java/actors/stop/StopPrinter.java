package actors.stop;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class StopPrinter extends AbstractBehavior<StopPrinter.PrintMessage> {
    static final class PrintMessage {
        public final String text;

        public PrintMessage(String text) {
            this.text = text;
        }
    }

    @Override
    public Receive<PrintMessage> createReceive() {
        return newReceiveBuilder().onMessage(PrintMessage.class, (message) -> {
            ActorRef<String> actorRef = getContext().spawn(Worker.create(message.text), "printer-worker");
            getContext().stop(actorRef);

            return Behaviors.stopped();
        }).build();
    }

    public StopPrinter(ActorContext<PrintMessage> context) {
        super(context);
    }

    public static Behavior<PrintMessage> create() {
        return Behaviors.setup(StopPrinter::new);
    }

    public static void main(String[] args) {
        final ActorSystem<PrintMessage> system = ActorSystem.create(create(), "stop-printer");
        system.tell(new PrintMessage("Hello there!"));
    }
}