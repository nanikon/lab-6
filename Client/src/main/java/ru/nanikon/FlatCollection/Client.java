package ru.nanikon.FlatCollection;

import ru.nanikon.FlatCollection.arguments.AbstractArgument;
import ru.nanikon.FlatCollection.arguments.EnumArgument;
import ru.nanikon.FlatCollection.arguments.ObjectArgument;
import ru.nanikon.FlatCollection.commands.Command;
import ru.nanikon.FlatCollection.commands.ExitCommand;
import ru.nanikon.FlatCollection.commands.HistoryCommand;
import ru.nanikon.FlatCollection.exceptions.NotPositiveNumberException;
import ru.nanikon.FlatCollection.exceptions.ScriptException;
import ru.nanikon.FlatCollection.utils.ArgParser;
import ru.nanikon.FlatCollection.utils.Connection;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.util.*;

public class Client {
    private Socket s;
    private final Connection connection;
    private final String filename;
    public static String PS1 = "$";
    public static String PS2 = ">";
    private String endGame = "";
    HashMap<String, Command> commands;
    private final int port;
    private final String addr;

    public Client(String addr, int port, String filename) {
        this.connection = new Connection();
        System.out.println("Подключаемся к серверу...");
        this.filename = filename;
        this.port = port;
        this.addr = addr;
    }

    public void start() {
        for (int i = 0; i < 5; i++) {
            try {
                connection.startConnection(addr, port);
                connection.sendString(filename);
                Thread.sleep(1000);
                commands = connection.receiveMap();
                System.out.println("Подключились!");
                return;
            } catch (InterruptedException e) {
                System.out.println("Э, с потоком все хорошо должно быть!");
            } catch (ClassCastException e) {
                System.out.println("Имя файла неверное");
                connection.stopConnection();
                System.exit(0);
            } catch (IOException e) {
                System.out.println("Не удается подключится к серверу. Пробуем снова через 5 сек");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {
                }
            }
        }
        System.out.println("Кажется, сегодня сервер не встанет. Приходи в следующий раз!");
        connection.stopConnection();
        System.exit(0);
    }

