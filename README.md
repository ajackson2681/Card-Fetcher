# CardFetcher

## Usage
In order to use this bot, you'll need to create a discord app to generate a host token, to do this, simply go here and follow thi instructions:  
https://discordapp.com/developers/applications/  

After doing this, download the repo here, and in the App.java class, where it says REF.TOKEN_TEST, add your application token here (you can do this by either creating another class named Ref like I did, or you can load it from a text file, or simply by pasting the token string directly into the JDABuilder). After this, just run the bot and you're ready to go!

---

## Dependencies

This bot makes use of Apache commons for substring extraction, and JSON simple/forohfor scryfall API to interface with scryfall.com in order to load card data. forohfor is not made be me, but is instead located at:  
https://github.com/ForOhForError/ScryfallAPIBinding

However, I use an older version that I have made a few modifications to, that supports my usage a little better.
