package com.game;

import com.game.renderer.texture.Tile;
import com.game.util.IOUtils;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    static void main() {
        Path mapsPath = Path.of("src/main/resources/maps");
        if (!Files.isDirectory(mapsPath))
            IOUtils.createDirectory(mapsPath);

        try (FileWriter fw = new FileWriter(mapsPath.resolve("simple_map.txt").toFile())){
            for (int i = -7; i <= 7; i++) {
                fw.write(i + " -8 1 1 1 " + Tile.ROCK + " TRUE\n");
                fw.write(i + " 8 1 1 1 " + Tile.ROCK + " TRUE\n");
                fw.write("-8 " + i + " 1 1 1 " + Tile.ROCK + " TRUE\n");
                fw.write("8 " + i + " 1 1 1 " + Tile.ROCK + " TRUE\n");
            }
            fw.write("-8 -8 1 1 1 " + Tile.ROCK + " TRUE\n");
            fw.write("8 -8 1 1 1 " + Tile.ROCK + " TRUE\n");
            fw.write("8 8 1 1 1 " + Tile.ROCK + " TRUE\n");
            fw.write("-8 8 1 1 1 " + Tile.ROCK + " TRUE\n");

            for (int i = -8; i <= 8; i++)
                for (int j = -8; j <= 8; j++)
                    if (i < -1 || i > 1 || j < -1 || j > 1)
                        fw.write(i + " " + j + " 1 1 0 "  + Tile.GRASS + " FALSE\n");

            for (int i = -1; i <= 1; i++)
                for (int j = -1; j <= 1; j++)
                    fw.write(i + " " + j + " 1 1 0 "  + Tile.WATER + " FALSE\n");

            for (int i = -1; i <= 1; i++) {
                fw.write(i + " -2 1 1 1 " + Tile.LAKE_BOTTOM + " FALSE\n");
                fw.write(i + " 2 1 1 1 " + Tile.LAKE_TOP + " FALSE\n");
                fw.write("-2 " + i + " 1 1 1 " + Tile.LAKE_LEFT + " FALSE\n");
                fw.write("2 " + i + " 1 1 1 " + Tile.LAKE_RIGHT + " FALSE\n");
            }

            fw.write("-2 -2 1 1 1 " + Tile.LAKE_BOTTOM_LEFT + " FALSE\n");
            fw.write("2 -2 1 1 1 " + Tile.LAKE_BOTTOM_RIGHT + " FALSE\n");
            fw.write("2 2 1 1 1 " + Tile.LAKE_TOP_RIGHT + " FALSE\n");
            fw.write("-2 2 1 1 1 " + Tile.LAKE_TOP_LEFT + " FALSE\n");
        } catch (Exception e) {
            throw  new RuntimeException(e);
        }

       Game game = new Game();
       game.run();
    }
}