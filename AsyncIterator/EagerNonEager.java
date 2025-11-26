
public static <T, U> Iterator<CompletableFuture<U>> asyncMapStream(
        Iterator<T> upstream,
        Function<T, CompletableFuture<U>> mapper,
        int maxFuturesInFlight) {

    return new Iterator<CompletableFuture<U>>() {

        private final ArrayDeque<CompletableFuture<U>> queue = new ArrayDeque<>();

        private void fillQueue() {
            while (queue.size() < maxFuturesInFlight && upstream.hasNext()) {
                T item = upstream.next();
                queue.add(mapper.apply(item));
            }
        }

        @Override
        public boolean hasNext() {
            if (!queue.isEmpty()) return true;
            fillQueue();  // try filling
            return !queue.isEmpty();
        }

        @Override
        public CompletableFuture<U> next() {
            if (!hasNext()) throw new NoSuchElementException();
            CompletableFuture<U> fut = queue.poll();
            fillQueue();  // refill after consuming one
            return fut;
        }
    };
}
////EagerVersion

public static <T, U> Iterator<CompletableFuture<U>> asyncMapStream(
        Iterator<T> upstream,
        Function<T, CompletableFuture<U>> mapper,
        int maxFuturesInFlight) {

    return new Iterator<CompletableFuture<U>>() {

        private final BlockingQueue<CompletableFuture<U>> readyQueue =
                new LinkedBlockingQueue<>();

        private int inFlight = 0;
        private boolean upstreamExhausted = false;

        // Lock protects scheduling/reads
        private final Object lock = new Object();

        {
            // Start by filling initial futures
            scheduleMore();
        }

        private void scheduleMore() {
            synchronized (lock) {
                while (inFlight < maxFuturesInFlight && upstream.hasNext()) {
                    T nextItem = upstream.next();
                    inFlight++;

                    CompletableFuture<U> fut = mapper.apply(nextItem);

                    fut.whenCompleteAsync((val, ex) -> {
                        synchronized (lock) {
                            inFlight--;
                        }
                        // add to completed queue
                        readyQueue.add(fut);
                        // try scheduling next one
                        scheduleMore();
                    });
                }

                if (!upstream.hasNext()) {
                    upstreamExhausted = true;
                }
            }
        }

        @Override
        public boolean hasNext() {
            synchronized (lock) {
                // If queue has completed futures → more results exist
                if (!readyQueue.isEmpty()) return true;

                // If upstream is done and no in-flight → finished
                return !(upstreamExhausted && inFlight == 0);
            }
        }

        @Override
        public CompletableFuture<U> next() {
            if (!hasNext()) throw new NoSuchElementException();
            try {
                // Blocking take is ok because async completion will eventually add
                return readyQueue.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
    };
}

////Non Eager Main

import java.util.*;
import java.util.concurrent.*;

public class NonEagerDemo {

    public static void main(String[] args) {

        List<Integer> input = List.of(1, 2, 3, 4, 5);

        // simulate async mapper
        Function<Integer, CompletableFuture<String>> mapper = i ->
                CompletableFuture.supplyAsync(() -> {
                    sleep(500); // simulate remote call
                    return "Processed-" + i;
                });

        Iterator<CompletableFuture<String>> it =
                asyncMapStream(input.iterator(), mapper, 2);

        // consume iterator
        while (it.hasNext()) {
            CompletableFuture<String> f = it.next();

            // block on each future JUST FOR DEMO
            System.out.println("Got future result = " + f.join());
        }

        System.out.println("Non-eager demo complete!");
    }

    static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (Exception ignored) {}
    }
}

////Eager MAin

import java.util.*;
import java.util.concurrent.*;

public class EagerDemo {

    public static void main(String[] args) {

        List<Integer> input = List.of(1, 2, 3, 4, 5);

        // async mapper with variable delay to show out-of-order finishing
        Random rand = new Random();
        Function<Integer, CompletableFuture<String>> mapper = i ->
                CompletableFuture.supplyAsync(() -> {
                    sleep(200 + rand.nextInt(500)); // random delay
                    return "Processed-" + i;
                });

        Iterator<CompletableFuture<String>> it =
                asyncMapStreamEager(input.iterator(), mapper, 2);

        // consumer can be slow to call next()
        while (it.hasNext()) {
            CompletableFuture<String> f = it.next();

            // block just for demo
            System.out.println("Got future result = " + f.join());
            
            sleep(300); // simulate slow consumer
        }

        System.out.println("Eager demo complete!");
    }

    static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (Exception ignored) {}
    }
}

