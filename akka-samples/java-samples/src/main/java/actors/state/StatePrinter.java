package actors.state;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

class StatePrinter extends AbstractBehavior<StatePrinter.PrintMessage> {
    static final class PrintMessage {
        public final String text;

        public PrintMessage(String text) {
            this.text = text;
        }
    }

    private final int maximumMessages = 3;
    private int messageCounter = 0;

    @Override
    public Receive<PrintMessage> createReceive() {
        return newReceiveBuilder().onMessage(PrintMessage.class, (message) -> {
            System.out.println(message.text);

            int messagesTillShutdown = maximumMessages - ++messageCounter;
            System.out.printf("%s messages left before shutdown\r\n", messagesTillShutdown);

            if (messagesTillShutdown == 0) {
                System.out.println("Shutting down!");
                return Behaviors.stopped();
            }

            return Behaviors.same();
        }).build();
    }

    public StatePrinter(ActorContext<PrintMessage> context) {
        super(context);
    }

    public static Behavior<PrintMessage> create() {
        return Behaviors.setup(StatePrinter::new);
    }

    public static void main(String[] args) {
        final ActorSystem<PrintMessage> system = ActorSystem.create(StatePrinter.create(), "printer");
        system.tell(new PrintMessage("Hello there!"));
        system.tell(new PrintMessage("Oh, Hi there!"));
        system.tell(new PrintMessage("Oh, Hi Mark!"));
    }
}