package ru.nanikon.FlatCollection.commands;

import ru.nanikon.FlatCollection.arguments.AbstractArgument;
import ru.nanikon.FlatCollection.utils.CollectionManager;

/**
 * Universal interface for all teams
 */
public interface Command {
    /**
     * running the command
     */
    String execute(CollectionManager collection);

    /**
     * @return - returns the help for the command. For help command
     */
    String getInformation();

    /**
     * @return - returns the list of arguments required for the command to work, which must be obtained from the user
     */
    AbstractArgument<?>[] getArgs();

    void putArg(String name, AbstractArgument<?> arg);
}
