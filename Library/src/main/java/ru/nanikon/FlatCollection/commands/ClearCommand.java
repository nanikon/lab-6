package ru.nanikon.FlatCollection.commands;

import ru.nanikon.FlatCollection.arguments.AbstractArgument;
import ru.nanikon.FlatCollection.utils.CollectionManager;

import java.io.Serializable;

/**
 * Clear collection
 */
public class ClearCommand implements Command, Serializable {
    private AbstractArgument<?>[] params = {};
    private String information = "'clear' - очистить коллекцию";

    public ClearCommand() {
    }

    /**
     * running the command
     */
    @Override
    public String execute(CollectionManager collection) {
        collection.clearCollection();
        return "Коллекция успешно очищена";
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
        return "clear";
    }

    /**
     * @return - returns the help for the command. For help command
     */
    @Override
    public String getInformation() {
        return information;
    }
}
