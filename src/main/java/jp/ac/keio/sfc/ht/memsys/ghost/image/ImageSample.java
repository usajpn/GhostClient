package jp.ac.keio.sfc.ht.memsys.ghost.image;

import jp.ac.keio.sfc.ht.memsys.ghost.sift.Feature;
import jp.ac.keio.sfc.ht.memsys.ghost.sift.SIFT;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

public class ImageSample {

    private final BufferedImage mBufferedImage;

    public ImageSample(String size){

        BufferedImage readImage = null;

        try {
            readImage = ImageIO.read(new File("./img/sample_" + size + ".jpeg"));
        } catch (Exception e) {
            e.printStackTrace();
            readImage = null;
        }

        mBufferedImage = readImage;
    }

//    public ImageSample(BufferedImage bi) {
//        mBufferedImage = bi;
//    }

    public BufferedImage getImage(){
        return mBufferedImage;
    }

    public void showImage(Vector<Feature> features){

        JFrame canvas = new JFrame();
        canvas.setSize(mBufferedImage.getWidth(),mBufferedImage.getHeight());
        canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        canvas.setTitle("It's a sample.");
        Container pane = canvas.getContentPane();
        ColorPanel panel = new ColorPanel(mBufferedImage,features);
        pane.add(panel);
        canvas.setVisible(true);

        save(panel);
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

    class ColorPanel extends JPanel {
        BufferedImage theCat;

        private Vector<Feature> mFeatures;
        public ColorPanel(BufferedImage image, Vector<Feature> features){
            theCat = image;
            mFeatures = features;
        }

        public void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.drawImage(theCat, null, 0,0);

            BasicStroke s = new BasicStroke(2f);
            BasicStroke w = new BasicStroke(4f);

            for(Feature f : mFeatures){

                float x = f.location[0];
                float y = f.location[1];
                float scale = f.scale * 6f;
                float orientation = f.orientation;

                double sin = Math.sin(orientation);
                double cos = Math.cos(orientation);

                g2d.setStroke(s);
                g2d.setColor(Color.GREEN);
                g2d.drawLine(Math.round(x), Math.round(y), (int)Math.round(x - (sin - cos) * scale), (int)Math.round(y + (sin + cos) * scale));

                g2d.setStroke(w);
                g2d.setColor(Color.YELLOW);
                g2d.drawLine((int) x, (int) y, (int) x, (int) y);

            }
        }

    }

    public void processSIFT(){

        int[] pixels = SIFTUtil.getPixelsTab(mBufferedImage);

        System.out.println(mBufferedImage.getWidth() +" x " +  mBufferedImage.getHeight());

        for(int i : pixels){
            //System.out.print(i);
            //System.out.print(" ,");
        }

        Vector<Feature> features = SIFT.getFeatures(mBufferedImage.getWidth(), mBufferedImage.getHeight(), pixels);
        showImage(features);
    }

    public void save(JPanel p)
    {
        BufferedImage bImg = new BufferedImage(p.getWidth(), p.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D cg = bImg.createGraphics();
        p.paintAll(cg);
        try {
            if (ImageIO.write(bImg, "jpeg", new File("/Users/usa/cloud.jpeg")))
            {
                System.out.println("-- saved");
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}