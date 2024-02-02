package org.seamless.util;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import org.fourthline.cling.support.model.dlna.DLNAProfiles;
/* loaded from: classes.dex */
public class Gfx {
    public static byte[] resizeProportionally(ImageIcon icon, String contentType, int newWidth, int newHeight) throws IOException {
        int newWidth2 = newWidth;
        int newHeight2 = newHeight;
        double widthRatio = newWidth2 != icon.getIconWidth() ? newWidth2 / icon.getIconWidth() : 1.0d;
        double heightRatio = newHeight2 != icon.getIconHeight() ? newHeight2 / icon.getIconHeight() : 1.0d;
        if (widthRatio < heightRatio) {
            newHeight2 = (int) (icon.getIconHeight() * widthRatio);
        } else {
            newWidth2 = (int) (icon.getIconWidth() * heightRatio);
        }
        int imageType = DLNAProfiles.DLNAMimeTypes.MIME_IMAGE_PNG.equals(contentType) ? 2 : 1;
        BufferedImage bImg = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), imageType);
        Graphics2D g2d = bImg.createGraphics();
        g2d.drawImage(icon.getImage(), 0, 0, icon.getIconWidth(), icon.getIconHeight(), (ImageObserver) null);
        g2d.dispose();
        BufferedImage scaledImg = getScaledInstance(bImg, newWidth2, newHeight2, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR, false);
        String formatName = "";
        if (DLNAProfiles.DLNAMimeTypes.MIME_IMAGE_PNG.equals(contentType)) {
            formatName = "png";
        } else if (DLNAProfiles.DLNAMimeTypes.MIME_IMAGE_JPEG.equals(contentType) || "image/jpg".equals(contentType)) {
            formatName = "jpeg";
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        ImageIO.write(scaledImg, formatName, baos);
        return baos.toByteArray();
    }

    public static BufferedImage getScaledInstance(BufferedImage img, int targetWidth, int targetHeight, Object hint, boolean higherQuality) {
        int w;
        int h;
        int type = img.getTransparency() != 1 ? 2 : 1;
        BufferedImage ret = img;
        if (higherQuality) {
            w = img.getWidth();
            h = img.getHeight();
        } else {
            w = targetWidth;
            h = targetHeight;
        }
        while (true) {
            if (higherQuality && w > targetWidth) {
                w /= 2;
                if (w < targetWidth) {
                    w = targetWidth;
                }
            }
            if (higherQuality && h > targetHeight && (h = h / 2) < targetHeight) {
                h = targetHeight;
            }
            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, w, h, (ImageObserver) null);
            g2.dispose();
            ret = tmp;
            if (w == targetWidth && h == targetHeight) {
                return ret;
            }
        }
    }
}
