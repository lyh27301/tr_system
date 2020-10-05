package Client;

import java.util.StringTokenizer;
import java.util.Vector;

public class TestStr {
    public static void main(String[] args) {
        
        System.out.println("Message Passing - From Client to RM");

        String userinput = "AddCars, 2, mtl, 34, 500";
        String command = userinput.trim();
        System.out.println("\nUser Input: " + userinput);
        System.out.println("\nTrimmed command: " + command);

        Vector<String> arguments = parse(command);
        
        System.out.println("\narguments.get(0): -" + arguments.get(0) + "-");

        String msg0 = arguments.toString();

        System.out.println("\nMessage from client to middleware (String): \"" + msg0 + "\"");

        Vector<String> parsedCommandMW = parseRM(arguments.toString());
        System.out.println("\nMessage parsed in middleware (Vector<String>): " + parsedCommandMW.toString());

        int id = 2;
        String location = "mtl";
        int numCars = 34;
        int price = 500;
        String command2 = String.format("AddCars, %d, %s, %d, %d", id, location, numCars, price);
        System.out.println("\nMessage from middleware to RM (String): \"" + command2 + "\"");

        Vector<String> parsedCommand = parseRM(command2);
        System.out.println("\nMessage parsed in RM (Vector<String>): " + parsedCommand.toString());
        System.out.println("\nparsedMsg.get(0): -" + parsedCommand.get(0) + "-");

    }

    public static Vector<String> parse(String command)
	{
		Vector<String> arguments = new Vector<String>();
		StringTokenizer tokenizer = new StringTokenizer(command,",");
		String argument = "";
		while (tokenizer.hasMoreTokens())
		{
			argument = tokenizer.nextToken();
			argument = argument.trim();
			arguments.add(argument);
		}
		return arguments;
    }
    
    public static Vector<String> parseRM(String input) {
        if (input != null && input.length() != 0) {

            String command;

            if (input.charAt(0) == '[' && input.charAt(input.length() - 1) == ']') {
                // If there are brackets, remove them.
                command = input.substring(1, input.length() - 1);
            } else {
                command = input;
            }
            Vector<String> arguments = new Vector<String>();
            String[] commandParts = command.split(", ");
            for (int i = 0; i < commandParts.length; i++) {
                arguments.add(commandParts[i].trim());
            }

            return arguments;
        }

        return null;

    }
}
