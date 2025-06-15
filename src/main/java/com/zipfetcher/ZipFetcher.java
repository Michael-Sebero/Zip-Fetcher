package com.zipfetcher;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = ZipFetcher.MODID, name = ZipFetcher.NAME, version = ZipFetcher.VERSION)
public class ZipFetcher {
    public static final String MODID = "zipfetcher";
    public static final String NAME = "Zip Fetcher";
    public static final String VERSION = "1.0.0";

    private static Logger logger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        logger.info("Zip Fetcher mod loading...");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // Start the zip fetching process
        ZipFetcherCore.initialize(logger);
    }

    public static Logger getLogger() {
        return logger;
    }
}
