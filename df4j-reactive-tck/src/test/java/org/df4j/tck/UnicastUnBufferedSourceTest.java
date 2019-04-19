package org.df4j.tck;


import org.df4j.core.actor.Source;
import org.df4j.core.actor.UnicastUnBufferedSource;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.tck.TestEnvironment;

public class UnicastUnBufferedSourceTest extends PublisherVerification {
    static final  int defaultTimeout = 400;

    public UnicastUnBufferedSourceTest() throws NoSuchFieldException, IllegalAccessException {
        super(new TestEnvironment(defaultTimeout));
    }

    @Override
    public Publisher<Long> createPublisher(long elements) {
        Source flowPublisher = new UnicastUnBufferedSource(elements);
        flowPublisher.start();
        return flowPublisher;
    }

    @Override
    public Publisher<Long> createFailedPublisher() {
        Publisher<Long> flowPublisher = new FailedUnicastSource();
        return flowPublisher;
    }

    static class FailedUnicastSource extends UnicastUnBufferedSource {

        public FailedUnicastSource() {
            super(1);
        }

        @Override
        public void subscribe(Subscriber<? super Long> subscriber) {
            super.subscribe(subscriber);
            subscriber.onError(new RuntimeException());
        }

        @Override
        protected void runAction() {
            output.onError(new RuntimeException());
        }
    }
}
