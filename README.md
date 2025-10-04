# Multi-Agent Systems

## Description

This project is a simulation of multi-agent systems using the JADE (Java Agent DEvelopment Framework). It demonstrates how agents can interact, communicate, and collaborate to achieve their goals.

For this project, we have implemented a simple scenario where multiple agents work at a kitchen restaurant and chaos ensues.

This was based on the game [Overcooked](https://ghosttowngames.com/game/overcooked/).

## Setting up the Environment

### Running the Project (VS Code)

1. Ensure you have Java installed on your machine.
2. Clone this repository to your local machine.
3. Open the project folder in VS Code.
4. Open the `App.java` file located in `src/main/java/underfried/App.java`.
5. Run the `main` method in the `App` class to start the JADE platform with the agents.

### Running the Project (Command Line)

1. Ensure you have Java installed on your machine.
2. Clone this repository to your local machine.
3. Open a terminal and navigate to the project directory.
4. Compile the Java files using the following command:

   ```bash
   javac -cp ./lib/jade.jar -d bin src/main/java/underfried/*.java
   ```

5. Run the JADE platform with the agents using the following command:

   ```bash
   java -cp ./lib/jade.jar:./bin underfried.App
   ```

### JADE (Java Agent DEvelopment Framework) Standalone

Download the JADE framework from [JADE](https://jade.tilab.com/download/jade/).

Unzip either the bin (recommended) or the code version (needs to be compiled).

### Running JADE

On linux terminal, first compile your agent:

```bash
javac -cp ./lib/jade.jar -d bin src/main/java/underfried/TestAgent.java
```

Then, run the JADE platform with the agents using the following command:

```bash
java -classpath ./lib/jade.jar:./bin jade.Boot -gui -agents "testAgent:underfried.TestAgent"
```

On windows:

```text
I don't know. Its windows, probably will have to configure its awfull classpath system variable.
```
