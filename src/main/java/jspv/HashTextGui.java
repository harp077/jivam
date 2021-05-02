package jspv;

import jspv.converter.ImageFormats;
import jspv.converter.ImageConverter;
import jspv.fsmodel.FileSystemModel;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import net.coobird.thumbnailator.util.ThumbnailatorUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Async;

@Component
@Scope("singleton")
@DependsOn(value = {"fileSystemModel", "actionFacade"})
@Description("main")
public class HashTextGui extends javax.swing.JFrame {

    @Inject
    private FileSystemModel fsModel;
    @Inject
    private ActionFacade actionFacade;

    @Value("${skin}")
    private String currentLAF;
    //@Value("${help}")
    //private String help;
    @Value("${top}")
    private String top;
    @Value("${perevod}")
    private String perevod;    

    public static String langsel;  
    public static AbstractApplicationContext ctx;
    public static HashTextGui frame;
    private static Dimension frameDimension = new Dimension(900, 600);
    public ImageIcon FrameIcon = new ImageIcon(getClass().getResource("/img/FrameIcon-3.png"));
    //public String[] alphabet = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
    //private Random mygen = new Random(new Date().getTime());
    public String[] filtersArray = {
        "high quality", // "smooth"     
        "high speed" // "fast"
    //,"area average",
    //"replicate",
    //"default"
    };
    public String[] langArray = { "en", "ru" };    
    private File tmpFile;
    //private ImageIcon fimg;
    //private Image limg;
    //private ImageIcon icon;
    //private File picf;

    public HashTextGui() {
        initComponents();
        this.setIconImage(FrameIcon.getImage());
    }

