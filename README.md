# Lifesteal

## Download

1. Login to github, or register a new account
2. Click on the latest build [here](https://github.com/CloudCraftProjects/Lifesteal/actions/workflows/build.yml?query=branch%3Amaster)
3. Download the artifact at the bottom of the page
4. Unzip the artifact and put the jar file in your plugins folder

To use commands, install [CommandAPI](https://commandapi.jorel.dev/).

## How to build it yourself


1. Clone the repository using git
2. Open a terminal/cmd window in the cloned directory

If on linux or mac, use `./gradlew build` to build.
If you're on windows, use `gradlew build` to build.

After running the command, the jar file should be in `build/libs`.
Choose the one without `-sources` at the end of the name.
