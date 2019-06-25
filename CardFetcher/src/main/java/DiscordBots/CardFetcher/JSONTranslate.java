package DiscordBots.CardFetcher;

import org.json.simple.JSONObject;

public class JSONTranslate {
  private static String convertKey(String input) {
    String[] inputSplit = input.split(" ");
    String keywordKey = "";
    for (int i = 1; i < inputSplit.length; i++) {
      keywordKey = keywordKey + inputSplit[i].substring(0, 1).toUpperCase()
          + inputSplit[i].substring(1, inputSplit[i].length());
      if (i != inputSplit.length - 1) {
        keywordKey = keywordKey + " ";
      }
    }
    return keywordKey;
  }

  public static String getRule(String key, JSONObject obj) {
    return (String) obj.get(convertKey(key));
  }
}
