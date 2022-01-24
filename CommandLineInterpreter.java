import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

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
        
        System.out.println(USER.getUserNameWithHost());
        while((input = scanner.next()) != null) {
            System.out.println("User inputted: " + input);

            if(input.equalsIgnoreCase("exit")) {
                break;
            }

            Runtime rt = Runtime.getRuntime();

            String[] command = input.split(" ");

            Process process = rt.exec(command);

            try {
                InputStreamReader sr = new InputStreamReader(process.getInputStream());
                BufferedReader reader = new BufferedReader(sr);

                reader.lines().forEach(System.out::println);
            } catch(Exception e) {
                e.printStackTrace();
            }

            //process.waitFor();

            try {
                InputStreamReader sr = new InputStreamReader(process.getErrorStream());
                BufferedReader reader = new BufferedReader(sr);

                reader.lines().forEach(System.out::println); 
            } catch(Exception e) {
                new InputStreamReader(process.getErrorStream());
            }
        }
        
        scanner.close();
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
}
