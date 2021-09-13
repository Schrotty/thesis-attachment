package actors.simple;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

class SimplePrinter extends AbstractBehavior<SimplePrinter.PrintMessage> {
    static final class PrintMessage {
        public final String text;

        public PrintMessage(String text) {
            this.text = text;
        }
    }

    @Override
    public Receive<PrintMessage> createReceive() {
        return newReceiveBuilder().onMessage(PrintMessage.class, (message) -> {
            System.out.println(message.text);
            return Behaviors.same();
        }).build();
    }

    public SimplePrinter(ActorContext<PrintMessage> context) {
        super(context);
    }

    public static Behavior<PrintMessage> create() {
        return Behaviors.setup(SimplePrinter::new);
    }

    public static void main(String[] args) {
        final ActorSystem<PrintMessage> system = ActorSystem.create(SimplePrinter.create(), "simple-printer");
        system.tell(new PrintMessage("Hello there!"));
    }
}