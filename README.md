# Earthquake-Footprint
Source code and other files for the earthquake prediction software.

Create a project and add the source files to it. Then add a reference to the 'licas_ai.jar' file in 'licasTP'.
You can check for updates for this file at https://sourceforge.net/projects/licas/, but you probably don't need to.
The jar file provides the FrequencyGrid and the logger, for example.

There are two data file in the 'data' folder that are read when you run the program.
There are two test classes - TestEQ_Greece and TestEQ_USA, which read one of the repective data files. 

You only need to run the 'ReadData.formatData' statement once, then you can comment it out.
You should use the data files provided, but they are the same as the sources and you can easily create new ones, just the header information.
Some of the data rows are not complete, so if there is an error message indicating this when you run the code, just ignore it.
