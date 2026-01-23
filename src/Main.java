
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import javax.imageio.ImageIO;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        System.out.println("Starting program.");
        Properties props = ImageRW.loadProperties("config.properties");

        int[] blacklist = Precompute.precomputeBlacklist();
        assert blacklist.length == 123 : "you messed with the precomputeBlacklist function";

        File file = new File(props.getProperty("input"));
        BufferedImage image = ImageIO.read(file);
        
        int height = image.getHeight();
        int width = image.getWidth();

        boolean[] mask = ImageRW.LoadImageToMask(image, height, width);
        assert mask.length == height*width : "image was not loaded in correctly";

        List<Integer> outline = Outline.cleanupMask(mask, height, width, blacklist);
        List<Integer> spurs = Spurs.createSpurs(outline, height, width, Integer.parseInt(props.getProperty("spurcount")));

        int[] heightmap = Temp.createHeightmap(outline, spurs, height, width, props);

        int[] noise = Noise.generatePerlin(Integer.parseInt(props.getProperty("noiseRandomness")), height, width);

        heightmap = Noise.applyNoise(heightmap, noise, Double.parseDouble(props.getProperty("noiseStrength")));

        File outFile = new File(props.getProperty("outputPNG"));
        ImageRW.writeIntMaskToFile(heightmap, outFile, height, width);

        System.out.println("End of file");
    }
}
