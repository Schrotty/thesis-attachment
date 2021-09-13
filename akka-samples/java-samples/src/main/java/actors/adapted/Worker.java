package actors.adapted;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class Worker extends AbstractBehavior<Worker.Request> {
    interface Request {}
    static final class StartJob implements Request {
        public Double task;
        public ActorRef<Response> replyTo;

        StartJob(Double task, ActorRef<Response> replyTo) {
            this.task = task;
            this.replyTo = replyTo;
        }
    }

    interface Response {}
    static final class JobFinished implements Response {
        public Double task;
        public Double result;

        JobFinished(Double task, Double result) {
            this.task = task;
            this.result = result;
        }
    }

    public Worker(ActorContext<Request> context) {
        super(context);
    }

    public static Behavior<Request> create() {
        return Behaviors.setup(Worker::new);
    }

    @Override
    public Receive<Request> createReceive() {
        return newReceiveBuilder().onMessage(StartJob.class, (job) -> {
            job.replyTo.tell(new JobFinished(job.task, job.task*2));

            return Behaviors.stopped();
        }).build();
    }
}
