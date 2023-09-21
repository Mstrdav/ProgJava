import java.sql.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Threads {
    static int concurrent = 0; // used for showing synchronzation

    public static void log(Object o) {
        Thread thread = Thread.currentThread();
        System.out.println("[" + thread.getName() + "] " + o);
    }

    public static void logWithPriority(Object o) {
        Thread thread = Thread.currentThread();
        System.out.println("[" + thread.getName() + "/" + thread.getPriority() + "] " + o);
    }

    // logp is a shortcut for logWithPriority
    public static void logp(Object o) {
        logWithPriority(o);
    }

    public static class GreeterThread extends Thread {
        // override constructor to set the name of the thread
        public GreeterThread(String name) {
            super(name.isEmpty() ? "Colin" : name);
        }

        @Override
        public void run() {
            // sleep random time
            try {
                Thread.sleep((long) (Math.random() * 100));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            logp("Greetings from " + getName() + "!");
        }
    }

    public static class GreeterRunnable implements Runnable {
        @Override
        public void run() {
            logp("Hi from runnable");
        }
    }

    private static void greetAll(List<String> names) {
        names.forEach(name -> {
            Thread t = new GreeterThread(name);
            t.start();
        });
    }

    // greetAllJoined greets in order by joining threads
    private static void greetAllJoined(List<String> names) {
        names.forEach(name -> {
            Thread t = new GreeterThread(name);
            t.start();
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    // TickerThread
    public static class TickerThread extends Thread {
        private int ticks;

        public TickerThread(int ticks) {
            this.ticks = ticks;
        }

        // constructor with default value
        public TickerThread() {
            this(10);
        }

        @Override
        public void run() {
            while (ticks > 0) {
                logp("Tick " + ticks--);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logp("Interrupted! (pfiouu)");
                    return;
                }
            }

            logp("Boom!");
        }
    }

    // TickerThread as daemon
    public static class TickerThreadDaemon extends Thread {
        private int ticks;

        public TickerThreadDaemon(int ticks) {
            this.ticks = ticks;
            setDaemon(true);
        }

        // constructor with default value
        public TickerThreadDaemon() {
            this(10);
        }

        @Override
        public void run() {
            while (ticks > 0) {
                logp("Tick " + ticks--);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            logp("Boom!");
        }
    }

    // TimerTickerThread
    public static class TimerTickerThread extends Timer {
        public void start() {
            schedule(new TimerTask() {
                private int ticks = 0;

                @Override
                public void run() {
                    logp("Tick " + ticks++);
                    if (ticks > 1000) {
                        logp("Canceling timer cause its been too long...");
                        cancel();
                        return;
                    }
                }
            }, 0, 1000);
        }
    }

    // BirthTimerThread
    public static class BirthdayTimerThread extends Timer {
        private Date date;

        public BirthdayTimerThread(Date date) {
            this.date = date;
        }

        public void start() {
            schedule(new TimerTask() {
                @Override
                public void run() {
                    logp("Happy Birthday !!");
                    this.cancel();
                    super.cancel();
                }
            }, date);
        }
    }

    public static void main(String[] args) {
        log("hello");
        logWithPriority("hello with priority");

        // extended greeter threads
        GreeterThread gt = new GreeterThread("Miguel");
        // gt.setName("MiguelDos");
        gt.start();

        // runnables
        Thread runT = new Thread(new GreeterRunnable());
        runT.start();

        // inline runnable
        Thread inlRunT = new Thread(new Runnable() {
            @Override
            public void run() {
                log("Hi from inline runnable");
            }
        });
        inlRunT.start();

        // lambda runnable
        // Thread t = new Thread(() -> log("Hi from runnable"));
        // t.start();


        // greet all threads
        greetAll(List.of("list-Miguel", "list-Colin", "list-MiguelDos"));

        // greet all threads joined
        greetAllJoined(List.of("joined-Miguel", "joined-Colin", "joined-MiguelDos"));

        // ticker thread
        TickerThread ticker = new TickerThread(5);
        ticker.start();
        // and stop it before the bomb explodes
        try {
            Thread.sleep(4500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ticker.interrupt();

        // timer ticker thread
        TimerTickerThread timerTicker = new TimerTickerThread();
        timerTicker.start();
        // and stop it after a few seconds
        try {
            Thread.sleep(4500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        timerTicker.cancel();

        // wish me happy birthday in a few seconds
        Date nowPlus5Seconds = new Date(System.currentTimeMillis() + 5000);
        BirthdayTimerThread birthdayTimer = new BirthdayTimerThread(nowPlus5Seconds);
        birthdayTimer.start(); // never stop ??

        // SYNCRHONIZATION
        // race to modify a shared variable : concurrent
        // we create 100 threads that will increment the variable
        // we expect concurrent to be 100 at the end
        // but it is not, because the threads are not synchronized
        for (int i = 0; i < 100; i++) {
            new Thread(() -> {
                int a = concurrent;
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    return;
                }
                concurrent = a + 1;
            }).start();
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        log("concurrent = " + concurrent);

    }

}

// Function log output the name of the current name, followed by the given
// parameter
// main function execute in main thread, so the output is [main] hello

// Creating new threads
// the greeting thread is named Thread-0, because it is the first thread
// If we replace start with run, the greeting thread will be named main (we're
// running from the main thread)
