package Hilligans.Client.Rendering;

import Hilligans.Util.Settings;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientUtil {


    public static ExecutorService chunkBuilder;

    public static void register() {
        chunkBuilder = Executors.newSingleThreadExecutor();
    }





}
