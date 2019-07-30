package DiscordBots.CardFetcher;

import java.util.Set;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.json.simple.JSONObject;

public class JSONTranslate {
  
  private static String convertedKey(String input) {
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

  @SuppressWarnings("unchecked")
  public static String getRule(String key, JSONObject obj) {
    String convertedKey = convertedKey(key);
    
    if((String)obj.get(convertedKey) != null) {
      return (String) obj.get(convertedKey);
    }
    else {
      Set<String> keys = obj.keySet();
      for(String s: keys) {
        if(new JaroWinklerDistance().apply(convertedKey, s) > .8) {
          return (String) obj.get(s);
        }
      }
    }
    
    return null;
  }
}
