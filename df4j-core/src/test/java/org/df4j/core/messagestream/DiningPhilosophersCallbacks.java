package org.df4j.core.messagestream;

import org.df4j.core.boundconnector.permitscalar.ScalarPermitSubscriber;
import org.df4j.core.simplenode.messagestream.PickPoint;
import org.df4j.core.tasknode.AsyncAction;
import org.df4j.core.util.TimeSignalPublisher;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

/**
 * Demonstrates how coroutines can be emulated.
 */
public class DiningPhilosophersCallbacks {
    static final int num = 5; // number of phylosofers
    static int N = 4; // number of rounds
    ForkPlace[] forkPlaces = new ForkPlace[num];
    CountDownLatch counter = new CountDownLatch(num);
    Philosopher[] philosophers = new Philosopher[num];
    TimeSignalPublisher timer = new TimeSignalPublisher();

    @Test
    public void test() throws InterruptedException {
        // create places for forks with 1 fork in each
        for (int k = 0; k < num; k++) {
            ForkPlace forkPlace = new ForkPlace(k);
            forkPlace.post("Fork_" + k);
            forkPlaces[k] = forkPlace;
        }
        // create philosophers
        for (int k = 0; k < num; k++) {
            philosophers[k] = new Philosopher(k);
        }
        // animate all philosophers
        for (int k = 0; k < num; k++) {
            philosophers[k].startThinking();
        }
        assertTrue(counter.await(2, TimeUnit.SECONDS));
    }

    static class ForkPlace extends PickPoint<String> {
        int id;
        String label;

        public ForkPlace(int k) {
            id = k;
            label = "Forkplace_" + id;
        }
    }

    class Philosopher extends AsyncAction {
        Runnable nextAction;
        Random rand = new Random();
        int id;
        ForkPlace firstPlace, secondPlace;
        String firstFork, secondFork;
        String indent;
        int rounds = 0;

        public Philosopher(int id) {
            this.id = id;
            // to avoid deadlocks, allocate resource with lower number first
            if (id == num - 1) {
                firstPlace = forkPlaces[0];
                secondPlace = forkPlaces[id];
            } else {
                firstPlace = forkPlaces[id];
                secondPlace = forkPlaces[id + 1];
            }

            StringBuffer sb = new StringBuffer();
            sb.append(id).append(":");
            for (int k = 0; k < id; k++) sb.append("              ");
            indent = sb.toString();
            System.out.println("Ph no. " + id + ": first place = " + firstPlace.id + "; second place = " + secondPlace.id + ".");
        }

        protected long randDelay() {
            return rand.nextLong() % 15 + 17;
        }

        public void setNextTimedAction(long delay, ScalarPermitSubscriber action) {
            nextAction = () -> timer.subscribe(action, delay);
            start();
        }

        public void setNextAction(Runnable action) {
            nextAction = action;
            start();
        }

        public void startThinking() {
            nextAction =  () -> timer.subscribe(this::endThinking, randDelay());
            start();
        }

        public void endThinking() {
            println("Request first (" + firstPlace.id + ")");
            nextAction = () -> firstPlace.subscribe(this::getFork1);
            start();
        }

        public void getFork1(String fork) {
            firstFork = fork;
            println("Request second (" + secondPlace.id + ")");
            setNextAction(() -> secondPlace.subscribe(this::startEating));
        }

        public void startEating(String fork) {
            secondFork = fork;
            setNextTimedAction(randDelay(), this::endEating);
        }

        public void eating() {
            println("Release first (" + firstPlace.id + ")");
            firstPlace.post(firstFork);
            firstFork = null;
            println("Release second (" + secondPlace.id + ")");
            secondPlace.post(secondFork);
            secondFork = null;
            rounds++;
            if (rounds < N) {
                println("Ph no. " + id + ": continue round " + rounds);
                startThinking();
            } else {
                println("Ph no. " + id + ": died at round " + rounds);
                counter.countDown();
                nextAction = null;
                stop();
            }
        }

        public void endEating() {
            setNextAction(this::eating);
        }

        @Override
        public void runAction() {
            nextAction.run();
        }

        private void println(String s) {
            System.out.println(indent + s);
        }
    }
}
