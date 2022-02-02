import java.util.function.Function;
import java.util.function.Supplier;

public  class Task implements Runnable{
    Thread thread;
    Runnable func;
    Boolean isBackgroundThread;

    public Task(Runnable func, Boolean isBackgroundThread) {

        this. func = func;

        this.isBackgroundThread = isBackgroundThread;

        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        func.run();
    }
    


}