    @PostConstruct
    public void afterBirn() {
        this.bcomboDevice.setModel(new DefaultComboBoxModel<>(fsModel.getAllrootsString()));
        this.bcomboFilter.setModel(new DefaultComboBoxModel<>(filtersArray));
        this.bcomboLang.setModel(new DefaultComboBoxModel<>(langArray));
        this.fsjTree.setModel(fsModel);
        this.fsjTree.setRootVisible(true);
        this.fsjTree.setShowsRootHandles(true);
        this.fsjTree.setEditable(false);
        this.setTitle(top);
        this.mainSplitPane.setDividerLocation(0.3);
        this.fsjTree.setComponentPopupMenu(mpMenu);
        this.ImgLabel.setComponentPopupMenu(mpMenu);
        langsel=this.perevod;
        //this.bAbout.setVisible(false);
        //this.bHelp.setVisible(false);
        this.bcomboLang.setSelectedItem(perevod);
        this.bcomboLang.setVisible(false);
        this.labelLang.setVisible(false);
        //this.mainSplitPane.setBorder(javax.swing.BorderFactory.createTitledBorder(getMSG("main.img.view",perevod)));
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

    //@Async - ЗДЕСЬ НЕ РАБОТАЕТ, РАБОТАЕТ ИЗ ACTION-FACADE, НО ГЛЮЧИТ !!!!!!!!!!!!!
    public void showImage(String fpath, String scale) {
        //ImgLabel.setIcon(null);
        try {
            if (fpath.equals("")||scale.equals("")) {
                return;
            }
            outTF.setText("Please wait ! Show thread = " + Thread.currentThread().getName());
            String ext = fpath.toLowerCase();
            //System.out.println(ext);
            //if (!(ext.endsWith(".bmp") ||ext.endsWith(".png") || ext.endsWith(".gif") || ext.endsWith(".jpg") || ext.endsWith(".jpeg"))) {
            if (!(ext.endsWith(".png") || ext.endsWith(".gif") || ext.endsWith(".jpg") || ext.endsWith(".jpeg"))) {
                outTF.setText("");
                ImgLabel.setIcon(null);
                return;
            }
            //new Thread(() -> outTF.setText("Please wait ! Show thread = " + Thread.currentThread().getName())).start();
            /*if (ext.endsWith(".bmp")) {
                //if(tmpFile.exists()) tmpFile.delete();
                String newpath = fpath.substring(0, fpath.length() - 4);
                try {
                    tmpFile = File.createTempFile("tmp", ".png");
                    newpath = tmpFile.getPath();//newpath+".png";
                } catch (IOException ex) {
                    Logger.getLogger(HashTextGui.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.out.println(newpath);
                //if (!new File(newpath).exists())
                ImageConverter.imgConvert(fpath, newpath, ImageFormats.PNG_FORMAT);
                fpath = newpath;
                //fsjTree.updateUI();
            } */           
            ImageIcon fimg = new ImageIcon(fpath);
            double k = 0.0;
            if (scale.equals("by window")) {
                k = Math.min((0.1 + imgScrollPane.getHeight()) / (0.1 + fimg.getIconHeight()), (0.1 + imgScrollPane.getWidth()) / (0.1 + fimg.getIconWidth()));
                if (k < 1) {
                    k = k - 0.0099;
                } else k=1.0;
            } else {
                k = (0.01 + Integer.parseInt(scale)) / 100;
            }
            System.out.println("k=" + k);
            Image limg = fimg
                    .getImage()
                    .getScaledInstance((int) Math.round(k * fimg.getIconWidth()), (int) Math.round(k * fimg.getIconHeight()), actionFacade.getSelectedScaleFilter(bcomboFilter.getSelectedItem().toString()));
            ImageIcon icon = new ImageIcon(limg);
            //ImageIcon icon = new ImageIcon(new ImageIcon(jTree1.getSelectionPath().getLastPathComponent().toString()).getImage().getScaledInstance(jScrollPane1.getWidth() - 5, jScrollPane1.getHeight() - 5, Image.SCALE_DEFAULT));
            ImgLabel.setIcon(icon);
            ImgLabel.setSize(imgScrollPane.getWidth() - 5, imgScrollPane.getHeight() - 5);
            //outTF.setText("");
            String ImgProp = "";
            File picf = new File(fpath);
            ImgProp
                    = "Scale=" + Math.round(100 * k)
                    + "%, Dimension=" + fimg.getIconWidth() + "x" + fimg.getIconHeight()
                    + ", Name=" + picf.getName()
                    + ", Size=" + picf.length() + " bytes";
            outTF.setText(ImgProp);
            SwingUtilities.updateComponentTreeUI(imgPanel);
        } catch (NullPointerException nn) {        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mpMenu = new javax.swing.JPopupMenu();
        mpConvert = new javax.swing.JMenuItem();
        mpRotate = new javax.swing.JMenuItem();
        mpResize = new javax.swing.JMenuItem();
        topjToolBar = new javax.swing.JToolBar();
        labelLang = new javax.swing.JLabel();
        bcomboLang = new javax.swing.JComboBox<>();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        jLabel2 = new javax.swing.JLabel();
        bcomboDevice = new javax.swing.JComboBox<>();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        jLabel4 = new javax.swing.JLabel();
        bcomboFilter = new javax.swing.JComboBox<>();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jLabel1 = new javax.swing.JLabel();
        bcomboScale = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        bResize = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        bRotate = new javax.swing.JButton();
        jSeparator6 = new javax.swing.JToolBar.Separator();
        bConvert = new javax.swing.JButton();
        jSeparator7 = new javax.swing.JToolBar.Separator();
        bHelp = new javax.swing.JButton();
        jSeparator8 = new javax.swing.JToolBar.Separator();
        bAbout = new javax.swing.JButton();
        jSeparator9 = new javax.swing.JToolBar.Separator();
        bQuit = new javax.swing.JButton();
        mainSplitPane = new javax.swing.JSplitPane();
        fsScrollPane = new javax.swing.JScrollPane();
        fsjTree = new javax.swing.JTree();
        imgPanel = new javax.swing.JPanel();
        imgScrollPane = new javax.swing.JScrollPane();
        ImgLabel = new javax.swing.JLabel();
        outTF = new javax.swing.JTextField();
        topjMenuBar = new javax.swing.JMenuBar();
        mFile = new javax.swing.JMenu();
        mExit = new javax.swing.JMenuItem();
        mManipulations = new javax.swing.JMenu();
        mConvert = new javax.swing.JMenuItem();
        mRotate = new javax.swing.JMenuItem();
        mResize = new javax.swing.JMenuItem();
        mHelpAbout = new javax.swing.JMenu();
        mHelp = new javax.swing.JMenuItem();
        mAbout = new javax.swing.JMenuItem();

        mpConvert.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/16x16/Converter-icon-16.png"))); // NOI18N
        mpConvert.setText("Convert Image");
        mpConvert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mpConvertActionPerformed(evt);
            }
        });
        mpMenu.add(mpConvert);

