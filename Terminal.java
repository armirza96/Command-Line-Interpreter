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
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.omg.SendingContext.RunTime;

public class Terminal {
    
    private static User USER;

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
        try {
            controlUserFlow();
        } catch (IOException e) {
            e.printStackTrace();
        } 
    }

    private void controlUserFlow() throws IOException {
        Scanner scanner = new Scanner(System.in);

        String input;
        
        System.out.print(USER.getUserNameWithHost() + " ");
        while((input = scanner.nextLine()) != null) {
            System.out.println("User inputted: " + input);

            if(input.equalsIgnoreCase("exit")) {
                break;
            }

            decipherCommand(input);

            System.out.print(USER.getUserNameWithHost() + " ");
        }
        
        scanner.close();

        System.out.println("Terminal exited.");
    }

    private void decipherCommand(String input) { 

        Function<String, ?> functionExecutable = null;
        Function<String, ?> func;
        String dataToWrite = "";
        String[] command = input.split(" ");

        HighLevelPredicate<String> predicate = new HighLevelPredicate<>();
        if(predicate.test(input)) {
            
            String fileName = command[command.length-1];
            func = (content) -> {
                 return writeToFile(fileName, content, predicate.testForAppendingToFile(input)); 
            };
  
        } else {
            func = (content) -> {
                System.out.print(content);
                return "";
            };
        }

        if(checkIfValidPath(command[0])) {
            functionExecutable = (path) -> {
                String results = "";
                try {
                    results = runExec(path);
                } catch (IOException e) {
                    results = convertStrackTraceToString(e);
                }
                
                return results;
            };
        } 

        if(functionExecutable != null) {
            dataToWrite = (String) functionExecutable.apply(command[0]);
        } else {
            dataToWrite = getOutputString(input);
        }

        String consoleOutput = (String) func.apply(dataToWrite);

        System.out.println(consoleOutput);
    }

    private String getOutputString(String input) {
        String text="";

        COMMAND_LOW_LEVEL value = checkLowLevelPredicate(input);

        if(value == COMMAND_LOW_LEVEL.ECHO) {
            Pattern p = Pattern.compile("\"([^\"]*)\"");
            Matcher m = p.matcher(input);
            while (m.find()) {
                text += m.group(1);
            }
        } else if(value == COMMAND_LOW_LEVEL.DATETIME) {
            text = new Date().toString();
        } else {
            text = COMMAND_LOW_LEVEL.EXECUTABLE.value;
        }

        return text;
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

    /**
     * https://stackoverflow.com/questions/468789/is-there-a-way-in-java-to-determine-if-a-path-is-valid-without-attempting-to-cre
     * @param path
     * @return
     */
    public boolean checkIfValidPath(String path) {
        try {
            Paths.get(path);
        } catch (InvalidPathException | NullPointerException e) {
            //e.printStackTrace();
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
