package dev.boarbot.interactives;

import dev.boarbot.util.interactive.StopType;
import dev.boarbot.util.logging.Log;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IDeferrableCallback;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.util.Objects;

public abstract class UserInteractive extends Interactive {
    private final Interaction interaction;
    @Getter protected final String interactionID;
    @Getter protected final User user;
    protected final InteractionHook hook;
    private Message msg;
    private final boolean isMsg;

    protected UserInteractive(Interaction interaction) {
        this(interaction, false);
    }

    protected UserInteractive(Interaction interaction, boolean isMsg) {
        super(
            interaction.getId() + "," + interaction.getUser().getId(),
            Objects.requireNonNull(interaction.getGuild()).getId()
        );

        this.interaction = interaction;
        this.interactionID = interaction.getId();
        this.user = interaction.getUser();
        this.hook = ((IDeferrableCallback) interaction).getHook();
        this.isMsg = isMsg;
    }

    protected UserInteractive(Interaction interaction, boolean isMsg, long waitTime, long hardTime) {
        super(
            interaction.getId() + "," + interaction.getUser().getId(),
            Objects.requireNonNull(interaction.getGuild()).getId(),
            waitTime,
            hardTime
        );

        this.interaction = interaction;
        this.interactionID = interaction.getId();
        this.user = interaction.getUser();
        this.hook = ((IDeferrableCallback) interaction).getHook();
        this.isMsg = isMsg;
    }

    @Override
    public Message updateInteractive(MessageEditData editedMsg) {
        if (this.msg == null && this.isMsg) {
            this.msg = ((ComponentInteraction) this.interaction).getHook().sendMessage(
                MessageCreateData.fromEditData(editedMsg)
            ).complete();
            return this.msg;
        }

        if (this.msg != null) {
            return this.msg.editMessage(editedMsg).complete();
        }

        this.hook.editOriginal(editedMsg).complete();
        return null;
    }

    @Override
    public Message updateComponents(ActionRow... rows) {
        if (this.msg == null && this.isMsg) {
            throw new IllegalStateException("The interactive hasn't been initialized yet!");
        }

        if (this.msg != null) {
            return this.msg.editMessageComponents(rows).complete();
        }

        return this.hook.editOriginalComponents(rows).complete();
    }

    @Override
    public void deleteInteractive() {
        if (this.msg == null && this.isMsg) {
            throw new IllegalStateException("The interactive hasn't been initialized yet!");
        }

        if (this.msg != null) {
            this.msg.delete().complete();
        } else {
            this.hook.deleteOriginal().complete();
        }
    }

    @Override
    public void stop(StopType type) {
        Interactive interactive = this.removeInteractive();
        this.isStopped = true;

        if (interactive == null) {
            return;
        }

        if (type.equals(StopType.EXCEPTION)) {
            if (this.hook != null) {
                this.hook.editOriginalComponents().complete();
            } else {
                this.msg.editMessageComponents().complete();
            }

            return;
        }

        if (this.hook != null) {
            this.hook.editOriginalComponents().complete();
        } else {
            this.msg.editMessageComponents().complete();
        }

        Log.debug(this.user, this.getClass(), "Interactive expired");
    }
}