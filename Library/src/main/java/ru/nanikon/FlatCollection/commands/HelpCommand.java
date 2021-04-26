package ru.nanikon.FlatCollection.commands;

import ru.nanikon.FlatCollection.arguments.AbstractArgument;
import ru.nanikon.FlatCollection.utils.CollectionManager;

import java.io.Serializable;
import java.util.HashMap;

/**
 * display help for available commands
 */
public class HelpCommand implements Command, Serializable {
    private HashMap<String, Command> commands;
    private AbstractArgument<?>[] params = {};
    private String information = "'help' - вывести справку по доступным командам";
    private HashMap<String, AbstractArgument<?>> args;

    public HelpCommand(HashMap<String, Command> commands) {
        this.commands = commands;
    }

    /**
     * running the command
     */
    @Override
    public String execute(CollectionManager collection) {
        StringBuilder result = new StringBuilder();
        result.append("Справка по командам:").append("\n");
        for (String nameCommand : commands.keySet()) {
            result.append(commands.get(nameCommand).getInformation()).append("\n");
        }
        return result.toString().trim();
    }

    /**
     * @return - returns the help for the command. For help command
     */
    @Override
    public String getInformation() {
        return information;
    }

    /**
     * @return - returns the list of arguments required for the command to work, which must be obtained from the user
     */
    @Override
    public AbstractArgument<?>[] getArgs() {
        return params;
    }

    @Override
    public String getName() {
        return "help";
    }
}
