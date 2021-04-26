package ru.nanikon.FlatCollection.commands;


import ru.nanikon.FlatCollection.arguments.AbstractArgument;
import ru.nanikon.FlatCollection.utils.CollectionManager;

import java.io.Serializable;

/**
 * Calculates the average value of the numberOfRooms field for the entire collection
 */
public class AverageOfNumberOfRoomsCommand implements Command, Serializable {
    private AbstractArgument<?>[] params = {};
    private String information = "'average_of_number_of_rooms' - вывести среднее значение поля numberOfRooms для всех элементов коллекции";

    public AverageOfNumberOfRoomsCommand() {

    }

    /**
     * running the command
     */
    @Override
    public String execute(CollectionManager collection) {
        return "Среднее значение поля количество комнат по всем квартирам коллекции: " + collection.getAverageNumberOfRooms();
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
        return "average_of_number_of_rooms";
    }

    /**
     * @return - returns the help for the command. For help command
     */
    @Override
    public String getInformation() {
        return information;
    }
}
