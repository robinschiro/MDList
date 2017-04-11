Running the Performance Tests in Eclipse:

.) Unzip the zip file of the project to a location on your computer.
.) Open Eclipse (preferably Eclipse Neon)
.) Import the project 'MDList' into Eclipse.
.) In the 'Package Explorer' tab, expand the 'MDList' project folder. Expand the 'test' folder
.) Right click on the 'DataStructurePerformanceTests' package and select 'Run As' -> 'JUnit Test'. The performance tests should begin running.

Explanation:

Once you start the unit tests, both the MDList and an alternative dictionary (SkipList) will undergo performance tests with various sets of parameters. The parameters that are different in each test are the following:

- Size of key space
- Proportion of insert instructions
- Proportion of delete instructions
- Proportion of find instructions

The name of the class file for each performance test class contains information about the values of the parameters. For example, the following performance test class uses a key space of size 2^10 with 9% Insert instructions, 1% Delete instructions and 90% Find instructions.

    PerformanceTests_10_09I01D90F
    
Each test class runs a test with these parameters for 1, 2, 4 and 8 threads on both the MDList and SkipList data structures. You can see the time taken for each test in the 'JUnit' output window.
