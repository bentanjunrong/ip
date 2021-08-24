import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Duke {
    private static final String LINEBREAK = "____________________________________________________________";
    List<Task> tasks = new ArrayList<Task>();

    private final Map<String, ThrowingConsumer<String>> FUNCTIONS = new HashMap<>() {
        {
            put("list", (input) -> list());
            put("done", (input) -> markDone(input));
            put("todo", (input) -> addToDo(input));
            put("deadline", (input) -> addDeadline(input));
            put("event", (input) -> addEvent(input));
            put("delete", (input) -> deleteTask(input));
        }
    };

    private void print(String s) {
        System.out.printf("%s\n%s\n%s\n", LINEBREAK, s, LINEBREAK);
    }

    private void greet() {
        System.out.print("               _,........__\n");
        System.out.print("            ,-'            \"`-.\n");
        System.out.print("          ,'                   `-.\n");
        System.out.print("        ,'                        \\\n");
        System.out.print("      ,'                           .\n");
        System.out.print("      .'\\               ,\"\".       `\n");
        System.out.print("     ._.'|             / |  `       \\\n");
        System.out.print("     |   |            `-.'  ||       `.\n");
        System.out.print("     |   |            '-._,'||       | \\\n");
        System.out.print("     .`.,'             `..,'.'       , |`-.\n");
        System.out.print("     l                       .'`.  _/  |   `.\n");
        System.out.print("     `-.._'-   ,          _ _'   -\" \\  .     `\n");
        System.out.print("`.\"\"\"\"\"'-.`-...,---------','         `. `....__.\n");
        System.out.print(".'        `\"-..___      __,'\\          \\  \\     \\\n");
        System.out.print("\\_ .          |   `\"\"\"\"'    `.           . \\     \\\n");
        System.out.print("  `.          |              `.          |  .     L\n");
        System.out.print("    `.        |`--...________.'.        j   |     |\n");
        System.out.print("      `._    .'      |          `.     .|   ,     |\n");
        System.out.print("         `--,\\       .            `7\"\"' |  ,      |\n");
        System.out.print("            ` `      `            /     |  |      |    _,-'\"\"\"`-.\n");
        System.out.print("             \\ `.     .          /      |  '      |  ,'          `.\n");
        System.out.print("              \\  v.__  .        '       .   \\    /| /              \\\n");
        System.out.print("               \\/    `\"\"\\\"\"\"\"\"\"\"`.       \\   \\  /.''                |\n");
        System.out.print("                `        .        `._ ___,j.  `/ .-       ,---.     |\n");
        System.out.print("                ,`-.      \\         .\"     `.  |/        j     `    |\n");
        System.out.print("               /    `.     \\       /         \\ /         |     /    j\n");
        System.out.print("              |       `-.   7-.._ .          |\"          '         /\n");
        System.out.print("              |          `./_    `|          |            .     _,'\n");
        System.out.print("              `.           / `----|          |-............`---'\n");
        System.out.print("                \\          \\      |          |\n");
        System.out.print("               ,'           )     `.         |\n");
        System.out.print("                7____,,..--'      /          |\n");
        System.out.print("                                  `---.__,--.'\n");
        System.out.println(
                " BOW BEFORE ME, FOR I AM SQUIRTLE, DESTROYER OF MEN, TAKER OF LIVES.\n THE GODS FEARED MY EXISTENCE, SO THEY BANISHED ME TO YOUR MORTAL REALM TO SAVE YOUR MISERABLE LIFE.");
        System.out.println(LINEBREAK);
        System.out.print("SO WHAT DO YOU WANT DO, INSECT?\n ");
    }

    private void list() {
        System.out.println(LINEBREAK);
        System.out.println("YOU WISH FOR THESE FOOLISH THINGS:");
        for (int i = 0; i < tasks.size(); i++) {
            System.out.printf(" %s. %s\n", i + 1, tasks.get(i));
        }
        System.out.println(LINEBREAK);
    }

    private void markDone(String input) throws DukeException {
        if (tasks.isEmpty()) {
            throw new DukeException("YOU HAVE NO TASKS YOU FOOL.");
        }
        try {
            int index = Integer.parseInt(input);
            if (index < 1 || index > tasks.size()) {
                throw new DukeException("INVALID TASK NUMBER YOU FOOL.");
            }
            Task currentTask = tasks.get(index - 1);
            if (currentTask.getIsDone()) {
                print("YOU ALREADY DID THIS YOU FOOL.");
                return;
            }
            currentTask.setIsDone(true);
            print("YOU SAY YOU'VE COMPLETED THIS TASK:\n " + currentTask);
            saveToFile();
        } catch (NumberFormatException e) {
            throw new DukeException("DO YOU NOT KNOW WHAT A NUMBER IS, FOOLISH HUMAN?");
        }
    }

    private void deleteTask(String input) throws DukeException {
        if (tasks.isEmpty()) {
            throw new DukeException("YOU HAVE NO TASKS YOU FOOL.");
        }
        try {
            int index = Integer.parseInt(input);
            if (index < 1 || index > tasks.size()) {
                throw new DukeException("INVALID TASK NUMBER YOU FOOL.");
            }
            Task currentTask = tasks.get(index - 1);
            tasks.remove(index - 1);
            print(String.format("YOU'VE CHOSEN TO ABANDON THIS TASK:\n %s\n YOU HAVE %d TASKS LEFT.", currentTask,
                    tasks.size()));
            saveToFile();
        } catch (NumberFormatException e) {
            throw new DukeException("DO YOU NOT KNOW WHAT A NUMBER IS, FOOLISH HUMAN?");
        }
    }

    private void addToDo(String taskName) throws DukeException {
        if (taskName.equals("")) {
            throw new DukeException("NAME YOUR TASK. DON'T WASTE MY TIME.");
        }
        Task newTask = new ToDo(taskName);
        tasks.add(newTask);
        print(String.format(" MORTAL, YOU'VE ADDED THIS TASK:\n %s\n YOU HAVE %d TASKS LEFT.", newTask, tasks.size()));
        saveToFile();
    }

    private void addEvent(String input) throws DukeException {
        if (input.equals("") || !input.contains(" /at")) {
            throw new DukeException("FORMAT YOUR EVENT PROPERLY. DON'T WASTE MY TIME.");
        }
        try {
            String[] args = input.split("/at", 2);
            Task newEvent = new Event(args[0].trim(), args[1].trim());
            tasks.add(newEvent);
            print(String.format(" MORTAL, YOU'VE ADDED THIS EVENT:\n %s\n YOU HAVE %d TASKS LEFT.", newEvent,
                    tasks.size()));
        } catch (DateTimeParseException e) {
            print("FORMAT YOUR DATETIME PROPERLY YOU FOOL");
        }
    }

    private void addDeadline(String input) throws DukeException {
        if (input.equals("") || !input.contains(" /by")) {
            throw new DukeException("FORMAT YOUR DEADLINE PROPERLY. DON'T WASTE MY TIME.");
        }
        try {
            String[] args = input.split("/by", 2);
            Task newDeadline = new Deadline(args[0].trim(), args[1].trim());
            tasks.add(newDeadline);
            print(String.format(" MORTAL, YOU'VE ADDED THIS DEADLINE:\n %s\n YOU NOW HAVE %d TASKS.", newDeadline,
                    tasks.size()));
        } catch (DateTimeParseException e) {
            print("FORMAT YOUR DATETIME PROPERLY YOU FOOL");
        }
    }

    private void getTasksFromFile() throws DukeException {
        try {
            File taskFile = new File("../../../data", "duke.txt");
            Scanner myReader = new Scanner(taskFile);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                System.out.println("PENIS" + data);
                String[] args = data.split(Pattern.quote("|"));
                boolean isDone = args[1].trim().equals("1");
                switch (args[0].trim()) {
                    case "T":
                        tasks.add(new ToDo(args[2], isDone));
                        break;
                    case "E":
                        tasks.add(new Event(args[2], isDone, args[3]));
                        break;
                    case "D":
                        tasks.add(new Deadline(args[2], isDone, args[3]));
                        break;
                    default:
                        myReader.close();
                        throw new IllegalArgumentException(
                                "COULDN'T PARSE YOUR STUPID FILE. FORMAT THE INPUT PROPERLY.");
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            // Init file and directory if they do not exist
            saveToFile();
        } catch (DateTimeParseException e) {
            throw new RuntimeException("FORMAT YOUR FILE DATETIMEs PROPERLY YOU FOOL.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new DukeException("COULDN'T GET YOUR MISERABLE FILE. TRY AGAIN.");
        }
    }

    private void saveToFile() throws DukeException {
        try {
            // create directory if it does not exist
            File directory = new File("../../../data");
            directory.mkdirs();

            File myFile = new File("../../../data", "duke.txt");
            FileWriter DukeWriter = new FileWriter(myFile);
            for (Task task : tasks) {
                DukeWriter.write(String.format("%s\n", task.getFileString()));
            }
            DukeWriter.close();
        } catch (IOException e) {
            throw new DukeException("SAVING THE FILE FAILED YOU IDIOT. JUST GIVE UP.");
        }

    }

    private void serve() throws DukeException {
        getTasksFromFile();
        greet();
        Scanner input = new Scanner(System.in);

        while (true) {
            // Extract first word as command
            String inputString = input.nextLine().trim();
            String[] cmd = inputString.split(" ", 2);

            if (inputString.equals("bye")) {
                print(" LIVE OUT YOUR PATHETIC LIFE, WEAKLING.");
                break;
            } else {
                FUNCTIONS
                        .getOrDefault(cmd[0],
                                (task) -> print("FOOLISH MORTAL, I CAN'T EXECUTE THAT COMMAND. TRY AGAIN:"))
                        .accept(cmd.length > 1 ? cmd[1] : "");
            }
            System.out.print("WHAT ELSE DO YOU WANT, INSECT?\n ");
        }
        input.close();
    }

    public static void main(String[] args) throws DukeException {
        Duke Squirtle = new Duke();
        Squirtle.serve();
    }
}
