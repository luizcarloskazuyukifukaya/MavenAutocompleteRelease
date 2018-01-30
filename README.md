# Autocomplete Sample Demo (Public Preview version 20180130t1034.RC5 ** MAX EXTENDED Release  **)
This sample has a limitation where the cache into the memory is not optimized for search for data of many product names. One way to improve this application will be to implement a faster cache or/and to change the data storage structure to trie tree data structure.

- important to remember: SQL statement is written with SELECT to return limited numbers of recordes.
- This is sample application is deployed to [Final Demo URL](http://gautocompletefinal.appspot.com/)
- Google Storage Bucket name is "autocomplete_xml_big_cache" in this source code.

# Achieved
- Autocomplete core feature, where GET request is sent to the Servlet (AutoCocompleteServlet?id=*) whenever the user type a letter in the input field. AJAX is used to process this task in the client browser application.
- On the Servlet side, product name, which is the target for the autocompletion to perform comparison, is read from the MySQL database when Servlet.init() is called. This way, the product name databased information can stay in the memory as a cache.
Cache design is not yet the best to perform quick search as fast as possible (NEED IMPROVEMENT HERE, and need to do more investigation on 1) Trie Tree Structure algorism and 2)Google App Engine Memcache, to determine how to improve the performance.)
- Cache mecanism (Data structure and cache performance) with Google Storage implemented. Though, timing to create the cache (XML files corresponding to the keyword) is not determinded. At this prototype, we simply implemented the cache sample creation on Servlet.init(). This part need to be consider on the final version.

# Experiments
- No ProductNameInfo cache from DB no longer exist. Only XML cache on Google Storage for this version. Not yet with pre-fetch mechanism. Pre-fetch to be implemented later.
- POPUP NUM now 100
- Product name length is know  (keyword length + MAX_NAME_LENGTH) : Every time user types a keyword, the product name shown is extended.

# Areas for improvements
- Global scallability (Deployment to region, but still try to reverage one central database for easy operation of data integrety)
- Search algorism and Better User Assistance (product name search with key is performed with String.startWith(), and there maybe a case where the result returns more than 100 results which is not relavent for users to look up and select the target word from that). Should implement as better way to prompt/advise user of the possible next letter (ex. next letter could be either "a", "b", "d", "x" only, but in total there are more than 100 words)

# Bug fix (FIXED)
- Fixed the case where the XML data is not found in the Storage Cache and saved on the cache. There was a bug where popup did not update based on new product name found. 
- SQL Statement wrong: " ...WHERE list_name ORDER BY.." should be with WHERE removed.
- ProductNameInfo.name should be with String.trim() before registering the value.
- When autocomplete is with "a", it does not give any popup with all product name starting with "a", but when typing "b", it does work as expected. This is cased somehow in relation to the feature to limit entries for popup. This bug has to do with GET request sent without id especified after always hiting "a".
- When product name includes "'", it fails to retrieve the information from MySQL. This is because the SQL statement should be escaped for this character.

# Bug fix (OPEN)
- Local execution for debugging not working (still) [Low priority]
- When product name includs "?", it fails. Either on MySQL retrieval or cache creation, I suppose. (Need to investigate, but for the purpose of the prototype we should not care much now.)

# Development Version Only
- Data Structure - limiting the data size of ProductNameInfo.name (128 characters to 16)

# Open Items
- Data Structure with Trie Tree Support
- App Engine Memcache implementation for XML file index and for the Trie Tree Data to be shared among batch app processes.
- Global deployment for better performance on the regions
- Queue to be used to dispatch Autocomplete request (specification to be confirmed first)

# New Features To Be Implemented
- Queue to be used to dispatch XML file creation
