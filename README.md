# Command-Line-Interpreter
# COEN 346 - Concordia University
Java command Line Inpreter

A command-line-interpreter built in Java using multiple threads to background/foreground threads.

Use of threads, lambda expressions and general good coding practices to keep the code succint and efficient as possible.

Commands have 3 levels: internal, inlcuded, and external.

Internal commands are such: echo, exit, -> (write to file), ->> (append to file).
Included commands are any executable/bat found with in the path location specificed within input.txt
External commands are commands found using an absolute path.

Included/External commands are executed on a new thread whether in the background or not.

Internal commands are executed on the Terminal thread.

