package dev.mcearth.reforged.patcher.steps;

import brut.androlib.Androlib;
import brut.androlib.ApkOptions;
import dev.mcearth.reforged.patcher.MainActivity;
import dev.mcearth.reforged.patcher.R;
import dev.mcearth.reforged.patcher.utils.AndroidUtils;
import dev.mcearth.reforged.patcher.utils.LoggedRunnable;
import dev.mcearth.reforged.patcher.utils.StorageLocations;
import dev.mcearth.reforged.patcher.utils.UILogger;
import lombok.SneakyThrows;

public class ApkRecompile extends LoggedRunnable {
    @SneakyThrows
    @Override
    public void run() {
        // TODO: Close the logger or somehow cleanup the object
        UILogger libLogger = new UILogger(Androlib.class.getName(),null);
        libLogger.setLogEventListener(logEventListener);
        AndroidUtils.setFinalStatic(Androlib.class.getDeclaredField("LOGGER"), libLogger);

        ApkOptions apkOptions = new ApkOptions();
        apkOptions.frameworkFolderLocation = StorageLocations.getFrameworkDir();
        apkOptions.aaptPath = StorageLocations.getAaptExec().getAbsolutePath();

        new Androlib(apkOptions).build(StorageLocations.getOutDir().toFile(), StorageLocations.getOutFile());
        logEventListener.onLogLine(MainActivity.getAppContext().getResources().getString(R.string.step_done));
    }
}
