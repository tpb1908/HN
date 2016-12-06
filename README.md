# HN

## Features

Theming, light or dark and at time

Options for card style in main view and comments

Volume key navigation in Content (Add this for comments)?

Ad blocking in browser

Lazy content loading

Mercury web parser loading

Amp page loading

Spritz style skim reading with skipping features not featured in Spritz


## TODO
- [ ] Search view
- [ ] Filter by user in search
- [ ] Picking of default browser mode and fragment
- [ ] Media service for TTS
- [ ] Snappy scrolling
- [ ] Remove item from adapter when swiped in saved section
- [ ] Blacklist of URLs (And hosts) for parsed sites. User editable
- [ ] Font sizes
- [ ] Make sure settings subsections are correct
- [ ] Add a setting for scrolling to a comment when opened from UserViewActivity
- [ ] Only expand a single comment tree when the above setting is on
- [ ] Allow clearing cache
- [x] Preload greater number of items and don't both cancelling background task
- [ ] Free floating FAB for scrolling
- [x] Option for bottom toolbar

## Refactoring plan

- [x] Fragment structure 
- [x] Three booleans, for views, data, and context
- [x] One application level 'loader' 
- [x] which switches based on network
- [x] Loader has sets of Ids
- [x] Enums for sections
- [x] Rewrite database and listeners


### License 

    Copyright 2016 Theo Pearson-Bray
        
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
       
    http://www.apache.org/licenses/LICENSE-2.0
        
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.