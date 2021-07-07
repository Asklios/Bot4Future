package main.java.util;

import net.dv8tion.jda.api.entities.Message;

import java.util.List;

public class Emojis {
    public static final String BACK = "◀";
    public static final String REDO = "\uD83D\uDD01";
    public static final String READY = "☑";
    public static final String LOCK = "\uD83D\uDD12";
    public static final String CLOSE = "❌";
    public static final String VIEW = "\uD83D\uDC40";

    public static final String INFO = "\uD83D\uDCA1";

    public static final String CHART_EMPTY = "─";
    public static final String CHART_FULL = "█";

    public static final String COOKIE = "\uD83C\uDF6A";

    public static final List<String> EMOJI_LETTERS = List.of("\uD83C\uDDE6 \uD83C\uDDE7 \uD83C\uDDE8 \uD83C\uDDE9 \uD83C\uDDEA \uD83C\uDDEB \uD83C\uDDEC \uD83C\uDDED \uD83C\uDDEE \uD83C\uDDEF \uD83C\uDDF0 \uD83C\uDDF1 \uD83C\uDDF2 \uD83C\uDDF3 \uD83C\uDDF4 \uD83C\uDDF5 \uD83C\uDDF6 \uD83C\uDDF7 \uD83C\uDDF8 \uD83C\uDDF9 \uD83C\uDDFA \uD83C\uDDFB \uD83C\uDDFC \uD83C\uDDFD \uD83C\uDDFE \uD83C\uDDFF".split(" "));

    public static class ReactionAdder {
        private final List<String> reactions;

        private int pointer = 0;

        public ReactionAdder(List<String> emojis) {
            reactions = emojis;
        }

        public void addReactions(Message msg, Runnable callback) {
            addReaction(this, msg, callback);
        }

        private void addReaction(ReactionAdder adder, Message msg, Runnable callback) {
            if (adder.reactions.size() > adder.pointer) {
                msg.addReaction(adder.reactions.get(adder.pointer)).queue(nothing -> {
                    adder.pointer++;
                    addReaction(adder, msg, callback);
                });
            } else {
                callback.run();
            }
        }
    }
}
