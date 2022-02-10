
public  class Task implements Runnable{
    Thread thread;
    Runnable run;
    Boolean isBackgroundThread;

    public Task(Runnable run, Boolean isBackgroundThread) {

        this. run = run;

        this.isBackgroundThread = isBackgroundThread;

        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        run.run();
    }
    


}
