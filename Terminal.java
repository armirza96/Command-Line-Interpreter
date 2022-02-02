import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.omg.SendingContext.RunTime;

public class Terminal {
    
    private User USER;

    public Terminal() {
        System.out.println("Program starting...");

        System.out.println("Reading environment variables...");
        try {
            USER = getEnvirUser();

            System.out.println("User Generated...");
        } catch (IOException e) {
            e.printStackTrace();
        }

        
    }

    public void run() {
        Runnable run = () -> {
            try {
                controlUserFlow();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        createThread(run, false);
    }

    private void controlUserFlow() throws IOException {
        Scanner scanner = new Scanner(System.in);

        String input;
        
        System.out.print(USER.getUserNameWithHost() + " ");
        while((input = scanner.nextLine()) != null) {
           // System.out.println("User inputted: " + input);

            if(input.equalsIgnoreCase("exit")) {
                break;
            } else if (!input.isEmpty()) {
                String[] command = input.split(" ");
                if(command.length <= 5)
                    decipherCommand(input);
                else
                    System.out.println("Uknown command.");
            }

            System.out.print(USER.getUserNameWithHost() + " ");
        }
        
        scanner.close();

        System.out.println("Terminal exited.");
    }

    private void decipherCommand(String input) { 

        COMMAND_LOW_LEVEL lowLevel = checkLowLevelPredicate(input);
        COMMAND_HIGH_LEVEL highLevel = checkHighLevelPredicate(input);
        Boolean runInBg = input.contains("&");

        // if string is echo "Hello world!" ->> file.txt &
        input = input.replace(lowLevel.value, ""); // removes echo or datetime
        input = input.replace(highLevel.value, ""); // removes ->> or ->
        input = input.replace("&", ""); // removes &
        // left with "Hello world!" file.txt

        String cleanedInput = input;
        String[] command = cleanedInput.split(" ");

        //String output = getOutPutString(lowLevel, cleanedInput);
        Function<String, String> func;

        if(highLevel == COMMAND_HIGH_LEVEL.APPEND || highLevel == COMMAND_HIGH_LEVEL.OVERWRITE) {
            String fileName = command[command.length-1];
            func = (content) -> {
                return writeToFile(fileName, content, highLevel == COMMAND_HIGH_LEVEL.APPEND); 
            };
        } else {
            func = (content) -> {
                return content;
            };
        }

        Runnable run = () -> {
            String dataToWrite =  getOutPutString(lowLevel, cleanedInput);   

            String consoleOutput = (String) func.apply(dataToWrite);

            if(!runInBg)
                System.out.println(consoleOutput);
        };

       createThread(run, runInBg);

    }

    private String getOutPutString(COMMAND_LOW_LEVEL cmd, String cleanedInput) {
        String text = "";

        if(cmd == COMMAND_LOW_LEVEL.ECHO) {
            Pattern p = Pattern.compile("\"([^\"]*)\"");
            Matcher m = p.matcher(cleanedInput);
            while (m.find()) {
                text += m.group(1);
            }
        } else if(cmd == COMMAND_LOW_LEVEL.DATETIME) {
            text = new Date().toString();
        } else if(cmd == COMMAND_LOW_LEVEL.EXECUTABLE){
            //System.out.println("PATH: " + cleanedInput);

            /***
             * insert loop for external commands here
             * if theres no hit for checking the external commands in a Hashmap<String, String>
             * then go on to this if statement
             * we then supply the path to the if statement and the runExec method
             * getExternal commands will have implementation line 268
            */

            if(checkIfValidPath(cleanedInput)) {
                try {
                    text = runExec(cleanedInput);
                } catch (IOException e) {
                    text = convertStrackTraceToString(e);
                }
            } else {
                text = "Supplied path was not valid.";
            }
        } 

        return text;
    }

    private void createThread(Runnable run, Boolean runInBg) {
        Task task = new Task(run, runInBg);

        if(!runInBg) {
            try {
                task.thread.join();
            } catch (InterruptedException e) {
                
                e.printStackTrace();
            }
        }
    }

    private COMMAND_LOW_LEVEL checkLowLevelPredicate(String input) {
        LowLevelPredicate<String> predicate = new LowLevelPredicate<>();

        if(predicate.test(input)) {
            if(predicate.testForEcho(input)) {
                return COMMAND_LOW_LEVEL.ECHO;
            } else {
                return COMMAND_LOW_LEVEL.DATETIME;
            }
        
        } else {
            return COMMAND_LOW_LEVEL.EXECUTABLE;
        }
    }

    private COMMAND_HIGH_LEVEL checkHighLevelPredicate(String input) {
        HighLevelPredicate<String> predicate = new HighLevelPredicate<>();
        if(predicate.test(input)) {
            if(predicate.testForAppendingToFile(input)) {
                return COMMAND_HIGH_LEVEL.APPEND;
            } else {
                return COMMAND_HIGH_LEVEL.OVERWRITE;
            }
        
        } else {
            return COMMAND_HIGH_LEVEL.CONSOLE;
        }
    }

    private String writeToFile(String nameOfFile, String content, Boolean appendToFile) {
        try {
            FileWriter w = new FileWriter(nameOfFile, appendToFile);
            w.write("\n"+content);
            w.close();
            return "Wrote to file successfully.";
          } catch (IOException e) {
            e.printStackTrace();
            return convertStrackTraceToString(e);
          }
    }

    private String runExec(String path) throws IOException {
        Runtime rt = Runtime.getRuntime();

        Process process = rt.exec(path);

        String results = "";

        try {
            InputStreamReader sr = new InputStreamReader(process.getInputStream());
            BufferedReader reader = new BufferedReader(sr);

            results += reader.lines().collect(Collectors.joining("\n"));//
            reader.lines().forEach(System.out::println);
        } catch(Exception e) {
            results = convertStrackTraceToString(e);
        }

        try {
            InputStreamReader sr = new InputStreamReader(process.getErrorStream());
            BufferedReader reader = new BufferedReader(sr);

            //reader.lines().forEach(System.out::println); 
            results += reader.lines().collect(Collectors.joining("\n"));
            reader.lines().forEach(System.out::println);
        } catch(Exception e) {
            new InputStreamReader(process.getErrorStream());
        }

        return results;
    }

    private User getEnvirUser() throws IOException{
        BufferedReader reader = new BufferedReader(new FileReader("input.txt"));

        String userName = reader.readLine();
        String hostName = reader.readLine();
        String path = reader.readLine();

        System.out.println(String.format("Environment Variables: %s, %s, %s", userName, hostName, path));

        String[] paths = path.split(",");

        reader.close();

        return new User(userName, hostName, paths);
    }

    private Dictionary<String, String> getExternalCommands() {
        /**
         * What im thinking is that for the paths in the User variable
         * check each path for executables or .bat files
         * and then when executing online 45 we check if the hashtable has any commands that are like the what the user 
         * typed and we execute it if not we know its some external thing
         * Im thinking dictionary should be Eexectubale => path 
         */
        return new Hashtable<>();
    }

    /**
     * https://stackoverflow.com/questions/468789/is-there-a-way-in-java-to-determine-if-a-path-is-valid-without-attempting-to-cre
     * @param path
     * @return
     */
    public boolean checkIfValidPath(String path) {
        path = path.trim();
        try {
            Paths.get(path);
        } catch (InvalidPathException | NullPointerException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private String convertStrackTraceToString(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
