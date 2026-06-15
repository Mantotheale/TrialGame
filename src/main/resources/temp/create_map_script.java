import com.game.renderer.texture.Tile;
import java.io.FileWriter;

try (FileWriter fw = new FileWriter("src/main/resources/maps/simple_map.txt")){
    for (int i = -6; i <= 6; i++) {
        fw.write(i + " -7 1 1 1 " + Tile.ROCK + " TRUE\n");
        fw.write(i + " 7 1 1 1 " + Tile.ROCK + " TRUE\n");
        fw.write("-7 " + i + " 1 1 1 " + Tile.ROCK + " TRUE\n");
        fw.write("7 " + i + " 1 1 1 " + Tile.ROCK + " TRUE\n");
    }
    fw.write("-7 -7 1 1 1 " + Tile.ROCK + " TRUE\n");
    fw.write("7 -7 1 1 1 " + Tile.ROCK + " TRUE\n");
    fw.write("7 7 1 1 1 " + Tile.ROCK + " TRUE\n");
    fw.write("-7 7 1 1 1 " + Tile.ROCK + " TRUE\n");

    for (int i = -7; i <= 7; i++)
        for (int j = -7; j <= 7; j++)
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