package actors.adapted;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class Manager extends AbstractBehavior<Manager.Command> {

    private int workerCounter;
    private final ActorRef<Worker.Response> workerResponseMapper;

    interface Command {}
    static final class Calculate implements Command {
        public Double task;

        Calculate(Double task) {
            this.task = task;
        }
    }
    static final class MappedWorkerResponse implements Command {
        public Worker.Response response;

        MappedWorkerResponse(Worker.Response response) {
            this.response = response;
        }
    }

    public Manager(ActorContext<Command> context) {
        super(context);

        this.workerCounter = 0;
        this.workerResponseMapper = context.messageAdapter(Worker.Response.class, MappedWorkerResponse::new);
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(Manager::new);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder().onMessage(Calculate.class, (message) -> {
            this.workerCounter++;
            getContext().spawnAnonymous(Worker.create()).tell(new Worker.StartJob(message.task, this.workerResponseMapper));

            return Behaviors.same();
        }).onMessage(MappedWorkerResponse.class, (response) -> {
            if (response.response instanceof Worker.JobFinished) {
                this.workerCounter--;
                System.out.printf("Job (%s) finished: %s%n", ((Worker.JobFinished) response.response).task, ((Worker.JobFinished) response.response).result);
                return workerCounter == 0 ? Behaviors.stopped() : Behaviors.same();
            }

            return Behaviors.same();
        }).build();
    }
}
