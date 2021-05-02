package jspv.converter;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FileUtils;

public class ImageRotate {
    
    private static File tmpFile = null;

    public static void thumbnailsRotate(String put, int tip) {
        //put = put.toLowerCase();
        String append = put.substring(put.length() - 4, put.length());
        if (put.toLowerCase().endsWith(".jpeg")) {
            append = put.substring(put.length() - 5, put.length());
        }        
        try {
            tmpFile = File.createTempFile("rotate", append);
            Thumbnails.of(put)
                    .rotate(tip)
                    .scale(1.0)
                    .toFile(tmpFile);
            FileUtils.copyFile(tmpFile, new File(put + ".rotated_" + tip + append));
            //FileUtils.moveFile(new File(put), new File(put + ".rotated" + append));
            tmpFile.delete();            
        } catch (IOException ex) {
            Logger.getLogger(ImageRotate.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

   /* public static void rotateImage(String srcput, String dstput, int tip) {
        if (Math.abs(tip) != 90) {
            return;
        }
        //srcput = srcput.toLowerCase();
        //dstput = dstput.toLowerCase();
        try {
            BufferedImage source = ImageIO.read(new File(srcput));
            BufferedImage output = new BufferedImage(source.getHeight(), source.getWidth(), source.getType());
            AffineTransformOp atop = null;
            switch (tip) {
                case +90:
                    atop = new AffineTransformOp(rotate90plus(source), AffineTransformOp.TYPE_BICUBIC);
                    break;
                case -90:
                    atop = new AffineTransformOp(rotate90minus(source), AffineTransformOp.TYPE_BICUBIC);
                    break;
            }
            atop.filter(source, output);
            if (srcput.toLowerCase().endsWith(".png")) {
                ImageIO.write(output, "png", new File(dstput));
            } else if (srcput.toLowerCase().endsWith(".gif")) {
                ImageIO.write(output, "gif", new File(dstput));
            } else if (srcput.toLowerCase().endsWith(".jpg") 
                    || srcput.toLowerCase().endsWith(".jpeg")) {
                ImageIO.write(output, "jpg", new File(dstput));
            } else if (srcput.toLowerCase().endsWith(".bmp")) {
                ImageIO.write(output, "bmp", new File(dstput));
            }
            //output.flush();
            //source.flush();
        } catch (IOException ex) {
            Logger.getLogger(ImageRotate.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static AffineTransform rotate90plus(BufferedImage source) {
        AffineTransform transform = new AffineTransform();
        transform.rotate(Math.PI / 2, source.getWidth() / 2, source.getHeight() / 2);
        double offset = (source.getWidth() - source.getHeight()) / 2;
        transform.translate(offset, offset);
        return transform;
    }

    private static AffineTransform rotate90minus(BufferedImage source) {
        AffineTransform transform = new AffineTransform();
        transform.rotate(-Math.PI / 2, source.getWidth() / 2, source.getHeight() / 2);
        double offset = (source.getWidth() - source.getHeight()) / 2;
        transform.translate(-offset, -offset);
        return transform;
    }*/

}
