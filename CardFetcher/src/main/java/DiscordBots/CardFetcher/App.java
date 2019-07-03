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

  private static Object keywords; // holds the json object for the keywords
  private static final Random rng = new Random(); // used in the roll() method

  public static void main(String[] args) throws Exception {
    JDA jda = new JDABuilder(AccountType.BOT).setToken("INSERT TOKEN HERE").build();
    jda.addEventListener(new App());
    initFiles();
  }
  
  /**
   * Initializes the kewords JSON file
   */
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
  
  /**
   * Performs actions when the bot comes online
   */
  @Override
  public void onReady(ReadyEvent e) {
    System.out.println("\nCardFetcher "+versionID+" is ready to receive commands!\n");
  }

  /**
   * Performs actions whenever a message is received in a text channel
   */
  @Override
  public void onMessageReceived(MessageReceivedEvent e) {
    MessageChannel channel = e.getChannel(); // channel the message is received in
    Message message = e.getMessage(); // content of the message
    User user = e.getAuthor(); // author of the message

    if (!user.isBot()) { // Only listens to non-bot messages
      String rawMessage = message.getContentRaw(); // gets the raw string from the message

      // if the message is formatted as [[such]], performs a search on Gatherer
      if ((rawMessage.contains("[[")) && (rawMessage.contains("]]"))) {
        String[] command = StringUtils.substringsBetween(rawMessage, "[[", "]]");
        processNormalCommand(command, channel, user);
      }
      // if the message is formatted as {{such}}, performs a search on EDHREC
      else if ((rawMessage.contains("{{")) && (rawMessage.contains("}}"))) {
        String[] command = StringUtils.substringsBetween(rawMessage, "{{", "}}");
        processEDHRECCommand(command, channel, user);
      }
      // if the message starts with an !, processes other commands such as help or keywords
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

  /**
   * handles the actions related to Gatherer searches
   * @param command is the card(s) to be searched for
   * @param channel is the channel the request was sent from
   * @param user is the user who requested the information
   */
  private void processNormalCommand(String[] command, MessageChannel channel, User user) {
    
    // loops equal to the number of cards searched for
    for (int i = 0; i < command.length; i++) {
      
      // searches scryfall for a card of the query
      ArrayList<Card> cards = MTGCardQuery.search(command[i]);
      
      // builds a message if the card search was valid
      if ((!cards.isEmpty()) && (cards != null)) {
        Card card = cards.get(0); // grabs the first one from the list (likely the most correct one)

        EmbedBuilder eb = new EmbedBuilder(); // creates a message builder to progressively add 
                    // information as it's found

        // tries to get the card image, otherwise substitutes with card text
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
        
        // attempts to get the multiverse ID and search on Gatherer using it, otherwise says
        // gatherer information is not currently available
        try {
          int multiverseID = card.getMultiverseID().intValue();

          String gathererURL =
              "http://gatherer.wizards.com/Pages/Card/Details.aspx?multiverseid=" + multiverseID;

          eb.setTitle(cardName + " - Gatherer page", gathererURL);
        }
        catch (Exception ex) {
          eb.setTitle("Unable to retrieve gatherer infromation at this time.");
        }
        
        // sends the message
        channel.sendMessage(eb.build()).queue();
      }
      else {
        // if the search is invalid, sends this message
        channel.sendMessage(user.getAsMention() + " I was unable to find that card as searched."
            + " Please check your spelling and try again. If you're sure the card exists, the"
            + " problem will be fixed as soon as possible.").queue();
      }
    }
  }
  
  /**
   * handles the actions related to EDHREC searches
   * @param command is the card(s) to be searched for
   * @param channel is the channel the request was sent from
   * @param user is the user who requested the information
   */
  
  private void processEDHRECCommand(String[] command, MessageChannel channel, User user) {
    
    // loops equal to the number of cards being searched for
    for (int i = 0; i < command.length; i++) {
      
      // searches scryfall for the cards specified
      ArrayList<Card> cards = MTGCardQuery.search(command[i]);
      
      // builds a message if the search is valid
      if ((!cards.isEmpty()) && (cards != null)) {
       
        Card card = cards.get(0); // grabs the first card from the list

        EmbedBuilder eb = new EmbedBuilder(); // builds a message as info is retrieved
        
        // attempts to get the card image, if not, replaces it with text
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
        
        // formats the card name to be part of a valid EDHREC URL
        cardName = cardName.replaceAll("'", "");
        cardName = cardName.replaceAll(",", "");
        cardName = cardName.replaceAll(" ", "-");

        // retrieves information to see if it's a valid commander
        String typeLine = card.getTypeLine();
        String oracleText = card.getOracleText();
        
        // if the card is a valid commander by being a legendary creature, or if
        // the rules text contains "can be your commander", prints the EDHREC
        // commander page, otherwise uses the card page
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
        
        // sends the message
        channel.sendMessage(eb.build()).queue();
      }
      else {
        // if the search is invalid, sends this message
        channel.sendMessage(user.getAsMention() + " I was unable to find that card as searched."
            + " Please check your spelling and try again. If you're sure the card exists, the"
            + " problem will be fixed as soon as possible.").queue();
      }
    }
  }
  
  
  /**
   * Performs actions when the user command begins with !
   * @param command is the command to be processed
   * @param channel is the channel the command was sent from
   * @param user is who requested the command
   * @throws FileNotFoundException if you do not have keywords.json loaded in your bot's directory
   */
  private void processHelpCommand(String command, MessageChannel channel, User user)
      throws FileNotFoundException {
    
    // splits the command on spaces
    String[] commandSplit = command.toLowerCase().split(" ");
    
    // the first part of the command is the key
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
  
  
  /**
   * Prints information regarding the commands to the discord channel
   * @param channel is the channel the information was requested from
   * @param user is the user who requested the information
   */
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
  
  /**
   * Searches the keywords.json file for the specified keyword
   * @param command is the keyword to be found
   * @param channel is the channel the command came from
   * @param user is the user who requested the information
   */
  
  private void searchRules(String command, MessageChannel channel, User user) {
    // uses the JSON utilities to convert the command to a valid key and searches the keywords
    String rule = JSONTranslate.getRule(command, (JSONObject) keywords);

    // if the rule is not valid, splits it into 2000 character chunks (this is the max the API can
    // support
    if (rule != null) {
      if(rule.length() > 2000) { // splits only if the length is greater than 2000
        
        // creates an array of strings by splitting it based on the integer division of the length
        // of the rules divided by 2000 + 1
        String[] ruleParts = new String[rule.length() / 2000 + 1];
        
        int min = 0; // represents the current spot into in the rule string
        
        // stores the divided rules text into the respective indices in the array
        for(int i = 0; i < ruleParts.length; i++) {
          if(rule.length() >= min + 2000) { // 
            
            // creates a substring of 2000 characters starting at the current
            // index of min
            ruleParts[i] = rule.substring(min, min+2000);
            
            min += 2000; // adds 2000 to the current index
            
            channel.sendMessage(ruleParts[i]).queue();
          }
          else { // if the difference between the end of the rules text and min is < 2000, simply
            // prints the remainder of the rule
            ruleParts[i] = rule.substring(min, rule.length());

            channel.sendMessage(ruleParts[i]).queue();
          }
        }
      }
      else {
        channel.sendMessage(rule).queue(); // prints the entire rule if it's less than 2000 characters
      }
    }
    else {
      // if the rule was typed incorrectly or does not exist, prints this message
      channel.sendMessage("That rule could not be found. It either does not exist, or the"
          + " admin has not yet added it to the database.").queue();
    }
  }  
  
  
  /**
   * Rolls a number between 1 and the specified number
   * @param channel is the channel the request came from
   * @param user is the user who rolled
   * @param num is the max number to roll
   */
  private void roll(MessageChannel channel, User user, int num) {
    channel.sendMessage(user.getAsMention() + " You rolled: " + (1 + rng.nextInt(num))).queue();
  }
  
}
