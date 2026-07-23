package UI;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.swing.ImageIcon;

public class UIUtils {

    /**
     * Generate a clean vector star icon (smooth and clear for all sizes)
     * @param filled true for a full star (favorite), false for a hollow star
     * @param size size in pixels
     * @return a ready-to-use ImageIcon
     */
    public static ImageIcon generateStarIcon(boolean filled, int size) {
        // Taille exacte demandée, pas de suréchantillonnage
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        g2d.translate(size / 2.0, size / 2.0);
        double scale = (size / 2.0) * 0.8;

        Path2D.Double starPath = new Path2D.Double();
        int points = 5;
        for (int i = 0; i < 2 * points; i++) {
            double angle = Math.PI / 2 + (i * Math.PI / points);
            double r = (i % 2 == 0) ? scale : scale * 0.45;
            double x = r * Math.cos(angle);
            double y = r * Math.sin(angle);
            if (i == 0) starPath.moveTo(x, y);
            else starPath.lineTo(x, y);
        }
        starPath.closePath();

        if (filled) {
            g2d.setColor(Color.BLACK);
            g2d.fill(starPath);
        } else {
            g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.setColor(Color.GRAY);
            g2d.draw(starPath);
        }
        g2d.dispose();

        return new ImageIcon(img);
    }
    /**
     * Safely load an image
     */
    public static ImageIcon loadIcon(String path) {
        URL imgURL = UIUtils.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Impossible de trouver l'image : " + path);
            return null;
        }
    }

    /**
     * Load a Java AWT image (for the window icon)
     */
    public static Image loadImage(String path) {
        URL imgURL = UIUtils.class.getResource(path);
        if (imgURL != null) {
            return Toolkit.getDefaultToolkit().getImage(imgURL);
        } else {
            System.err.println("Impossible de trouver l'image de l'application : " + path);
            return null;
        }
    }

    /**
     * Resizes a high-resolution image to a small size
     * using the Multi-Step Downscaling technique.
     */
    public static ImageIcon loadAndScaleIcon(String path, int targetWidth, int targetHeight) {
        URL imgURL = UIUtils.class.getResource(path);
        if (imgURL == null) {
            System.err.println("⚠️ Impossible de trouver l'image : " + path);
            return null;
        }

        Image srcImg = new ImageIcon(imgURL).getImage();
        int w = srcImg.getWidth(null);
        int h = srcImg.getHeight(null);
        if (w <= 0 || h <= 0) return null;

        // Rendre à 3x puis downscaler → net même à 125%
        int renderW = targetWidth * 3;
        int renderH = targetHeight * 3;

        BufferedImage hiRes = new BufferedImage(renderW, renderH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = hiRes.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.drawImage(srcImg, 0, 0, renderW, renderH, null);
        g2.dispose();

        BufferedImage finalImg = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gFinal = finalImg.createGraphics();
        gFinal.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        gFinal.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gFinal.drawImage(hiRes, 0, 0, targetWidth, targetHeight, null);
        gFinal.dispose();

        return new ImageIcon(finalImg);
    }

    public static Image loadAndScaleImage(String path, int width, int height) {
        ImageIcon icon = loadAndScaleIcon(path, width, height);
        return icon != null ? icon.getImage() : null;
    }
}