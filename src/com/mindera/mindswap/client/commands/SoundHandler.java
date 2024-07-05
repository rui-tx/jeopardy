package com.mindera.mindswap.client.commands;

import com.mindera.mindswap.client.Client;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.mindera.mindswap.Constants.SOUNDS_FILE_PATH;

/**
 * Handler for the sound command.
 */
public class SoundHandler implements CommandHandler {

    /**
     * Executes the sound command by playing the specified sound.
     *
     * @param client the client on which the command will be executed
     */
    @Override
    public void execute(Client client) {
        ExecutorService threads = Executors.newSingleThreadExecutor();

        if (client.getLastCommand() != null && client.getLastCommand()[0].equals("/sound")) {
            switch (client.getLastCommand()[1]) {
                case "win":
                    threads.submit(new Thread(() -> {
                        playSound(SOUNDS_FILE_PATH + "roundwinner.wav");
                    }));
                    break;
                case "lost":
                    threads.submit(new Thread(() -> {
                        playSound(SOUNDS_FILE_PATH + "laugh.wav");
                    }));
                    break;
                case "turn":
                    threads.submit(new Thread(() -> {
                        playSound(SOUNDS_FILE_PATH + "yourturn.wav");
                    }));
                    break;
                case "roundwinner":
                    threads.submit(new Thread(() -> {
                        playSound(SOUNDS_FILE_PATH + "tada.wav");
                    }));
                    break;
                case "cue1":
                    threads.submit(new Thread(() -> {
                        playSound(SOUNDS_FILE_PATH + "cue1.wav");
                    }));
                    break;
                case "intro":
                    threads.submit(new Thread(() -> {
                        playSound(SOUNDS_FILE_PATH + "intro.wav");
                    }));
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Plays the specified sound file.
     *
     * @param filePath the path to the sound file
     */
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
