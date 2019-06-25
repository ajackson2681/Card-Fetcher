package DiscordBots.CardFetcher;

import forohfor.scryfall.api.Card;
import forohfor.scryfall.api.MTGCardQuery;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * 
 * @author Adam Jackson
 * @version versionID
 */
public class App extends ListenerAdapter {

  public static final String versionID = "v1.4";

  private static Object keywords;
  private static final Random rng = new Random();

  public static void main(String[] args) throws Exception {
    JDA jda = new JDABuilder(AccountType.BOT).setToken("INSERT TOKEN HERE").build();
    jda.addEventListener(new App());
    initFiles();
  }
  
  private static void initFiles() {
    try {
      keywords = new JSONParser().parse(
          new BufferedReader(new InputStreamReader(
              new FileInputStream("./keywords.json"), "UTF-8")));
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  @Override
  public void onReady(ReadyEvent e) {
    System.out.println("\nCardFetcher "+versionID+" is ready to receive commands!\n");
  }

  @Override
  public void onMessageReceived(MessageReceivedEvent e) {
    MessageChannel channel = e.getChannel();
    Message message = e.getMessage();
    User user = e.getAuthor();

    if (!user.isBot()) {
      String rawMessage = message.getContentRaw();

      if ((rawMessage.contains("[[")) && (rawMessage.contains("]]"))) {
        String[] command = StringUtils.substringsBetween(rawMessage, "[[", "]]");
        processNormalCommand(command, channel, user);
      }
      else if ((rawMessage.contains("{{")) && (rawMessage.contains("}}"))) {
        String[] command = StringUtils.substringsBetween(rawMessage, "{{", "}}");
        processEDHRECCommand(command, channel, user);
      }
      else if (rawMessage.startsWith("!")) {
        String command = StringUtils.substringAfter(rawMessage, "!");
        try {
          processHelpCommand(command, channel, user);
        }
        catch (FileNotFoundException e1) {
          e1.printStackTrace();
        }
      }
    }
  }

  private void processNormalCommand(String[] command, MessageChannel channel, User user) {
    
    for (int i = 0; i < command.length; i++) {
      
      ArrayList<Card> cards = MTGCardQuery.search(command[i]);
      
      if ((!cards.isEmpty()) && (cards != null)) {
        Card card = cards.get(0);

        EmbedBuilder eb = new EmbedBuilder();

        try {
          String url = card.getCannonicalImageURI();
          eb.setImage(url);
        }
        catch (Exception ex) {
          String cardText = card.getName() + " - " + card.getManaCost() + "\n" + card.getTypeLine()
              + "\n" + card.getOracleText() + "\n" + card.getPower() + "/" + card.getToughness();
          eb.setDescription("Unable to find card image, but here is the card text:\n" + cardText);
        }
        
        String cardName = card.getName();
        
        try {
          int multiverseID = card.getMultiverseID().intValue();

          String gathererURL =
              "http://gatherer.wizards.com/Pages/Card/Details.aspx?multiverseid=" + multiverseID;

          eb.setTitle(cardName + " - Gatherer page", gathererURL);
        }
        catch (Exception ex) {
          eb.setTitle("Unable to retrieve gatherer infromation at this time.");
        }
        channel.sendMessage(eb.build()).queue();
      }
      else {
        channel.sendMessage(user.getAsMention() + " I was unable to find that card as searched."
            + " Please check your spelling and try again. If you're sure the card exists, the"
            + " problem will be fixed as soon as possible.").queue();
      }
    }
  }
  
  private void processEDHRECCommand(String[] command, MessageChannel channel, User user) {
    for (int i = 0; i < command.length; i++) {
      
      ArrayList<Card> cards = MTGCardQuery.search(command[i]);
      
      if ((!cards.isEmpty()) && (cards != null)) {
        Card card = cards.get(0);

        EmbedBuilder eb = new EmbedBuilder();
        try {
          String url = card.getCannonicalImageURI();
          eb.setImage(url);
        }
        catch (Exception ex) {
          String cardText = card.getName() + " - " + card.getManaCost() + "\n" + card.getTypeLine()
              + "\n" + card.getOracleText() + "\n" + card.getPower() + "/" + card.getToughness();
          eb.setDescription("Unable to find card image, but here is the card text:\n" + cardText);
        }
        
        String cardName = card.getName();
        cardName = cardName.replaceAll("'", "");
        cardName = cardName.replaceAll(",", "");
        cardName = cardName.replaceAll(" ", "-");

        String typeLine = card.getTypeLine();
        String oracleText = card.getOracleText();
        
        if (typeLine.contains("Legendary Creature")) {
          eb.setTitle(card.getName() + " - EDHREC commander page",
              "http://www.edhrec.com/commanders/" + cardName);
        }
        else if ((typeLine.contains("Legendary Planeswalker")) && (oracleText != null)
            && (oracleText.contains("can be your commander"))) {
          eb.setTitle(card.getName() + " - EDHREC commander page",
              "http://www.edhrec.com/commanders/" + cardName);
        }
        else {
          eb.setTitle(card.getName() + " - EDHREC card page",
              "https://www.edhrec.com/cards/" + cardName);
        }
        channel.sendMessage(eb.build()).queue();
      }
      else {
        channel.sendMessage(user.getAsMention() + " I was unable to find that card as searched."
            + " Please check your spelling and try again. If you're sure the card exists, the"
            + " problem will be fixed as soon as possible.").queue();
      }
    }
  }
  
  private void processHelpCommand(String command, MessageChannel channel, User user)
      throws FileNotFoundException {
    
    String[] commandSplit = command.toLowerCase().split(" ");
    
    String str = commandSplit[0].toLowerCase();

    switch (str) {
      case "kw":
        searchRules(command, channel, user);
        break;
      case "help":
        printHelp(channel, user);
        break;
      case "roll":
        try {
          int max = Integer.parseInt(commandSplit[1]);
          roll(channel, user, max);
        }
        catch (Exception e) {
          channel.sendMessage(
              user.getAsMention() + " you tried to roll using a non-integer or non-number input/")
              .queue();
        }
        break;
      case "version":
        channel.sendMessage("I am currently running " + versionID + ".").queue();
        break;
      default:
        channel.sendMessage("Invalid command, please try again").queue();
    }

  }
  
  private void printHelp(MessageChannel channel, User user) {
    
    channel.sendMessage(user.getAsMention() + " These are the following commands I can perform:\n\n"
        + "[[cardname]] returns card information from gatherer, and also puts the card image "
        + "in the chat.\n\n" +

        "{{cardname}} returns card information from EDHREC, and also puts the card image in"
        + " the chat.\n\n" +

        "If you desire a specific set image, insert e:SET inside the brackets and after the"
        + " card name, using the 3 letter set code instead of the word SET.\n\n" +

        "!kw KEYWORD will return the keyword definition from the Comprehensive MTG Rulebook.\n\n" +

        "!version will tell you what version the bot is currently running.\n\n" +

        "!roll <number> Rolls a random number from 1 to your chosen number.").queue();
  }
  
  private void searchRules(String command, MessageChannel channel, User user) {
    String rule = JSONTranslate.getRule(command, (JSONObject)keywords);
    
    if(rule != null) {
      channel.sendMessage(rule).queue();
    }
    else {
      channel.sendMessage("That rule could not be found. It either does not exist, or the"
        + " admin has not yet added it to the database.").queue();
    }
  }  
  
  private void roll(MessageChannel channel, User user, int num) {
    channel.sendMessage(user.getAsMention() + " You rolled: " + (1 + rng.nextInt(num))).queue();
  }
  
}
