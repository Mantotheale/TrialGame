try (FileWriter fw = new FileWriter("src/main/resources/maps/simple_map.txt")){
    for (int i = -7; i <= 7; i++)
        for (int j = -7; j <= 7; j++)
            if (i < -1 || i > 1 || j < -1 || j > 1)
                fw.write(i + " " + j + " 1 1 0 "  + GRASS + "\n");

    for (int i = -1; i <= 1; i++)
        for (int j = -1; j <= 1; j++)
            fw.write(i + " " + j + " 1 1 0 "  + WATER + "\n");

    for (int i = -1; i <= 1; i++) {
        fw.write(i + " -2 1 1 1 " + LAKE_BOTTOM + "\n");
        fw.write(i + " 2 1 1 1 " + LAKE_TOP + "\n");
        fw.write("-2 " + i + " 1 1 1 " + LAKE_LEFT + "\n");
        fw.write("2 " + i + " 1 1 1 " + LAKE_RIGHT + "\n");
    }

    fw.write("-2 -2 1 1 1 " + LAKE_BOTTOM_LEFT + "\n");
    fw.write("2 -2 1 1 1 " + LAKE_BOTTOM_RIGHT + "\n");
    fw.write("2 2 1 1 1 " + LAKE_TOP_RIGHT + "\n");
    fw.write("-2 2 1 1 1 " + LAKE_TOP_LEFT + "\n");
} catch (Exception e) {
    throw  new RuntimeException(e);
}