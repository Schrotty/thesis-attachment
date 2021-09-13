package actors.context;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.Calendar;

class ContextPrinter extends AbstractBehavior<ContextPrinter.PrintMessage> {
    static final class PrintMessage {
        public final String text;

        public PrintMessage(String text) {
            this.text = text;
        }
    }

    public ContextPrinter(ActorContext<PrintMessage> context) {
        super(context);
        ActorRef<PrintMessage> reference = context.spawn(date(), "datetime-actor");
    }

    private Behavior<PrintMessage> date() {
        return Behaviors.setup(context -> {
            System.out.println(Calendar.getInstance().getTime());
            return Behaviors.stopped();
        });
    }

    public static Behavior<PrintMessage> create() {
        return Behaviors.setup(ContextPrinter::new);
    }

    @Override
    public Receive<PrintMessage> createReceive() {
        return newReceiveBuilder().onMessage(PrintMessage.class, (message) -> {
            System.out.println(message.text);
            getContext().getLog().info(String.format("Received message: %s", message.text));

            return Behaviors.same();
        }).build();
    }

    public static void main(String[] args) {
        final ActorSystem<PrintMessage> system = ActorSystem.create(ContextPrinter.create(), "simple-printer");
        system.tell(new PrintMessage("Hello there!"));
    }
}