import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omg.SendingContext.RunTime;

public class CommandLineInterpreter {
    
    private static User USER;

    public static void main(String args[]) {
        System.out.println("Program starting...");

        System.out.println("Reading environment variables...");
        try {
            USER = readFile();

            System.out.println("User Generated...");

            controlUserFlow();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Program exited.");
    }

    private static void controlUserFlow() throws IOException {

        
        Scanner scanner = new Scanner(System.in);

        String input;
        
        System.out.print(USER.getUserNameWithHost() + " ");
        while((input = scanner.nextLine()) != null) {
            System.out.println("User inputted: " + input);

            if(input.equalsIgnoreCase("exit")) {
                break;
            }

            
            
            decipherCommand(input);
            // if(input.contains("->") || input.contains("->>")) {

            // } 
            // Runtime rt = Runtime.getRuntime();

            // 

            // Process process = rt.exec(command);

            // try {
            //     InputStreamReader sr = new InputStreamReader(process.getInputStream());
            //     BufferedReader reader = new BufferedReader(sr);

            //     reader.lines().forEach(System.out::println);
            // } catch(Exception e) {
            //     e.printStackTrace();
            // }

            // //process.waitFor();

            // try {
            //     InputStreamReader sr = new InputStreamReader(process.getErrorStream());
            //     BufferedReader reader = new BufferedReader(sr);

            //     reader.lines().forEach(System.out::println); 
            // } catch(Exception e) {
            //     new InputStreamReader(process.getErrorStream());
            // }

             System.out.print(USER.getUserNameWithHost() + " ");
        }
        
        scanner.close();
    }

    private static void decipherCommand(String input) { //(String[] commands)

        //ArrayList<Consumer<String>> functions = new List<Consumer<String>>();

        //Function <String, String> whatToDo;

        Consumer<String> lambda;
        //String[] command = input.split(" ");

        String cleanedInput = input;

        Pattern p = Pattern.compile("\"([^\"]*)\"");
        Matcher m = p.matcher(input);
        while (m.find()) {
            cleanedInput += m.group(1);
            //System.out.println(m.group(1));
        }

        if(input.contains(COMMAND_MID_LEVEL.values()[0].value.toString().toLowerCase()) ||
        input.contains(COMMAND_MID_LEVEL.values()[1].value.toString().toLowerCase())) {
            

            if(input.contains(COMMAND_MID_LEVEL.values()[0].value.toString().toLowerCase())) {
                lambda = (content) -> {writeToFile("nameOfFile.txt", content, true); };
            } else {
                lambda = (content) -> {writeToFile("nameOfFile.txt", content, false); };
            }

            
        } else {
            lambda = content -> {System.out.println(content); };
        }

        executLambda(cleanedInput, lambda);
        // for(COMMAND_HIGH_LEVEL cmd : COMMAND_HIGH_LEVEL.values()) {
        //     if(input.contains(cmd.toString().toLowerCase())) {

        //     }
        // }

        // for(COMMAND_MID_LEVEL cmd : COMMAND_MID_LEVEL.values()) {
        //     if(input.contains(cmd.toString().toLowerCase())) {

        //     }
        // }

        // for(COMMAND_LOW_LEVEL cmd : COMMAND_LOW_LEVEL.values()) {
        //     String command = cmd.toString().toLowerCase();
        //     if(input.contains(command)) {
        //         input.replace(command, "");

        //         functions.add(displayOnScreen(input));
        //         displayOnScreen(input);
        //     }

        // }
    }

    private static void executLambda(String input, Consumer<String> lambda) {
        lambda.accept(input);
    }

    private static void writeToFile(String nameOfFile, String content, Boolean appendToFile) {
        try {
            FileWriter w = new FileWriter(nameOfFile, appendToFile);
            w.write("\n"+content);
            w.close();
            System.out.println("Wrote to file successfully.");
          } catch (IOException e) {
            System.out.println("Error during writing to file: ");
            e.printStackTrace();
          }
    }

    private static User readFile() throws IOException{
        BufferedReader reader = new BufferedReader(new FileReader("input.txt"));

        String userName = reader.readLine();
        String hostName = reader.readLine();
        String path = reader.readLine();

        System.out.println(String.format("Environment Variables: %s, %s, %s", userName, hostName, path));

        String[] paths = path.split(",");

        reader.close();

        return new User(userName, hostName, paths);
    }

    
    public void println(String content) {
        System.out.println(content);
    }
}
