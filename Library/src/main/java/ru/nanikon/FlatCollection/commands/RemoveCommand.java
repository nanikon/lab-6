package ru.nanikon.FlatCollection.commands;

import ru.nanikon.FlatCollection.arguments.AbstractArgument;
import ru.nanikon.FlatCollection.arguments.IntArg;
import ru.nanikon.FlatCollection.utils.CollectionManager;

import java.io.Serializable;

/**
 * delete an item from the collection by its id
 */
public class RemoveCommand implements Command, Serializable {
    private AbstractArgument<?>[] params = {new IntArg()};
    private String information = "'remove_by_id id' - удалить элемент из коллекции по его id";

    public RemoveCommand() {
    }

    /**
     * running the command
     */
    @Override
    public String execute(CollectionManager collection) {
        int id = ((IntArg) params[0]).getValue() - 1;
        try {
            collection.removeById(id);
        } catch (IndexOutOfBoundsException e) {
            return "Элемента с таким id не найденно. Проверьте коллекцию и попробуйте ещё раз";
        }
        return "Успешно удален элемент с id " + (id + 1) + "\nКоллекция:\n" + collection.toLongString();
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
        return "remove_by_id";
    }

    /**
     * @return - returns the help for the command. For help command
     */
    @Override
    public String getInformation() {
        return information;
    }
}
