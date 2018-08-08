
package jspv.converter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageConverter {

    //public static final String ERROR_STRING = "ImageConverter has encountered an error: ";

    public static void imgConvert(String inpath, String outpath, ImageFormats imf) {
        File outFile=new File(outpath);
        File inFile =new File(inpath);
        try {
            BufferedImage image = ImageIO.read(inFile);
            ImageIO.write(image, imf.getCaption(), outFile);
            /*System.out.println(imf.getCaption());
            for (String str: ImageIO.getReaderFormatNames())
                System.out.println("reader types = " + str);
            for (String str: ImageIO.getWriterFormatNames())
                System.out.println("writer types = " + str); */         
            //return outFile;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            //return null;
        }
    }
}