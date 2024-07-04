package com.mindera.mindswap.client.commands;

import com.mindera.mindswap.client.Client;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.mindera.mindswap.Constants.SOUNDS_FILE_PATH;

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
        ExecutorService threads = Executors.newCachedThreadPool();
        Thread test = new Thread(() -> {
            playSound(SOUNDS_FILE_PATH + "tada.wav");
        });
        threads.submit(test);

        test = new Thread(() -> {
            playSound(SOUNDS_FILE_PATH + "laugh.wav");
        });
        threads.submit(test);
        System.out.println(winner);
    }

    private void playSound(String filePath) {
        // Create an AudioInputStream object
        try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(filePath))) {

            // Get an AudioFormat object for the specified AudioInputStream
            AudioFormat format = audioStream.getFormat();

            // Get a DataLine.Info object for the specified AudioFormat
            DataLine.Info info = new DataLine.Info(Clip.class, format);

            // Create a Clip object for playing the sound
            Clip audioClip = (Clip) AudioSystem.getLine(info);

            // Open the audio clip and load samples from the audio input stream
            audioClip.open(audioStream);

            // Play the audio clip
            audioClip.start();

            // Keep the program running until the audio clip finishes playing
            while (audioClip.isRunning()) {
                Thread.sleep(1000);
            }

            // Close the audio clip
            //audioClip.close();

        } catch (UnsupportedAudioFileException e) {
            System.err.println("The specified audio file is not supported.");
        } catch (LineUnavailableException e) {
            System.err.println("Audio line for playing the audio file is unavailable.");
        } catch (IOException e) {
            System.err.println("Error playing the audio file.");
        } catch (InterruptedException e) {
            System.err.println("Playback interrupted.");
        }
    }

}
