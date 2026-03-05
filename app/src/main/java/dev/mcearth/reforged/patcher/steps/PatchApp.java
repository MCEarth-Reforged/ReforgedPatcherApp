package dev.mcearth.reforged.patcher.steps;

import androidx.preference.PreferenceManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import dev.mcearth.reforged.patcher.MainActivity;
import dev.mcearth.reforged.patcher.R;
import dev.mcearth.reforged.patcher.utils.AndroidUtils;
import dev.mcearth.reforged.patcher.utils.LoggedRunnable;
import dev.mcearth.reforged.patcher.utils.StorageLocations;
import lombok.SneakyThrows;

public class PatchApp extends LoggedRunnable {
    private static final int urlMax = 27;

    @SneakyThrows
    @Override
    public void run() {
        logEventListener.onLogLine(MainActivity.getAppContext().getResources().getString(R.string.step_patch_server));
        String serverAddress = PreferenceManager.getDefaultSharedPreferences(MainActivity.getAppContext()).getString("locator_server", "http://192.168.1.82:8080");

        // Make sure we have http or https
        if (!serverAddress.matches("^(http|https)://.*$")) {
            serverAddress = "https://" + serverAddress;
        }

        // Remove trailing slash
        if (serverAddress.endsWith("/")) {
            serverAddress = serverAddress.substring(0, serverAddress.length() - 1);
        }

        // Check the url length
        if (serverAddress.length() > urlMax) {
            throw new IndexOutOfBoundsException("Server address too long (" + serverAddress.length() + ">" + urlMax + ")");
        }

        serverAddress = String.format("%1$-" + 27 + "s", serverAddress).replaceAll(" ", "\0");

        try (RandomAccessFile raf = new RandomAccessFile(StorageLocations.getOutDir().resolve("lib/arm64-v8a/libgenoa.so").toString(), "rw")) {
            // Write server address
            raf.seek(0x0514D05D);
            raf.write(serverAddress.getBytes());

            // Patch sunset check for 0.33.0
            raf.seek(0x22A6DC8);
            raf.write(0x540005CB); // asm: b.ge -> b.lt
        }

        // Apply patches using system patch command with -p1
        File[] files = StorageLocations.getPatchDir().toFile().listFiles();

        if (files == null) {
            logEventListener.onLogLine("No patches found");
            return;
        }

        Arrays.sort(files); // Fix patch ordering on some devices

        for (final File file : files) {
            if (!file.getName().endsWith(".patch")) {
                continue;
            }

            logEventListener.onLogLine(MainActivity.getAppContext().getResources().getString(R.string.step_patch_install, file.getName()));

            try {
                applyPatchWithSystemCommand(file);
            } catch (Exception e) {
                logEventListener.onLogLine("Failed to apply patch " + file.getName() + ": " + e.getMessage());
                throw e;
            }
        }
        logEventListener.onLogLine(MainActivity.getAppContext().getResources().getString(R.string.step_done));
    }

    /**
     * Apply a patch file using the system 'patch -p1' command
     */
    private void applyPatchWithSystemCommand(File patchFile) throws IOException, InterruptedException {
        Path outputPath = StorageLocations.getOutDir();
        
        // Build the patch command
        ProcessBuilder pb = new ProcessBuilder("patch", "-p1", "-i", patchFile.getAbsolutePath());
        pb.directory(outputPath.toFile());
        pb.redirectErrorStream(true);
        
        // Start the process
        Process process = pb.start();
        
        // Read the output
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                logEventListener.onLogLine("patch: " + line);
            }
        }
        
        // Wait for the process to complete
        int exitCode = process.waitFor();
        
        if (exitCode != 0) {
            throw new RuntimeException("Patch command failed with exit code " + exitCode + "\nOutput: " + output.toString());
        }
        
        logEventListener.onLogLine("Successfully applied patch: " + patchFile.getName());
    }
}
