package com.mindera.mindswap;

import java.io.File;

public class Constants {
    public static final String QUESTIONS_FILE_PATH = "src/com/mindera/mindswap/resources/questionsDB.csv";
    public static final String ANSWERS_FILE_PATH = "src/com/mindera/mindswap/resources/answersDB.csv";
    public static final int BOARD_SIZE = 4;
    public static final String SOUNDS_FILE_PATH = "src" + File.separator + "com" + File.separator + "mindera" +
            File.separator + "mindswap" + File.separator + "resources" + File.separator +
            "sounds" + File.separator;

    public static final String BANNER =
            "   _                                _       " + "\n" +
                    "  (_)                              | |      " + "\n" +
                    "   _  ___  ___  _ __   __ _ _ __ __| |_   _ " + "\n" +
                    "  | | |/ _ \\/ _ \\| '_ \\ / _` | '__/ _` | | | |" + "\n" +
                    "  | | |  __/ (_) | |_) | (_| | | | (_| | |_| |" + "\n" +
                    "  | | |\\___|\\___/| .__/ \\__,_|_|  \\__,_|\\__, |" + "\n" +
                    " _/ |          | |                     __/ |" + "\n" +
                    "|__/           |_|                    |___/ " + "\n";

    public static final String WON = """
             __     __          __          ___       _\s
             \\ \\   / /          \\ \\        / (_)     | |
              \\ \\_/ /__  _   _   \\ \\  /\\  / / _ _ __ | |
               \\   / _ \\| | | |   \\ \\/  \\/ / | | '_ \\| |
                | | (_) | |_| |    \\  /\\  /  | | | | |_|
                |_|\\___/ \\__,_|     \\/  \\/   |_|_| |_(_)
            """;

    public static final String LOST =
            "__   __            _              _   _ \n" +
                    "\\ \\ / /__  _   _  | |    ___  ___| |_| |\n" +
                    " \\ V / _ \\| | | | | |   / _ \\/ __| __| |\n" +
                    "  | | (_) | |_| | | |__| (_) \\__ \\ |_|_|\n" +
                    "  |_|\\___/ \\__,_| |_____\\___/|___/\\__(_)\n";
}
