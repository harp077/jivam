package jspv;

import jspv.converter.ImageFormats;
import jspv.converter.ImageConverter;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jspv.converter.ImageRotate;
import jspv.converter.ImgResize;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
//import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
@Lazy(false)
@Description("action-facade")
public class ActionFacade implements ApplicationContextAware {

    @Value("${top}")
    private String top;
    @Value("${skin}")
    private String currentLAF;
    @Value("${perevod}")
    public String perevod;     

    public static List<String> lookAndFeelsDisplay = new ArrayList<>();
    public static List<String> lookAndFeelsRealNames = new ArrayList<>();
    public static String[] imgTipArray = {"PNG", "JPG", "GIF", "BMP"};
    private String nnput;
    public int intbuf;
    private ApplicationContext ctx;

    @PostConstruct
    public void afterBirn() {
        InstallLF();
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
    }
    
    @Override
    public void setApplicationContext (ApplicationContext ctx) {
        this.ctx=ctx;
    }
    
    public String getMSG(String msg, String loc) {
        return
        ctx.getMessage(msg, null, "default", new Locale(loc));
    }    
    
    public void saveProperties (JFrame frame) {
        try {
            PropertiesConfiguration config = new PropertiesConfiguration("cfg/jivam.properties");
            config.setProperty("skin", currentLAF);
            config.setProperty("perevod", perevod);
            config.save();
            JOptionPane.showMessageDialog(frame, "Config saved ","Save Config", JOptionPane.INFORMATION_MESSAGE);
        } catch (ConfigurationException ex) {
            Logger.getLogger(ActionFacade.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Boolean checkFolder(String ffput, JFrame frame) {
        File imgFile = new File(ffput);
        if (imgFile.isDirectory()) {
            JOptionPane.showMessageDialog(frame, "" + ffput + " is folder !", "Folder !", JOptionPane.WARNING_MESSAGE);
            return true;
        }
        return false;
    }

    public void pictureRotate(String ffput, JFrame frame) {
        if (checkFolder(ffput, frame)) {
            return;
        }
        JLabel inLabel = new JLabel("Image: " + ffput);
        JSeparator jsep1 = new JSeparator();
        JLabel tipLabel = new JLabel("Input Rotate Degree (from -360 to +360): ");
        Integer value = new Integer(0);
        Integer min = new Integer(-360);
        Integer max = new Integer(+360);
        Integer step = new Integer(1);
        SpinnerNumberModel spinmodel = new SpinnerNumberModel(value, min, max, step);
        JSpinner spin = new JSpinner(spinmodel);
        //JSpinner.NumberEditor numedit = new JSpinner.NumberEditor(spin, "+000;-000");
        //spin.setEditor(numedit);
        spin.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner js = (JSpinner) e.getSource();
                intbuf = Integer.parseInt(js.getValue().toString());
            }
        });
        Object[] ob = {inLabel, jsep1, tipLabel, spin};
        ImageIcon icon = new ImageIcon(getClass().getResource("/img/24x24/rotate-24.png"));
        int result = JOptionPane.showConfirmDialog(frame, ob, "Image Rotate", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, icon);
        if (result == JOptionPane.OK_OPTION) {
            ImageRotate.thumbnailsRotate(ffput, intbuf);
        }
    }

