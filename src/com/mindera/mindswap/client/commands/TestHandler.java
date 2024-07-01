package com.mindera.mindswap.client.commands;

import com.mindera.mindswap.client.Client;

public class TestHandler implements CommandHandler {
    @Override
    public void execute(Client client) {

        String title;
        title = "   _                                _       " + "\n";
        title += "  (_)                              | |      " + "\n";
        title += "   _  ___  ___  _ __   __ _ _ __ __| |_   _ " + "\n";
        title += "  | | |/ _ \\/ _ \\| '_ \\ / _` | '__/ _` | | | |" + "\n";
        title += "  | | |  __/ (_) | |_) | (_| | | | (_| | |_| |" + "\n";
        title += "  | | |\\___|\\___/| .__/ \\__,_|_|  \\__,_|\\__, |" + "\n";
        title += " _/ |          | |                     __/ |" + "\n";
        title += "|__/           |_|                    |___/ " + "\n";

        System.out.println(title);

        String gameBoard;
        gameBoard = " ________________________________________________________________ " + "\n";
        gameBoard += "| SCIENCE       | HISTORY       | SPORTS        | LITERATURE    |" + "\n";
        gameBoard += "|---------------|---------------|---------------|---------------|" + "\n";
        gameBoard += "|     $100      |     ----      |     $100      |     $100      |" + "\n";
        gameBoard += "| Question n 1  |     ----      | Question n 3  | Question n 4  |" + "\n";
        gameBoard += "|_______________|_______________|_______________|_______________|" + "\n";
        gameBoard += "|     $200      |     $200      |     $200      |     $200      |" + "\n";
        gameBoard += "| Question n 5  | Question n 6  | Question n 7  | Question n 8  |" + "\n";
        gameBoard += "|_______________|_______________|_______________|_______________|" + "\n";
        gameBoard += "|     $300      |     $300      |     ----      |     $300      |" + "\n";
        gameBoard += "| Question n 9  | Question n 10 |     ----      | Question n 12 |" + "\n";
        gameBoard += "|_______________|_______________|_______________|_______________|" + "\n";
        gameBoard += "|     ----      |     $400      |     $400      |     $400      |" + "\n";
        gameBoard += "|     ----      | Question n 14 | Question n 15 | Question n 16 |" + "\n";
        gameBoard += "|_______________|_______________|_______________|_______________|" + "\n";

        System.out.println(gameBoard);

        String winner =
                """
                         __     __          __          ___       _\s
                         \\ \\   / /          \\ \\        / (_)     | |
                          \\ \\_/ /__  _   _   \\ \\  /\\  / / _ _ __ | |
                           \\   / _ \\| | | |   \\ \\/  \\/ / | | '_ \\| |
                            | | (_) | |_| |    \\  /\\  /  | | | | |_|
                            |_|\\___/ \\__,_|     \\/  \\/   |_|_| |_(_)
                        """;

        System.out.println(winner);

        String lost =
                "__   __            _              _   _ \n" +
                        "\\ \\ / /__  _   _  | |    ___  ___| |_| |\n" +
                        " \\ V / _ \\| | | | | |   / _ \\/ __| __| |\n" +
                        "  | | (_) | |_| | | |__| (_) \\__ \\ |_|_|\n" +
                        "  |_|\\___/ \\__,_| |_____\\___/|___/\\__(_)\n";

        System.out.println(lost);

    }
}
