package jspv.converter;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FileUtils;

public class ImgResize {
    
    private static File tmpFile = null;

    public static void resizeImg(String put, Double scale) {
        //put=put.toLowerCase();
        String append = put.substring(put.length() - 4, put.length());
        if (put.toLowerCase().endsWith(".jpeg")) {
            append = put.substring(put.length() - 5, put.length());
        }
        try {
            tmpFile = File.createTempFile("resize", append);
            Thumbnails.of(put)
                    .scale(scale)
                    .toFile(tmpFile);
            FileUtils.copyFile(tmpFile, new File(put));
            FileUtils.moveFile(new File(put), new File(put + ".resized" + append));
            tmpFile.delete();
        } catch (IOException ex) {
            Logger.getLogger(ImgResize.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
