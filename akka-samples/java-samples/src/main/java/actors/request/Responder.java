package actors.request;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class Responder extends AbstractBehavior<Responder.Request> {
    static final class Request {
        public final String query;
        public final ActorRef<Requester.Response> replyTo;

        public Request(String query, ActorRef<Requester.Response> replyTo) {
            this.query = query;
            this.replyTo = replyTo;
        }
    }

    @Override
    public Receive<Responder.Request> createReceive() {
        return newReceiveBuilder().onMessage(Responder.Request.class, (message) -> {
            System.out.printf("Received request: %s%n", message.query);
            message.replyTo.tell(new Requester.Response(String.format("Your Result: %s%n", Math.PI)));

            return Behaviors.stopped();
        }).build();
    }

    public Responder(ActorContext<Responder.Request> context) {
        super(context);
    }

    public static Behavior<Responder.Request> create() {
        return Behaviors.setup(Responder::new);
    }
}