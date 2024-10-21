import java.io.*;

/*
 * Class containing the main method to read input from the user
 * and pass it to the SRPN program for processing.
 * You do not need to edit anything in this file.
 */

public class Main {

    /* Main method repeatedly reads input from the user and passes it to SRPN */
    public static void main(String[] args) {

        SRPN srpn = new SRPN(); //Creates a new instance of the SRPN class

        /*
         * Code to take input from the command line.
         * We'll cover this and the try/catch statements below in a future week.
         * This input is passed to the processCommand() method in SRPN.java
         */
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        try {
            while(true) {
                String command = reader.readLine();
                //Close on an End-of-file (EOF) (Ctrl-D on the terminal)
                if(command == null){
                    //Exit code 0 for a graceful exit
                    System.exit(0);
                }
                //Otherwise, (attempt to) process the character
                srpn.processCommand(command);
            }
        }
        catch(IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}