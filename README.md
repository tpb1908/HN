# HN

## Features
Theming, light or dark and at time
Options for card style
Volume navigation
Ad blocking in browser
Lazy content loading
Mercury web parser loading
Amp page loading
Spritz style skim reading


## TODO

- [ ]Picking of default browser mode and fragment
- [ ]Media service for TTS
- [ ]Snappy scrolling
- [ ]Remove item from adapter when swiped in saved section
- [ ]Blacklist of URLs (And hosts) for parsed sites. User editable
- [ ]Font sizes
- [ ]Make sure settings subsections are correct
- [ ]Add a setting for scrolling to a comment when opened from UserViewActivity
- [ ]Only expand a single comment tree when the above setting is on
- [ ]Allow clearing cache

## Refactoring plan

- [x] Fragment structure 
- [x] Three booleans, for views, data, and context
- [x] One application level 'loader' 
- [x] which switches based on network
- [x] Loader has sets of Ids
- [ ] Enums for sections
- [x] Rewrite database and listeners