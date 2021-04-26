package ru.nanikon.FlatCollection.commands;

import ru.nanikon.FlatCollection.arguments.AbstractArgument;
import ru.nanikon.FlatCollection.arguments.TransportArg;
import ru.nanikon.FlatCollection.data.Transport;
import ru.nanikon.FlatCollection.utils.CollectionManager;

import java.io.Serializable;

/**
 * remove one element from the collection whose transport field value is equivalent to the specified one
 */
public class RemoveAnyByTransportCommand implements Command, Serializable {
    private AbstractArgument<?>[] params = {new TransportArg()};
    private String information = "'remove_any_by_transport transport' - удалить из коллекции один элемент, значение поля transport которого эквивалентно заданному";

    public RemoveAnyByTransportCommand() {
    }

    /**
     * running the command
     */
    @Override
    public String execute(CollectionManager collection) {
        Transport transport = ((TransportArg) params[0]).getValue();
        String result;
        if (collection.getSize() == 0) {
            result = collection.removeByTransport(transport) + " так как коллекция пустая";
        } else {
            result = collection.removeByTransport(transport) + "\nКоллекция:\n" + collection.toLongString();
        }
        return result;
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
        return "remove_any_by_transport";
    }

    /**
     * @return - returns the help for the command. For help command
     */
    @Override
    public String getInformation() {
        return information;
    }
}

