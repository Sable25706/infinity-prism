package me.lyric.infinity.manager.client;

import me.lyric.infinity.Infinity;
import me.lyric.infinity.api.util.minecraft.chat.ChatUtils;
import me.lyric.infinity.impl.modules.client.Internals;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public class ThreadManager {

    public ExecutorService executorService;
    public int num = 2;

    public static ScheduledExecutorService newDaemonScheduledExecutor(String name) {
        ThreadFactoryBuilder factory = newDaemonThreadFactoryBuilder();
        factory.setNameFormat("Infinity-" + name + "-%d");
        return Executors.newSingleThreadScheduledExecutor(factory.build());
    }
    public static ThreadFactoryBuilder newDaemonThreadFactoryBuilder() {
        ThreadFactoryBuilder factory = new ThreadFactoryBuilder();
        factory.setDaemon(true);
        return factory;
    }
    public void init()
    {
        MinecraftForge.EVENT_BUS.register(this);
        executorService = Executors.newFixedThreadPool(num);
    }
    public void reload()
    {
        executorService.shutdownNow();
        executorService = Executors.newFixedThreadPool(num);
        ChatUtils.sendMessage("Thread pool reloaded.");
    }
    @SubscribeEvent
    public void onUpdate(LivingEvent.LivingUpdateEvent e)
    {
        num = Infinity.INSTANCE.moduleManager.getModuleByClass(Internals.class).threadCount.getValue();
    }

    public void run(Runnable command) {
        try {
            executorService.execute(command);
        } catch (Exception e){
            Infinity.LOGGER.error("Error in thread executor!" + " " + e.getMessage());
        }
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public static class ThreadFactoryBuilder {
        private Boolean daemon;
        private String nameFormat;

        public ThreadFactoryBuilder setDaemon(boolean daemon) {
            this.daemon = daemon;
            return this;
        }

        public ThreadFactoryBuilder setNameFormat(String nameFormat) {
            this.nameFormat = nameFormat;
            return this;
        }

        public ThreadFactory build() {
            Boolean daemon = this.daemon;
            String nameFormat = this.nameFormat;
            AtomicLong id = (nameFormat != null) ? new AtomicLong(0) : null;
            return r -> {
                Thread thread = Executors.defaultThreadFactory().newThread(r);
                if (daemon != null) {
                    thread.setDaemon(daemon);
                }

                if (nameFormat != null) {
                    thread.setName(format(nameFormat, id.getAndIncrement()));
                }

                return thread;
            };
        }

        private static String format(String format, Object... args) {
            return String.format(Locale.ROOT, format, args);
        }

    }
}
