# MTG Card Fetcher Bot for Discord

## Usage
In order to use this bot, you'll need to create a discord app to generate a host token, to do this, simply go here and follow thi instructions:  
https://discordapp.com/developers/applications/  

After doing this, download the repo here, and in the App.java main method, where it says "INSERT TOKEN HERE", add your application token here (you can do this by either creating another class named Ref like I did, or you can load it from a text file, or simply by pasting the token string directly into the JDABuilder). After this, just run the bot and you're ready to go! Below is where to insert the token:

    public static void main(String[] args) throws Exception {
        JDA jda = new JDABuilder(AccountType.BOT).setToken("INSERT TOKEN HERE").build();
        jda.addEventListener(new App());
        initFiles();
    }

---

## Dependencies

This bot makes use of Apache commons for substring extraction, and JSON simple/forohfor scryfall API to interface with scryfall.com in order to load card data. forohfor is not made be me, but is instead located at:  
https://github.com/ForOhForError/ScryfallAPIBinding

However, I use an older version that I have made a few modifications to, that supports my usage a little better.

---

## Use In Discord

This bot uses Scryfall syntax, which can be found here: https://scryfall.com/docs/syntax

To get a list of bot commands, type !help in the chat box and the bot will return a list of all valid commands.  
To serach for a card to get the gatherer information wrap your search inquiry like so: [[\<CARDNAME\> \<additional syntax\>]].  
To serach for a card to get the EDHRec information wrap your search inquiry like so: {{\<CARDNAME\>}}. Additional syntax is not used for this option.  
