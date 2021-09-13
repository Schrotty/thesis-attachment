package actors.request;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class Requester extends AbstractBehavior<Requester.Response> {
    static final class Response {
        public final String result;

        public Response(String result) {
            this.result = result;
        }
    }

    @Override
    public Receive<Requester.Response> createReceive() {
        return newReceiveBuilder().onMessage(Requester.Response.class, (message) -> {
            System.out.println(message.result);

            return Behaviors.stopped();
        }).build();
    }

    public Requester(ActorContext<Requester.Response> context, ActorRef<Responder.Request> target) {
        super(context);

        target.tell(new Responder.Request("PI", context.getSelf()));
    }

    public static Behavior<Response> create(ActorRef<Responder.Request> target) {
        return Behaviors.setup(context -> new Requester(context, target));
    }
}