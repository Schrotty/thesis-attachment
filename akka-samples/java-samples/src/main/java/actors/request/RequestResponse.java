package actors.request;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class RequestResponse extends AbstractBehavior<String> {

    @Override
    public Receive<String> createReceive() {
        return newReceiveBuilder().onSignal(Terminated.class, (message) -> Behaviors.stopped()).build();
    }

    public RequestResponse(ActorContext<String> context) {
        super(context);

        ActorRef<Responder.Request> responder = context.spawnAnonymous(Responder.create());
        ActorRef<Requester.Response> requester = context.spawnAnonymous(Requester.create(responder));
        context.watch(requester);
    }

    public static Behavior<String> create() {
        return Behaviors.setup(RequestResponse::new);
    }

    public static void main(String[] args) {
        ActorSystem.create(RequestResponse.create(), "request-response");
    }
}
