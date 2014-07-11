package sample.hello;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.testkit.JavaTestKit;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.concurrent.duration.Duration;

/**
 * User: Rob
 * Date: 7/10/14
 * Time: 8:58 PM
 * <p>
 * (c) ontometrics 2014, All Rights Reserved
 */
public class HelloWorldTest {

    static ActorSystem system;

    public static class SomeActor extends UntypedActor {
        ActorRef target = null;

        public void onReceive(Object msg) {

            if (msg.equals("hello")) {
                getSender().tell("world", getSelf());
                if (target != null) target.forward(msg, getContext());

            } else if (msg instanceof ActorRef) {
                target = (ActorRef) msg;
                getSender().tell("done", getSelf());
            }
        }
    }


    @BeforeClass
    public static void setup(){
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testHelloWorldActor(){
        new JavaTestKit(system){{
            final Props props = Props.create(SomeActor.class);
            final ActorRef subject = system.actorOf(props);

            // can also use JavaTestKit “from the outside”
            final JavaTestKit probe = new JavaTestKit(system);
            // “inject” the probe by passing it to the test subject
            // like a real resource would be passed in production
            subject.tell(probe.getRef(), getRef());
            // await the correct response
            expectMsgEquals(duration("1 second"), "done");

            // the run() method needs to finish within 3 seconds
            new Within(duration("3 seconds")) {
                protected void run() {

                    subject.tell("hello", getRef());

                    // This is a demo: would normally use expectMsgEquals().
                    // Wait time is bounded by 3-second deadline above.
                    new AwaitCond() {
                        protected boolean cond() {
                            return probe.msgAvailable();
                        }
                    };

                    // response must have been enqueued to us before probe
                    expectMsgEquals(Duration.Zero(), "world");
                    // check that the probe we injected earlier got the msg
                    probe.expectMsgEquals(Duration.Zero(), "hello");
                    Assert.assertEquals(getRef(), probe.getLastSender());

                    // Will wait for the rest of the 3 seconds
                    expectNoMsg();
                }
            };
        }};
    }

}
