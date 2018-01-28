# Autocomplete Sample Demo (Public Preview version 20180128.1.0.1.RC1 **Experimental**)
This sample has a limitation where the cache into the memory is not optimized for search for data of many product names. One way to improve this application will be to implement a faster cache or/and to change the data storage structure to trie tree data structure.

- important to remmber: SQL statement is written with SELECT to return limited numbers of recordes.
- This is sample application is deployed to http://gautocompletefinal.appspot.com/

# Achieved
- Autocomplete core feature, where GET request is sent to the Servlet (AutoCocompleteServlet?id=*) whenever the user type a letter in the input field. AJAX is used to process this task in the client browser application.
- On the Servlet side, product name, which is the target for the autocompletion to perform comparison, is read from the MySQL database when Servlet.init() is called. This way, the product name databased information can stay in the memory as a cache.
Cache design is not yet the best to perform quick search as fast as possible (NEED IMPROVEMENT HERE, and need to do more investigation on 1) Trie Tree Structure algorism and 2)Google App Engine Memcache, to determine how to improve the performance.)
- Cache mecanism (Data structure and cache performance) with Google Storage implemented. Though, timing to create the cache (XML files corresponding to the keyword) is not determinded. At this prototype, we simply implemented the cache sample creation on Servlet.init(). This part need to be consider on the final version.

# Experiments
- No ProductNameInfo cache from DB no longer exist. Only XML cache on Google Storage for this version. Not yet with pre-fetch mechanism. Pre-fetch to be implemented later.

# Areas for improvements
- Global scallability (Deployment to region, but still try to reverage one central database for easy operation of data integrety)
- Search algorism and Better User Assistance (product name search with key is performed with String.startWith(), and there maybe a case where the result returns more than 100 results which is not relavent for users to look up and select the target word from that). Should implement as better way to prompt/advise user of the possible next letter (ex. next letter could be either "a", "b", "d", "x" only, but in total there are more than 100 words)

# Bug fix (FIXED)
- SQL Statement wrong: " ...WHERE list_name ORDER BY.." should be with WHERE removed.
- ProductNameInfo.name should be with String.trim() before registering the value.
- When autocomplete is with "a", it does not give any popup with all product name starting with "a", but when typing "b", it does work as expected. This is cased somehow in relation to the feature to limit entries for popup. This bug has to do with GET request sent without id especified after always hiting "a".

# Bug fix (OPEN)
- Local execution for debugging not working (still) [Low priority]

# Development Version Only
- Data Structure - limiting the data size of ProductNameInfo.name (128 characters to 16)

# Open Items
- Data Structure with Trie Tree Support
- App Engine Memcache implementation (Maybe not necessary)
- Global deployment for better performance on the regions
- Queue to be used to dispatch Autocomplete request (specification to be confirmed first)

# New Features To Be Implemented
- Queue to be used to dispatch XML file creation