        mpRotate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/16x16/rotate-16.png"))); // NOI18N
        mpRotate.setText("Rotate Image");
        mpRotate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mpRotateActionPerformed(evt);
            }
        });
        mpMenu.add(mpRotate);

        mpResize.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/16x16/resize-16.png"))); // NOI18N
        mpResize.setText("Resize Image");
        mpResize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mpResizeActionPerformed(evt);
            }
        });
        mpMenu.add(mpResize);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("HashText");
        setUndecorated(true);

        topjToolBar.setBorder(javax.swing.BorderFactory.createTitledBorder("ToolBar"));
        topjToolBar.setFloatable(false);

        labelLang.setText("Lang = ");
        topjToolBar.add(labelLang);

        bcomboLang.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "en", "ru" }));
        bcomboLang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bcomboLangActionPerformed(evt);
            }
        });
        topjToolBar.add(bcomboLang);
        topjToolBar.add(jSeparator3);

        jLabel2.setText("Device: ");
        topjToolBar.add(jLabel2);

        bcomboDevice.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        bcomboDevice.setToolTipText("Device");
        bcomboDevice.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        bcomboDevice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bcomboDeviceActionPerformed(evt);
            }
        });
        topjToolBar.add(bcomboDevice);
        topjToolBar.add(jSeparator4);

        jLabel4.setText("Filter: ");
        topjToolBar.add(jLabel4);

        bcomboFilter.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "smooth", "fast", "default", "replicate", "area average", " " }));
        bcomboFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bcomboFilterActionPerformed(evt);
            }
        });
        topjToolBar.add(bcomboFilter);
        topjToolBar.add(jSeparator1);

        jLabel1.setText("Scale: ");
        topjToolBar.add(jLabel1);

        bcomboScale.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "25", "50", "75", "100", "125", "150", "200", "by window" }));
        bcomboScale.setSelectedIndex(7);
        bcomboScale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bcomboScaleActionPerformed(evt);
            }
        });
        topjToolBar.add(bcomboScale);

        jLabel3.setText(" % ");
        topjToolBar.add(jLabel3);
        topjToolBar.add(jSeparator5);

        bResize.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/16x16/resize-16.png"))); // NOI18N
        bResize.setToolTipText("Resize Image");
        bResize.setFocusable(false);
        bResize.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bResize.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        bResize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bResizeActionPerformed(evt);
            }
        });
        topjToolBar.add(bResize);
        topjToolBar.add(jSeparator2);

        bRotate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/16x16/rotate-16.png"))); // NOI18N
        bRotate.setToolTipText("Rotate Image");
        bRotate.setFocusable(false);
        bRotate.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bRotate.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        bRotate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bRotateActionPerformed(evt);
            }
        });
        topjToolBar.add(bRotate);
        topjToolBar.add(jSeparator6);

        bConvert.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/16x16/Converter-icon-16.png"))); // NOI18N
        bConvert.setToolTipText("Convert Image");
        bConvert.setFocusable(false);
        bConvert.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bConvert.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        bConvert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bConvertActionPerformed(evt);
            }
        });
        topjToolBar.add(bConvert);
        topjToolBar.add(jSeparator7);

        bHelp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/16x16/info-cyan-16.png"))); // NOI18N
        bHelp.setToolTipText("Help");
        bHelp.setFocusable(false);
        bHelp.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bHelp.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        bHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bHelpActionPerformed(evt);
            }
        });
        topjToolBar.add(bHelp);
        topjToolBar.add(jSeparator8);

        bAbout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/16x16/help-green-16.png"))); // NOI18N
        bAbout.setToolTipText("About");
        bAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bAboutActionPerformed(evt);
            }
        });
        topjToolBar.add(bAbout);
        topjToolBar.add(jSeparator9);

        bQuit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/16x16/quit.png"))); // NOI18N
        bQuit.setFocusable(false);
        bQuit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bQuit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        bQuit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bQuitActionPerformed(evt);
            }
        });
        topjToolBar.add(bQuit);

        getContentPane().add(topjToolBar, java.awt.BorderLayout.NORTH);

        mainSplitPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Image Viewer"));
        mainSplitPane.setDividerLocation(99);
        mainSplitPane.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                mainSplitPaneComponentResized(evt);
            }
        });
        mainSplitPane.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                mainSplitPanePropertyChange(evt);
            }
        });

        fsScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Files"));
        fsScrollPane.setToolTipText("");
        fsScrollPane.setName("File"); // NOI18N

        fsjTree.setModel(fsModel);
        fsjTree.setToolTipText("");
        fsjTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fsjTreeMouseClicked(evt);
            }
        });
        fsjTree.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                fsjTreeKeyReleased(evt);
            }
        });
        fsScrollPane.setViewportView(fsjTree);

        mainSplitPane.setLeftComponent(fsScrollPane);

        imgPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Image"));

        ImgLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        imgScrollPane.setViewportView(ImgLabel);

        javax.swing.GroupLayout imgPanelLayout = new javax.swing.GroupLayout(imgPanel);
        imgPanel.setLayout(imgPanelLayout);
        imgPanelLayout.setHorizontalGroup(
            imgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(imgScrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 765, Short.MAX_VALUE)
        );
        imgPanelLayout.setVerticalGroup(
            imgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(imgScrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE)
        );

        mainSplitPane.setRightComponent(imgPanel);

        getContentPane().add(mainSplitPane, java.awt.BorderLayout.CENTER);

        outTF.setEditable(false);
        outTF.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        outTF.setBorder(javax.swing.BorderFactory.createTitledBorder("Info"));
        getContentPane().add(outTF, java.awt.BorderLayout.SOUTH);
        outTF.getAccessibleContext().setAccessibleName("Information");

        mFile.setText("File");

        mExit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/16x16/quit.png"))); // NOI18N
        mExit.setText("Exit");
        mExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mExitActionPerformed(evt);
            }
        });
        mFile.add(mExit);

        topjMenuBar.add(mFile);

        mManipulations.setText("Manipulations");

        mConvert.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/16x16/Converter-icon-16.png"))); // NOI18N
        mConvert.setText("Convert Image");
        mConvert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mConvertActionPerformed(evt);
            }
        });
        mManipulations.add(mConvert);

        mRotate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/16x16/rotate-16.png"))); // NOI18N
        mRotate.setText("Rotate Image");
        mRotate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mRotateActionPerformed(evt);
            }
        });
        mManipulations.add(mRotate);

        mResize.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/16x16/resize-16.png"))); // NOI18N
        mResize.setText("Resize Image");
        mResize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mResizeActionPerformed(evt);
            }
        });
        mManipulations.add(mResize);

        topjMenuBar.add(mManipulations);

        mHelpAbout.setText("Info");

        mHelp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/16x16/info-cyan-16.png"))); // NOI18N
        mHelp.setText("Help");
        mHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mHelpActionPerformed(evt);
            }
        });
        mHelpAbout.add(mHelp);

        mAbout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/16x16/help-green-16.png"))); // NOI18N
        mAbout.setText("About");
        mAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mAboutActionPerformed(evt);
            }
        });
        mHelpAbout.add(mAbout);

        topjMenuBar.add(mHelpAbout);

        setJMenuBar(topjMenuBar);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void bAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bAboutActionPerformed
        actionFacade.about(frame);
    }//GEN-LAST:event_bAboutActionPerformed

    private void mAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mAboutActionPerformed
        actionFacade.about(frame);
    }//GEN-LAST:event_mAboutActionPerformed

    private void mExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mExitActionPerformed
        actionFacade.exit(frame);
    }//GEN-LAST:event_mExitActionPerformed

    private void fsjTreeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fsjTreeMouseClicked
        try {
            //new Thread(() ->
            showImage(fsjTree.getSelectionPath().getLastPathComponent().toString(), bcomboScale.getSelectedItem().toString())
            ;//).start();
        } catch (NullPointerException ne) {
        }
    }//GEN-LAST:event_fsjTreeMouseClicked

    private void bcomboDeviceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bcomboDeviceActionPerformed
        fsModel.setRoot(fsModel.getAllroots()[bcomboDevice.getSelectedIndex()].getPath());
        fsjTree.setModel(fsModel);
        frame.outTF.setText("");
        SwingUtilities.updateComponentTreeUI(fsjTree);
    }//GEN-LAST:event_bcomboDeviceActionPerformed

    private void bcomboScaleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bcomboScaleActionPerformed
        try {
            //new Thread(() -> 
            showImage(fsjTree.getSelectionPath().getLastPathComponent().toString(), bcomboScale.getSelectedItem().toString())
            ;//).start();
        } catch (NullPointerException ne) {
        }
    }//GEN-LAST:event_bcomboScaleActionPerformed

    private void fsjTreeKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fsjTreeKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_DOWN || evt.getKeyCode() == KeyEvent.VK_UP) {
            //new Thread(() -> 
            showImage(fsjTree.getSelectionPath().getLastPathComponent().toString(), bcomboScale.getSelectedItem().toString())
            ;//).start();
        }
    }//GEN-LAST:event_fsjTreeKeyReleased

    private void bHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bHelpActionPerformed
        JOptionPane.showMessageDialog(frame, actionFacade.getMSG("jivam.help.txt", perevod), actionFacade.getMSG("jivam.help", perevod), JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_bHelpActionPerformed

    private void mHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mHelpActionPerformed
        JOptionPane.showMessageDialog(frame, actionFacade.getMSG("jivam.help.txt", perevod), actionFacade.getMSG("jivam.help", perevod), JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_mHelpActionPerformed

    private void bcomboFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bcomboFilterActionPerformed
        try {
            //new Thread(() ->
            showImage(fsjTree.getSelectionPath().getLastPathComponent().toString(), bcomboScale.getSelectedItem().toString())
            ;//).start();
        } catch (NullPointerException ne) {
        }
    }//GEN-LAST:event_bcomboFilterActionPerformed

    private void mainSplitPaneComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_mainSplitPaneComponentResized
        if (bcomboScale.getSelectedItem().toString().equals("by window")) {
            try {
                //new Thread(() ->
                showImage(fsjTree.getSelectionPath().getLastPathComponent().toString(), bcomboScale.getSelectedItem().toString())
                ;//).start();
            } catch (NullPointerException ne) {
            }
        }
    }//GEN-LAST:event_mainSplitPaneComponentResized

    private void mainSplitPanePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_mainSplitPanePropertyChange
        //System.out.println(evt.getPropertyName());
        if (evt.getPropertyName().equals("dividerLocation") && bcomboScale.getSelectedItem().toString().equals("by window")) {
            try {
                //new Thread(() ->
                showImage(fsjTree.getSelectionPath().getLastPathComponent().toString(), bcomboScale.getSelectedItem().toString())
                ;//).start();
            } catch (NullPointerException ne) {
            }
        }
    }//GEN-LAST:event_mainSplitPanePropertyChange

    private void bConvertActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bConvertActionPerformed
        try {
            actionFacade.picConvert(fsjTree.getSelectionPath().getLastPathComponent().toString(), frame);
        } catch (NullPointerException ne) {
            JOptionPane.showMessageDialog(frame, "File not selected !", "Info", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_bConvertActionPerformed

    private void mConvertActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mConvertActionPerformed
        try {
            actionFacade.picConvert(fsjTree.getSelectionPath().getLastPathComponent().toString(), frame);
        } catch (NullPointerException ne) {
            JOptionPane.showMessageDialog(frame, "File not selected !", "Info", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_mConvertActionPerformed

    private void mpConvertActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mpConvertActionPerformed
        try {
            actionFacade.picConvert(fsjTree.getSelectionPath().getLastPathComponent().toString(), frame);
        } catch (NullPointerException ne) {
            JOptionPane.showMessageDialog(frame, "File not selected !", "Info", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_mpConvertActionPerformed

    private void bRotateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bRotateActionPerformed
        try {
            actionFacade.picRotate(fsjTree.getSelectionPath().getLastPathComponent().toString(), frame);
        } catch (NullPointerException ne) {
            JOptionPane.showMessageDialog(frame, "File not selected !", "Info", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_bRotateActionPerformed

    private void mRotateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mRotateActionPerformed
        try {
            actionFacade.picRotate(fsjTree.getSelectionPath().getLastPathComponent().toString(), frame);
        } catch (NullPointerException ne) {
            JOptionPane.showMessageDialog(frame, "File not selected !", "Info", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_mRotateActionPerformed

    private void mpRotateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mpRotateActionPerformed
        try {
            actionFacade.picRotate(fsjTree.getSelectionPath().getLastPathComponent().toString(), frame);
        } catch (NullPointerException ne) {
            JOptionPane.showMessageDialog(frame, "File not selected !", "Info", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_mpRotateActionPerformed

    private void bResizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bResizeActionPerformed
        try {
            actionFacade.picResize(fsjTree.getSelectionPath().getLastPathComponent().toString(), frame);
        } catch (NullPointerException ne) {
            JOptionPane.showMessageDialog(frame, "File not selected !", "Info", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_bResizeActionPerformed

    private void mResizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mResizeActionPerformed
        try {
            actionFacade.picResize(fsjTree.getSelectionPath().getLastPathComponent().toString(), frame);
        } catch (NullPointerException ne) {
            JOptionPane.showMessageDialog(frame, "File not selected !", "Info", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_mResizeActionPerformed

    private void mpResizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mpResizeActionPerformed
        try {
            actionFacade.picResize(fsjTree.getSelectionPath().getLastPathComponent().toString(), frame);
        } catch (NullPointerException ne) {
            JOptionPane.showMessageDialog(frame, "File not selected !", "Info", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_mpResizeActionPerformed

    private void bcomboLangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bcomboLangActionPerformed
        this.perevod=bcomboLang.getSelectedItem().toString();
        actionFacade.perevod=this.perevod;
        langsel=this.perevod;
        //SwingUtilities.updateComponentTreeUI(this);
    }//GEN-LAST:event_bcomboLangActionPerformed

    private void bQuitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bQuitActionPerformed
        actionFacade.exit(frame);
    }//GEN-LAST:event_bQuitActionPerformed

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                //ApplicationContext 
                // Shutdown Spring container gracefully in non-web applications !!
                ctx = new AnnotationConfigApplicationContext(AppContext.class);
                //ConfigurableApplicationContext ctx = new AnnotationConfigApplicationContext(AppContext.class);
                ctx.registerShutdownHook();
                // app runs here...
                // main method exits, hook is called prior to the app shutting down...
                // define @PreDestroy methods for your beans !!! - it is called before close App !!!                
                frame = ctx.getBean(HashTextGui.class);
                frame.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
                frame.setLF(frame);
                frame.setSize(frameDimension);
                frame.setMinimumSize(frameDimension);
                frame.setAlwaysOnTop(false);
                frame.setVisible(true);
                System.out.println("ImageIO.getReaderFileSuffixes() = "+Arrays.asList(ImageIO.getReaderFileSuffixes()));
                System.out.println("ImageIO.getWriterFileSuffixes() = "+Arrays.asList(ImageIO.getWriterFileSuffixes()));
                System.out.println("ThumbnailatorUtils.getSupportedOutputFormats() = "+ThumbnailatorUtils.getSupportedOutputFormats());
                //Locale [] alocales = Locale.getAvailableLocales();
                //for (Locale al: alocales)
                //System.out.println(al.toLanguageTag());
                //System.out.println(langsel);
                //System.out.println(ctx.getMessage("jivam.help", null, "help", new Locale(langsel)));
                //frame.mainSplitPane.setBorder(javax.swing.BorderFactory.createTitledBorder(actionFacade.getMSG("main.img.view",langsel)));
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JLabel ImgLabel;
    private javax.swing.JButton bAbout;
    private javax.swing.JButton bConvert;
    private javax.swing.JButton bHelp;
    private javax.swing.JButton bQuit;
    private javax.swing.JButton bResize;
    private javax.swing.JButton bRotate;
    public javax.swing.JComboBox<String> bcomboDevice;
    public javax.swing.JComboBox<String> bcomboFilter;
    private javax.swing.JComboBox<String> bcomboLang;
    private javax.swing.JComboBox<String> bcomboScale;
    private javax.swing.JScrollPane fsScrollPane;
    private javax.swing.JTree fsjTree;
    public javax.swing.JPanel imgPanel;
    public javax.swing.JScrollPane imgScrollPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JToolBar.Separator jSeparator6;
    private javax.swing.JToolBar.Separator jSeparator7;
    private javax.swing.JToolBar.Separator jSeparator8;
    private javax.swing.JToolBar.Separator jSeparator9;
    private javax.swing.JLabel labelLang;
    private javax.swing.JMenuItem mAbout;
    private javax.swing.JMenuItem mConvert;
    private javax.swing.JMenuItem mExit;
    private javax.swing.JMenu mFile;
    private javax.swing.JMenuItem mHelp;
    private javax.swing.JMenu mHelpAbout;
    private javax.swing.JMenu mManipulations;
    private javax.swing.JMenuItem mResize;
    private javax.swing.JMenuItem mRotate;
    private static javax.swing.JSplitPane mainSplitPane;
    private javax.swing.JMenuItem mpConvert;
    private javax.swing.JPopupMenu mpMenu;
    private javax.swing.JMenuItem mpResize;
    private javax.swing.JMenuItem mpRotate;
    public static javax.swing.JTextField outTF;
    private javax.swing.JMenuBar topjMenuBar;
    private javax.swing.JToolBar topjToolBar;
    // End of variables declaration//GEN-END:variables
}
