package main.java.helper;

import net.dv8tion.jda.api.JDA;

@FunctionalInterface
public interface TaskContext {
    void run(JDA api);
}
