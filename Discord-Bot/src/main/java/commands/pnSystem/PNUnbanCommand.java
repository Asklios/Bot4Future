package main.java.commands.pnSystem;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class PNUnbanCommand extends ListenerAdapter implements Runnable  {
	
	long startTime;
	int timeoutTime = 5000*60;
	PrivateChannel channel;
	int phase = 1;
	
	public PNUnbanCommand(PrivateChannel c) {
		this.channel = c;
	}
	
	@Override
	public void run() {
		
		startTime = System.currentTimeMillis(); 
		channel.sendMessage("Für welchen Server möchtest du einen Entbannungsantrag stellen? ```<ServerID>```").complete();
		
		while (true) {
			try {
				this.wait(timeoutTime);
			}
			catch (InterruptedException e) {}
		}
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		
		
		if (event.isFromType(ChannelType.PRIVATE)) {
			if (event.getPrivateChannel().getIdLong() == channel.getIdLong()) {
				if (phase == 1) {
					phaseOne(event);
				}
				else {
					phaseTwo(event);
				}
				
			}
		}
	}
	
	private void phaseOne(MessageReceivedEvent event) {
		
		String message = event.getMessage().getContentDisplay();
		System.out.println(message + " - Überprüfen ob dies eine gültige ID ist, dann in PhaseTwo übergehen");
		channel.sendMessage("Warum möchtest du entbannt werden?").complete();
		phase ++;
	}
	
	private void phaseTwo(MessageReceivedEvent event) {
		channel.sendMessage("Der Antrag wurde weitergeleitet.").complete();
		Thread.currentThread().interrupt();
	}
	
}