    public void imageResize(String ffput, JFrame frame) {
        if (checkFolder(ffput, frame)) {
            return;
        }
        JLabel inLabel = new JLabel("Image: " + ffput);
        JSeparator jsep1 = new JSeparator();
        JLabel tipLabel = new JLabel("Input Resize Scale in percent (max=200%): ");
        Integer value = new Integer(100);
        Integer min = new Integer(1);
        Integer max = new Integer(200);
        Integer step = new Integer(1);
        SpinnerNumberModel spinmodel = new SpinnerNumberModel(value, min, max, step);
        JSpinner spin = new JSpinner(spinmodel);
        spin.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner js = (JSpinner) e.getSource();
                intbuf = Integer.parseInt(js.getValue().toString());
                //System.out.println("intbuf = " + intbuf);
            }
        });
        //spin.set
        JSeparator jsep2 = new JSeparator();
        Object[] ob = {inLabel, jsep1, tipLabel, jsep2, spin};
        ImageIcon icon = new ImageIcon(getClass().getResource("/img/24x24/resize-24.png"));
        int result = JOptionPane.showConfirmDialog(frame, ob, "Image Resize", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, icon);
        if (result == JOptionPane.OK_OPTION) {
            //int intbuf=(int)spin.getValue();
            Double db = (0.0001 + intbuf) / 100;
            //(int)spin.getValue();
            System.out.println("scale = " + db);
            ImgResize.resizeImg(ffput, db);
        }
    }

    public void convertImage(String ffput, JFrame frame) {
        if (checkFolder(ffput, frame)) {
            return;
        }
        JComboBox tipSelector = new JComboBox();
        tipSelector.setModel(new javax.swing.DefaultComboBoxModel(imgTipArray));
        nnput = ffput.substring(0, ffput.length() - 4) + "." + tipSelector.getSelectedItem().toString().toLowerCase();
        JLabel inLabel = new JLabel("Input file  = " + ffput);
        JSeparator jsep1 = new JSeparator();
        JSeparator jsep2 = new JSeparator();
        JLabel outLabel = new JLabel("Output file = " + nnput);
        JLabel tipLabel = new JLabel("Select Image Type to convert: ");
        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                nnput = ffput.substring(0, ffput.length() - 4) + "." + tipSelector.getSelectedItem().toString().toLowerCase();
                outLabel.setText("Output file = " + nnput);
            }
        };
        tipSelector.addActionListener(actionListener);
        Object[] ob = {inLabel, jsep1, outLabel, jsep2, tipLabel, tipSelector};
        ImageIcon icon = new ImageIcon(getClass().getResource("/img/24x24/Converter-icon-24.png"));
        int result = JOptionPane.showConfirmDialog(frame, ob, "Image Convert", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, icon);
        if (result == JOptionPane.OK_OPTION) {
            File tmpFile = new File(nnput);
            if (tmpFile.exists()) {
                JOptionPane.showMessageDialog(frame, "File " + nnput + " is already present !", "File already present !", JOptionPane.WARNING_MESSAGE);
                return;
            }
            switch (tipSelector.getSelectedItem().toString().toLowerCase()) {
                case "bmp":
                    ImageConverter.imgConvert(ffput, nnput, ImageFormats.BMP_FORMAT);
                    break;
                case "png":
                    ImageConverter.imgConvert(ffput, nnput, ImageFormats.PNG_FORMAT);
                    break;
                case "jpg":
                    ImageConverter.imgConvert(ffput, nnput, ImageFormats.JPG_FORMAT);
                    break;
                case "gif":
                    ImageConverter.imgConvert(ffput, nnput, ImageFormats.GIF_FORMAT);
                    break;
            }
        }
    }

    @Async
    public void killFileFolder(String ffput, JFrame frame, JTextField jtf, JLabel jlabel) {
        File fftokill = new File(ffput);
        String ffstr;
        if (fftokill.isFile()) {
            ffstr = "delete File " + ffput + " ?";
            int r = JOptionPane.showConfirmDialog(frame, ffstr, "Kill", JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) {
                if (fftokill.delete()) {
                    jtf.setText("File " + ffput + " success deleted !");
                    jlabel.setIcon(null);
                } else {
                    jtf.setText(ffput + "delete file failed !");
                }
            }
        }
        if (fftokill.isDirectory()) {
            if (fftokill.list().length > 0) {
                ffstr = "delete NOT empty Folder " + ffput + " ?";
            } else {
                ffstr = "delete empty Folder " + ffput + " ?";
            }
            int r = JOptionPane.showConfirmDialog(frame, ffstr, "Kill", JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) {
                try {
                    FileUtils.deleteDirectory(fftokill);
                    jtf.setText("Folder " + ffput + " success deleted !");
                } catch (IOException ex) {
                    jtf.setText(ffput + "delete folder failed !");
                    Logger.getLogger(ActionFacade.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public int getSelectedScaleFilter(String str) {
        int imgFilter;
        switch (str) {
            // smooth = area average 
            // fast/default = replicate - see Image.java:
            /*
            public Image getScaledInstance(int width, int height, int hints) {
                ImageFilter filter;
                if ((hints & (SCALE_SMOOTH | SCALE_AREA_AVERAGING)) != 0) {
                    filter = new AreaAveragingScaleFilter(width, height);
                } else {
                    filter = new ReplicateScaleFilter(width, height);
                }
                ImageProducer prod;
                prod = new FilteredImageSource(getSource(), filter);
                return Toolkit.getDefaultToolkit().createImage(prod);
            }            
             */
            case "high quality":
                imgFilter = Image.SCALE_SMOOTH;
                break;
            case "high speed":
                imgFilter = Image.SCALE_FAST;
                break;
            //case "default":      imgFilter=Image.SCALE_DEFAULT;        break;
            //case "replicate":    imgFilter=Image.SCALE_REPLICATE;      break;
            //case "area average": imgFilter=Image.SCALE_AREA_AVERAGING; break;
            default:
                imgFilter = Image.SCALE_SMOOTH;
        }
        System.out.println("Img Filter = " + str + " = " + imgFilter);
        return imgFilter;
    }

    public void exit(JFrame frame) {
        int r = JOptionPane.showConfirmDialog(frame, "Exit Jivam ?", "Quit Jivam", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    public void about(JFrame frame) {
        ImageIcon icon = new ImageIcon(getClass().getResource("/img/jivam.png"));
        JOptionPane.showMessageDialog(frame,
                  "JIVAM - free portable cross-platform image\n"
                + "viewer and manipulator, 100%-pure Java.\n"
                + "Support convert, resize, rotate images.\n"
                + "Support BMP/JPG/PNG/GIF/JPEG formats.\n"
                + "Developed with Java Spring Framework.\n"
                + "Tested in Windows/Linux. Need JRE-1.8.\n"
                + "Roman Koldaev, Saratov city, Russia.\n"
                + "Home=http://jivam.sf.net/ ,\n"
                + "E-mail=harp07@mail.ru",
                top, JOptionPane.INFORMATION_MESSAGE, icon);
    }

    public void MyInstLF(String lf) {
        lookAndFeelsDisplay.add(lf);
        lookAndFeelsRealNames.add(lf);
    }

    public void InstallLF() {
        ///////////////////
        MyInstLF("com.jtattoo.plaf.acryl.AcrylLookAndFeel");
        MyInstLF("com.jtattoo.plaf.aero.AeroLookAndFeel");
        MyInstLF("com.jtattoo.plaf.aluminium.AluminiumLookAndFeel");
        MyInstLF("com.jtattoo.plaf.fast.FastLookAndFeel");
        MyInstLF("com.jtattoo.plaf.hifi.HiFiLookAndFeel");
        MyInstLF("com.jtattoo.plaf.mcwin.McWinLookAndFeel");
        MyInstLF("com.jtattoo.plaf.mint.MintLookAndFeel");
        MyInstLF("com.jtattoo.plaf.noire.NoireLookAndFeel");
        MyInstLF("com.jtattoo.plaf.smart.SmartLookAndFeel");
        MyInstLF("com.jtattoo.plaf.luna.LunaLookAndFeel");
        MyInstLF("com.jtattoo.plaf.texture.TextureLookAndFeel");
        MyInstLF("com.jtattoo.plaf.graphite.GraphiteLookAndFeel");
        MyInstLF("com.jtattoo.plaf.bernstein.BernsteinLookAndFeel");
        ///////////////////////
    }

    public void setLF(JFrame frame) {
        try {
            UIManager.setLookAndFeel(currentLAF);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(ActionFacade.class.getName()).log(Level.SEVERE, null, ex);
        }
        SwingUtilities.updateComponentTreeUI(frame);
        //frame.pack();
    }

    public void changeLF(JFrame frame) {
        String changeLook = (String) JOptionPane.showInputDialog(frame, "Choose Skin Here:", "Select Skin", JOptionPane.QUESTION_MESSAGE, new ImageIcon(getClass().getResource("/img/color_swatch.png")), lookAndFeelsDisplay.toArray(), null);
        if (changeLook != null) {
            for (int a = 0; a < lookAndFeelsDisplay.size(); a++) {
                if (changeLook.equals(lookAndFeelsDisplay.get(a))) {
                    currentLAF = lookAndFeelsRealNames.get(a);
                    setLF(frame);
                    break;
                }
            }
        }
    }

}
