package com.game;

public class Main {
    static void main() {
        //FontUtils.openFont(Path.of("src/main/resources/fonts/JetBrainsMono-Regular.ttf"));
        //FontUtils.openFont(Path.of("src/main/resources/fonts/NotoSansJP-Regular.ttf"));

        Game game = new Game();
        game.run();
    }
}