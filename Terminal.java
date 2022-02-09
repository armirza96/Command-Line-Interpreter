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
import java.util.HashMap;
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

//\import org.omg.SendingContext.RunTime;

public class Terminal {
    
    private User USER;
    private Hashtable<String, String> EXTERNAL_COMMANDS = null;

    public Terminal() {
        System.out.println("Program starting...");

        System.out.println("Reading environment variables...");
        try {
            USER = getEnvirUser();
            EXTERNAL_COMMANDS = getExternalCommands();

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

    // I/O Method
    private void controlUserFlow() throws IOException {
        Scanner scanner = new Scanner(System.in);

        String input;														// User Input string
        
        System.out.print(USER.getUserNameWithHost() + " ");					// Output Prompt
        while((input = scanner.nextLine()) != null) {						// when complete
           // System.out.println("User inputted: " + input);

            if(input.equalsIgnoreCase("exit")) {							// if exit received
                break;														// leave loop
            } else if (!input.isEmpty()) {									// if not empty
                ArrayList<String> command = splitCommandBySpace(input);//String[] command = input.split(" ");						// Separate input by space delimeter
                if(command.size() <= 5)										// if command is less than 5 cells (maximum operator count)
                    decipherCommand(input);									// go to parser method
                else
                    System.out.println("Unknown command.");					// else error message
            }

            System.out.print(USER.getUserNameWithHost() + " ");				// return to prompt	
        }
        
        scanner.close();

        System.out.println("Terminal exited.");								// exit terminaL
    }

    private void decipherCommand(String input) { 

        COMMAND_LOW_LEVEL lowLevel = checkLowLevelPredicate(input);			// Check if low-level
        COMMAND_HIGH_LEVEL highLevel = checkHighLevelPredicate(input);		// check if high level
        Boolean runInBg = input.contains("&");								// background check

        																	// if string is echo "Hello world!" ->> file.txt &
        input = input.replace(lowLevel.value, ""); 							// removes echo or datetime
        input = input.replace(highLevel.value, ""); 						// removes ->> or ->
        input = input.replace("&", "");										// removes & left with "Hello world!" file.txt

        String cleanedInput = input.toLowerCase().trim();					// clean the user input and remve trailing and leading spaces							

        //String output = getOutPutString(lowLevel, cleanedInput);
        Function<String, String> func;										// define lambda expression

        																	// if high level command
        if(highLevel == COMMAND_HIGH_LEVEL.APPEND || highLevel == COMMAND_HIGH_LEVEL.OVERWRITE) {
            String[] command = cleanedInput.split(" ");							// split into command and input
            String fileName = command[command.length-1];					// extract file name
            func = (content) -> {
                return writeToFile(fileName, content, highLevel == COMMAND_HIGH_LEVEL.APPEND); 
            };                                                              // execute 
        } else {
            func = (content) -> {
                return content;
            };
        }

        Runnable run = () -> {                                              // if low-level command
            String dataToWrite =  getOutPutString(lowLevel, cleanedInput);  // get the correct output string   

            String consoleOutput = (String) func.apply(dataToWrite);

            if(!runInBg)
                System.out.println(consoleOutput);
        };

       createThread(run, runInBg);

    }

    private String getOutPutString(COMMAND_LOW_LEVEL cmd, String input) {
        String text = "";

        if(cmd == COMMAND_LOW_LEVEL.ECHO) {
            Pattern p = Pattern.compile("\"([^\"]*)\"");
            Matcher m = p.matcher(input);
            while (m.find()) {
                text += m.group(1);
            }
        } /*else if(cmd == COMMAND_LOW_LEVEL.DATETIME) {						// If datetime command
            text = new Date().toString();
        }*/ else if(cmd == COMMAND_LOW_LEVEL.EXECUTABLE){						// if executable
            //System.out.println("PATH: " + cleanedInput);

            /***
             * insert loop for external commands here
             * if theres no hit for checking the external commands in a Hashmap<String, String>
             * then go on to this if statement
             * we then supply the path to the if statement and the runExec method
             * getExternal commands will have implementation line 268
            */

            if (EXTERNAL_COMMANDS.containsKey(input)) {               
                String path = EXTERNAL_COMMANDS.get(input);
                if(checkIfValidPath(path)) {
                    try {
                        text = runExec(path);
                    } catch (IOException e) {
                        text = convertStrackTraceToString(e);
                    }
                } else {
                    text = "Supplied path was not valid.";
                }
        	} else {
                text = "Invalid command, path or input.";
        	}
        } 

        return text;
    }

    private void createThread(Runnable run, Boolean runInBg) {
        Task task = new Task(run, runInBg);									// Generate new task

        if(!runInBg) {														// if not run in background
            try {
                task.thread.join();											// run and join
            } catch (InterruptedException e) {
                
                e.printStackTrace();
            }
        }
    }

    private COMMAND_LOW_LEVEL checkLowLevelPredicate(String input) {
        LowLevelPredicate<String> predicate = new LowLevelPredicate<>();

        if(predicate.test(input)) {
            // if(predicate.testForEcho(input)) {
            //     return COMMAND_LOW_LEVEL.ECHO;
            // } else {
            //     return COMMAND_LOW_LEVEL.DATETIME;
            // }
            return COMMAND_LOW_LEVEL.ECHO;
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

        path = path.replace("PATH=", "");

        String[] paths = path.split(",");

        reader.close();

        return new User(userName, hostName, paths);
    }

    private Hashtable<String, String> getExternalCommands() {
        /**
         * What im thinking is that for the paths in the User variable
         * check each path for executables or .bat files
         * and then when executing online 45 we check if the hashtable has any commands that are like the what the user 
         * typed and we execute it if not we know its some external thing
         * Im thinking dictionary should be Executable => path 
         */
    	
    	Hashtable<String, String> keyTable= new Hashtable<String, String>();	
    	
    	String[] path = USER.getPaths();
    	
    	for (int counter = 0; counter < path.length; counter++) {
    		File DirectoryPath = new File(path[counter]);
    		String[] Files = DirectoryPath.list();
    		
            if(Files != null) {
                for (int cursor = 0; cursor < Files.length; cursor++) {
                    if(Files[cursor].toLowerCase().contains(".exe") || Files[cursor].toLowerCase().contains(".bat")) {
                        String fileName = Files[cursor].toLowerCase().replace(".bat", "");
                        fileName = fileName.replace(".exe", "");

                        keyTable.put(fileName, path[counter].toLowerCase() + Files[cursor].toLowerCase());
                    }
                }
            }
    	}
        return keyTable;
    }

    /**
     * https://stackoverflow.com/questions/7804335/split-string-on-spaces-in-java-except-if-between-quotes-i-e-treat-hello-wor
     * @param input
     * @return
     */
    private ArrayList<String> splitCommandBySpace(String input) {
        ArrayList<String> list = new ArrayList<String>();
        Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(input);
        while (m.find())
            list.add(m.group(1));

        return list;
    }

    /**
     * https://stackoverflow.com/questions/468789/is-there-a-way-in-java-to-determine-if-a-path-is-valid-without-attempting-to-cre
     * @param path
     * @return
     */
    private boolean checkIfValidPath(String path) {
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
