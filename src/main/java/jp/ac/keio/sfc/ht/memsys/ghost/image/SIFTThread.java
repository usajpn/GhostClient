package jp.ac.keio.sfc.ht.memsys.ghost.image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by aqram on 11/3/14.
 */
public class SIFTThread implements Runnable{

    private BufferedImage mImage;
    byte[] original;
    private LinkedBlockingQueue mQueue;

    public SIFTThread (byte[] image, LinkedBlockingQueue queue){
        InputStream in = new ByteArrayInputStream(image);
        mQueue = queue;
        try {
            mImage = ImageIO.read(in);
            original = image;

            byte[] imageInByte;

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(mImage, "jpg", baos);
            baos.flush();
            imageInByte = baos.toByteArray();
            baos.close();

            InputStream in2 = new ByteArrayInputStream(imageInByte);
            BufferedImage bImageFromConvert = ImageIO.read(in2);

//            File f = new File("/Users/usa/orf2014/ORF-SIFT-Web/img/cloud.jpeg");
//            ImageIO.write(bImageFromConvert, "jpeg", f);

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void run() {
        System.out.println("-----------------------------");
        System.out.println(mImage.getWidth());

        try {
            mQueue.put(mImage);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //mQueue.notifyAll();

        //Vector<Feature> features = SIFT.getFeatures(mImage.getWidth(), mImage.getHeight(), getPixelsTab(original));
        //System.out.println(String.valueOf(features.size()));
    }

    private int[] getPixelsTab(byte[] buf) {

        int width =mImage.getWidth();
        int height = mImage.getHeight();

        int[] pixels = convertTo2DUsingGetRGB(mImage);


        // copy pixels of picture into the tab
        // On Android, Color are coded in 4 bytes (argb),
        // whereas SIFT needs color coded in 3 bytes (rgb)

       for (int i = 0; i < (width * height); i++)
            pixels[i] &= 0x00ffffff;

        return pixels;
    }

    /*
    public void drawFeature(Canvas c, float x, float y, double scale,
                            double orientation) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // line too small...
        scale *= 6.;

        double sin = Math.sin(orientation);
        double cos = Math.cos(orientation);

        paint.setStrokeWidth(2f);
        paint.setColor(Color.GREEN);
        c.drawLine((float) x, (float) y, (float) (x - (sin - cos) * scale), (float) (y + (sin + cos) * scale), paint);


        paint.setStrokeWidth(4f);
        paint.setColor(Color.YELLOW);
        c.drawPoint(x, y, paint);
    }
    */

    private static int[] convertTo2DUsingGetRGB(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] result = new int[height * width];

        for (int row = 0; row < height; row++) {
            for (int col = 1; col <=width; col++) {
                result[row * col] = image.getRGB(col-1, row);
            }
        }

        return result;
    }

    private static int[][] convertTo2DWithoutUsingGetRGB(BufferedImage image) {

        final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        final int width = image.getWidth();
        final int height = image.getHeight();
        final boolean hasAlphaChannel = image.getAlphaRaster() != null;

        int[][] result = new int[height][width];
        if (hasAlphaChannel) {
            final int pixelLength = 4;
            for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
                int argb = 0;
                argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
                argb += ((int) pixels[pixel + 1] & 0xff); // blue
                argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
                argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
                result[row][col] = argb;
                col++;
                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        } else {
            final int pixelLength = 3;
            for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
                int argb = 0;
                argb += -16777216; // 255 alpha
                argb += ((int) pixels[pixel] & 0xff); // blue
                argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
                argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
                result[row][col] = argb;
                col++;
                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        }

        return result;
    }
}