    public void run() {
        endGame = ((ExitCommand) commands.get("exit")).getEnd();
        HistoryCommand history = (HistoryCommand) commands.get("history");
        Scanner scr = new Scanner(System.in);
        Stack<Scanner> scannerStack = new Stack<>();
        Stack<Path> pathStack = new Stack<>();
        offer:
        while (true) {
            if (pathStack.isEmpty()) {
                System.out.print(PS1);
            }
            String[] line;
            try {
                line = scr.nextLine().trim().split("[ \t\f]+");
            } catch (NoSuchElementException e) {
                System.out.println("Вы завершили выполнение программы");
                break;
            }
            String nameCommand = line[0];
            int i = 1;
            try {
                if (commands.containsKey(nameCommand)) {
                    Command command = commands.get(nameCommand);
                    AbstractArgument<?>[] listArg = command.getArgs();
                    for (AbstractArgument arg : listArg) {
                        if (arg.isObject()) {
                            if (i < line.length) {
                                if (!pathStack.isEmpty()) {
                                    throw new ScriptException("Ошибка в скрипте! Введено слишком много аргументов!");
                                }
                                System.out.println("Вы ввели слишком много аргументов. Попробуйте ещё раз");
                                continue offer;
                            }
                            ArgParser.parseObject((ObjectArgument<?>) arg, scr, pathStack.isEmpty(), nameCommand.equals("update"));
                        } else if (arg.isEnum()) {
                            try {
                                ArgParser.parseEnum((EnumArgument<?>) arg, line[i++], scr, pathStack.isEmpty());
                            } catch (IndexOutOfBoundsException e) {
                                if (!pathStack.isEmpty()) {
                                    throw new ScriptException("Ошибка в скрипте! Введены не все аргументы");
                                }
                                System.out.println("Вы ввели не все аргументы. Попробуйте ещё раз");
                                continue offer;
                            }
                        } else {
                            try {
                                ArgParser.parseLine(arg, line[i++]);
                            } catch (IndexOutOfBoundsException e) {
                                if (!pathStack.isEmpty()) {
                                    throw new ScriptException("Ошибка в скрипте! Введены не все аргументы");
                                }
                                System.out.println("Вы ввели не все аргументы. Попробуйте ещё раз");
                                continue offer;
                            } catch (IOException | NullPointerException | NumberFormatException | NotPositiveNumberException e) {
                                if (!pathStack.isEmpty()) {
                                    throw new ScriptException("Ошибка в скрипте! " + e.getMessage());
                                }
                                System.out.println(e.getMessage());
                                continue offer;
                            }
                        }
                    }
                    if (i < line.length) {
                        if (!pathStack.isEmpty()) {
                            throw new ScriptException("Ошибка в скрипте! Введено слишком много аргументов!");
                        }
                        System.out.println("Вы ввели слишком много аргументов. Попробуйте ещё раз");
                    } else {
                        scannerStack.push(scr);
                        history.putCommand(nameCommand);
                        try {
                            connection.sendCommand(command);
                            Thread.sleep(1000);
                            String answer = connection.receive();
                            System.out.println(answer);
                            if (answer.equals(endGame)) {
                                break;
                            }
                        } catch (IOException e) {
                            System.out.println("Упс, сервер отвалился и эту команду отправить не удалось. Если удастся переподключиться, вам придётся её повторить.");
                            start();
                        }
                        scr = scannerStack.pop();
                    }
                } else if (nameCommand.equals("execute_script")) {
                    if (line.length != 2) {
                        if (pathStack.isEmpty()) {
                            System.out.println("Вы ввели не все аргументы или же слишком много. Попробуйте ещё раз");
                        } else {
                            throw new ScriptException("Ошибка в скрипте! Введены не все аргументы или же их влишком много");
                        }
                    } else {
                        scannerStack.push(scr);
                        execute_script(line[1], pathStack, scannerStack);
                        history.putCommand(nameCommand);
                        scr = scannerStack.pop();
                    }
                } else if (!nameCommand.equals("")) {
                    if (pathStack.isEmpty()) {
                        System.out.println("Команды с именем " + nameCommand + " не существует. Проверьте правильность написания и попробуйте ещё раз.");
                    } else {
                        throw new ScriptException("Ошибка в скрипте: команды с именем " + nameCommand + " не существует.");
                    }
                }
            } catch (ScriptException e) {
                System.out.println(e.getMessage());
                System.out.println("Введите +, если хотите продолжить работу со скриптом, пропустив эту команду, и -, если хотите завершить выполнение скрипта");
                boolean answer = ArgParser.parseYesNot();
                if (answer) {
                    StringJoiner findCommand = new StringJoiner("|");
                    for (String name : commands.keySet()) {
                        findCommand.add(name);
                    }
                    while (!scr.hasNext(findCommand.toString()) & scr.hasNextLine()) {
                        scr.nextLine();
                    }
                } else {
                    scr.close();
                    scr = scannerStack.pop();
                    Path path = pathStack.pop();
                    System.out.println("Завершена работа файла " + path.getFileName());
                }
            } catch (OutOfMemoryError | StackOverflowError e) {
                System.out.println("Программа дошла до переполнения кучи");
            } catch (InterruptedException ignored) {
            }
            while (!(pathStack.isEmpty() || scr.hasNextLine())) {
                scr.close();
                scr = scannerStack.pop();
                Path path = pathStack.pop();
                System.out.println("Завершена работа файла " + path.getFileName());
            }
        }
        connection.stopConnection();
        System.exit(0);
    }

    public void execute_script(String fileName, Stack<Path> pathStack, Stack<Scanner> scannerStack) {
        File file = new File(fileName);
        Enumeration<Path> enu = pathStack.elements();
        int entry = 0;
        while (enu.hasMoreElements()) {
            if (enu.nextElement().equals(file.toPath())) {
                entry++;
                if (entry == 2) {
                    break;
                }
            }
        }
        if (entry == 1) {
            System.out.println("Обнаружен рекурсивный вызов файла " + fileName + ". Введите +, если желаете продолжить рекурсию, но тогда приложение может упасть. Введите -, если хотите пропустить эту команду и продолжить дальше выполнение скрипта.");
            boolean answer = ArgParser.parseYesNot();
            if (!answer) {
                System.out.println("Команда вызова скрипта пропущена, скрипт выполняется дальше");
            }
        }
        pathStack.push(file.toPath());
        try {
            scannerStack.push(new Scanner(file, "UTF-8"));
        } catch (FileNotFoundException e) {
            Path path = pathStack.pop();
            System.out.println("Не найден исполняемый файл " + path.getFileName());
            return;
        }
        System.out.println("Начинается выполнение файла " + fileName);
    }
}
