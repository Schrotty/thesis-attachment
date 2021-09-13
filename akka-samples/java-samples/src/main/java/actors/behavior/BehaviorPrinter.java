package actors.behavior;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

class BehaviorPrinter extends AbstractBehavior<BehaviorPrinter.PrintMessage> {
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

            return newReceiveBuilder().onMessage(PrintMessage.class, (msg) -> {
                System.out.println("Accepting no more messages!");
                return Behaviors.stopped();
            }).build();
        }).build();
    }

    public BehaviorPrinter(ActorContext<PrintMessage> context) {
        super(context);
    }

    public static Behavior<PrintMessage> create() {
        return Behaviors.setup(BehaviorPrinter::new);
    }

    public static void main(String[] args) {
        final ActorSystem<PrintMessage> system = ActorSystem.create(BehaviorPrinter.create(), "printer");
        system.tell(new PrintMessage("Hello there!"));
        system.tell(new PrintMessage("Oh Hi there!"));
    }
}