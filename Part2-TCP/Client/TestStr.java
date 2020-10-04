package Client;

import java.util.StringTokenizer;
import java.util.Vector;

public class TestStr {
    public static void main(String[] args) {
        String command = "QueryCars, 1, mtl";
        Vector<String> arguments = parse(command);
        
        System.out.println();
        System.out.println(command + " -> tokenizer-Vector<String>.toString-> " + arguments.toString());

        int id = 2;
        String location = "mtl";
        int numCars = 34;
        int price = 500;
        String command2 = String.format("AddCars,%d,%s,%d,%d", id, location, numCars, price);
        System.out.println("String.format(\"AddCars,%d,%s,%d,%d\", id, location, numCars, price)" + " -> " + command2);

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
}
