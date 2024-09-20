package dev.boarbot.interactives;

import dev.boarbot.BoarBotApp;
import dev.boarbot.api.util.Configured;
import dev.boarbot.util.interactive.StopType;
import dev.boarbot.util.logging.Log;
import dev.boarbot.util.time.TimeUtil;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class Interactive implements Configured {
    protected static final ConcurrentMap<String, Interactive> interactives = BoarBotApp.getBot().getInteractives();

    private final Future<?> future;

    @Getter protected final String interactiveID;
    @Getter protected final String guildID;

    protected long waitTime;
    protected long curStopTime;
    protected final long hardStopTime;
    protected long lastEndTime = 0;
    protected boolean isStopped = false;

    protected Interactive(String interactiveID, String guildID) {
        this(interactiveID, guildID, NUMS.getInteractiveIdle(), NUMS.getInteractiveHardStop());
    }

    protected Interactive(String interactiveID, String guildID, long waitTime, long hardStop) {
        this.interactiveID = interactiveID;
        this.guildID = guildID;
        this.waitTime = waitTime;
        this.curStopTime = TimeUtil.getCurMilli() + waitTime;
        this.hardStopTime = TimeUtil.getCurMilli() + hardStop;

        String duplicateInteractiveKey = this.findDuplicateKey();

        if (duplicateInteractiveKey != null) {
            interactives.get(duplicateInteractiveKey).stop(StopType.EXPIRED);
        }

        interactives.put(interactiveID, this);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        this.future = executor.submit(() -> this.tryStop(waitTime));
        executor.shutdown();
    }

    protected String findDuplicateKey() {
        for (String key : interactives.keySet()) {
            boolean isSameUser = this instanceof UserInteractive && key.endsWith(this.interactiveID.split(",")[1]);
            boolean isSameType = this.getClass().equals(interactives.get(key).getClass());

            if (isSameUser && isSameType) {
                return key;
            }
        }

        return null;
    }

    public synchronized void attemptExecute(GenericComponentInteractionCreateEvent compEvent, long startTime) {
        if (startTime < this.lastEndTime) {
            Log.debug(compEvent.getUser(), this.getClass(), "Clicked too fast!");
            compEvent.deferEdit().queue(null, e -> Log.warn(
                compEvent.getUser(), this.getClass(), "Discord exception thrown", e
            ));
            return;
        }

        this.curStopTime = TimeUtil.getCurMilli() + this.waitTime;
        this.execute(compEvent);
        this.lastEndTime = this.curStopTime;
    }

    public abstract void execute(GenericComponentInteractionCreateEvent compEvent);
    public abstract ActionRow[] getCurComponents();

    public abstract Message updateInteractive(MessageEditData editedMsg);
    public abstract Message updateComponents(ActionRow... rows);
    public abstract void deleteInteractive();

    private void tryStop(long waitTime) {
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return;
        }

        long curTime = TimeUtil.getCurMilli();

        if (this.curStopTime <= curTime || hardStopTime <= curTime) {
            if (!this.isStopped) {
                this.stop(StopType.EXPIRED);
            }
        } else {
            long newWaitTime = Math.min(this.curStopTime - curTime, hardStopTime - curTime);
            this.tryStop(newWaitTime);
        }
    }

    public void updateLastEndTime() {
        this.lastEndTime = TimeUtil.getCurMilli();
    }

    public abstract void stop(StopType type);

    public boolean isStopped() {
        return this.isStopped;
    }

    public Interactive removeInteractive() {
        this.future.cancel(true);
        return interactives.remove(this.interactiveID);
    }
}
