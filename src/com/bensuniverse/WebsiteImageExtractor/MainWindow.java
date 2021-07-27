package com.bensuniverse.WebsiteImageExtractor;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

public class MainWindow extends JFrame {

    // used GUI declarations
    private JPanel main_panel;
    private JTextField url;
    private JProgressBar main_progress_bar;
    private JButton download_button;
    private JButton grab_image_button;
    private JLabel image_preview;
    private JScrollPane middle_scroll_pane;
    private JPanel middle_panel;
    private JTextField file_prefix_textarea;
    private JSpinner start_index_spinner;
    private JTextField included_strings_textfield;
    private JTextField excluded_strings_textfield;
    private JButton update_checkboxes_button;
    private JComboBox extension_combobox;
    private JPanel right_panel;
    private JLabel status_label;
    private JTextField folder_location_textfield;
    private JButton browse_button;
    private JButton move_up_button;
    private JButton move_down_button;
    private JScrollPane left_scroll_pane;
    private JLabel current_position_label;
    private JButton delete_selected_items_button;

    // unused GUI declarations
    private JLabel enter_url_label;
    private JLabel extension_label;
    private JLabel image_preview_label;
    private JLabel select_images_label;
    private JLabel auto_select_settings_label;
    private JLabel including_label;
    private JLabel excluding_label;
    private JLabel image_output_settings_label;
    private JLabel file_prefix_label;
    private JLabel start_index_label;
    private JLabel folder_location_label;
    private JSeparator separator_02;
    private JSeparator separator_01;
    private JPanel left_panel;
    private JLabel footer_label;
    private JCheckBox file_rename_checkbox;
    private JButton select_none_button;
    private JButton select_all_button;
    private JPanel checkbox_button_panel;

    // other variables declarations
    private ArrayList<String> srcs = new ArrayList<String>();
    private ArrayList<String> srcs_NOURL = new ArrayList<String>();

    private ArrayList<ImageObject> images = new ArrayList<ImageObject>();

    public static URL selected_image;
//    private ArrayList<JCheckBox> images = new ArrayList<JCheckBox>();

    private String output_folder;

    private JScrollPane image_list;
    private int selected_index;

    private ImageGrabber ig = new ImageGrabber();
    private AutoCheckbox ac = new AutoCheckbox();

    private ImageList il = new ImageList();

    public MainWindow() {

        // set window title
        super(Main.getName());

        // set footer label text (version and other info)
        String footer_text = "Version " + Main.getVersion() + " | " + Main.getAuthor() + " | " + Main.getDate();
        footer_label.setText(footer_text);

        // set JScrollPane scroll speed
        left_scroll_pane.getVerticalScrollBar().setUnitIncrement(16);
        middle_scroll_pane.getVerticalScrollBar().setUnitIncrement(16);

        // set default selected index
        selected_index = 1;

        // set default output folder to current working directory
        try {
            File jar_location = new File (Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            output_folder = jar_location.getAbsolutePath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
//        output_folder = System.getProperty("user.dir");
        folder_location_textfield.setText(output_folder + "/");
        if (output_folder.equals("/")) // root dir
            folder_location_textfield.setText("/");

        // "Grab Images" button listener
        grab_image_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                // reset existing list of images
                srcs = new ArrayList<String>();
                srcs_NOURL = new ArrayList<String>();

                // get all image URLs from website, both with http:// and without main URL
                ArrayList srcs_temp = ig.getImages(url.getText(), true);
                ArrayList srcs_NOURL_temp = ig.getImages(url.getText(), false);

                // remove duplicates from lists
                for (int i = 0; i < srcs_temp.size(); i++) {

                    if (!srcs_NOURL.contains(srcs_NOURL_temp.get(i))) {

                        srcs_NOURL.add((String) srcs_NOURL_temp.get(i));
                        srcs.add((String) srcs_temp.get(i));

                    }
                }

                images.clear();
                for (int i = 0; i < srcs.size(); i++) {

                    images.add(new ImageObject(srcs_NOURL.get(i), srcs.get(i)));

                }

                updateImageLists();

            }
        });

        // "Update Checkboxes" button listener
        update_checkboxes_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                // get list of indicies to select
                ArrayList<Integer> indicies_list = ac.getSelection(images, included_strings_textfield.getText(), excluded_strings_textfield.getText());
                int[] indicies = new int[indicies_list.size()];
                for (int i = 0; i < indicies.length; i++) {

                    indicies[i] = indicies_list.get(i);

                }
                il.getList().setSelectedIndices(indicies);

                // give list focus
                il.getList().requestFocusInWindow();
                middle_panel.updateUI();

            }
        });

        // "Download Images" button listener
        download_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                // download images in new thread to allow for progress bar updates and prevent frozen UI
                new Thread(new Runnable() {
                    public void run() {

                        // download all images
                        ig.downloadImages(images, output_folder, file_rename_checkbox.isSelected(), file_prefix_textarea.getText(), extension_combobox.getSelectedItem().toString(), (int) start_index_spinner.getValue(), main_progress_bar, right_panel, status_label);

                        // when finished, set status text and fill progress bar
                        status_label.setText("Done!");
                        main_progress_bar.setValue(100);
                        main_progress_bar.setString("100%");

                    }
                }).start();
            }
        });

        // "Browse" button listener
        browse_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                JFileChooser file_chooser = new JFileChooser(); // create file chooser
                file_chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); // only allow choosing directories
                file_chooser.setDialogTitle("Select Output Directory"); // set dialog title

                // set current file chooser directory to previously selected directory
                file_chooser.setCurrentDirectory(new File(folder_location_textfield.getText()));

//                file_chooser.setCurrentDirectory(new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI())); // open in current folder

                if (file_chooser.showDialog(MainWindow.getFrames()[0], "Select") == JFileChooser.APPROVE_OPTION) { // if user selects a file

                    File file = file_chooser.getSelectedFile(); // get current "folder"
                    output_folder = file.getAbsolutePath(); // append last directory to path
                    output_folder = file.getAbsolutePath().equals("/") ? output_folder : output_folder + "/"; // don't append '/' to path if in root dir
                    folder_location_textfield.setText(output_folder); // set text field = new file path

                }
            }
        });

        // "Select all" button listener
        select_all_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                il.getList().setSelectionInterval(0, il.getModel().getSize());
                il.getList().requestFocusInWindow();

            }
        });

        // "Delete selected items" button listener
        delete_selected_items_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                // get selected values and remove them from images
                int[] to_remove = il.getList().getSelectedIndices();
                for (int i = to_remove.length - 1; i >= 0; i--) {

                    System.out.println("Removing: " + to_remove[i]);
                    images.remove(to_remove[i]);

                }

                updateImageLists();

            }
        });

        // "Enable custom file names" checkbox listener
        file_rename_checkbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                // disable naming fields if unselected
                boolean selected = file_rename_checkbox.isSelected();

                file_prefix_label.setEnabled(selected);
                file_prefix_textarea.setEnabled(selected);
                start_index_label.setEnabled(selected);
                start_index_spinner.setEnabled(selected);
                extension_label.setEnabled(selected);
                extension_combobox.setEnabled(selected);

            }
        });

        // window resize listener
        this.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent componentEvent) {

                // update image preview
                int width = (int) (main_panel.getWidth() * 0.3);
                left_panel.setSize(width, main_panel.getHeight());
                left_panel.updateUI();

//                middle_panel.setLayout(new BorderLayout());
//                middle_panel.add(image_list, BorderLayout.CENTER);

//                image_list.setSize(width, main_panel.getHeight());
//                middle_panel.updateUI();

                System.out.println("Width (int): " + width);
//                left_scroll_pane.setPreferredSize(new Dimension((int) (main_panel.getWidth() * 0.3), main_panel.getHeight()));
//                middle_scroll_pane.setPreferredSize(new Dimension((int) (main_panel.getWidth() * 0.4), main_panel.getHeight()));
//                    System.out.println("Width of panel: " + width);

                try {

                    image_preview.setIcon(new ImageIcon(ig.getImageFromURL(selected_image, true, width)));
                    main_panel.updateUI();

                } catch (NullPointerException e) {

                    System.out.println("No image selected!");

                }
            }
        });

        // other JPanel properties
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(main_panel);
        this.setSize(850, 550);
        this.setMinimumSize(new Dimension(850, 550));
//        this.setResizable(false);
        this.setLocationRelativeTo(null);

        // set JFrame icon
        try {

            this.setIconImage(new ImageIcon(ImageIO.read(getClass().getResource("/icon.png"))).getImage());

        } catch (IOException e) {

            e.printStackTrace();

        }

        // set URL text field to be selected
        url.requestFocusInWindow();
    }

    private void updateImageLists() {

        try {

            // remove existing box (old checkboxes)
            middle_panel.remove(0);

        } catch (ArrayIndexOutOfBoundsException e) {

            // nothing in panel (first run of program)

        }

        // empty list
        if (srcs.size() == 0) {

            current_position_label.setText("Current position: 0/0");
            image_preview.setIcon(null);

        }

        // set other miscellaneous variables
        main_progress_bar.setValue(0);
        main_progress_bar.setString("0%");
        status_label.setText("");
        included_strings_textfield.setText("");
        excluded_strings_textfield.setText("");
        file_prefix_textarea.setText("");
        start_index_spinner.setValue(0);

        // add checkboxes and update panel to refresh
        image_list = il.getImageList(images);

        middle_panel.setLayout(new BorderLayout());
        middle_panel.add(image_list, BorderLayout.CENTER);

//        middle_panel.add(image_list);
        il.getList().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {

                try {

                    selected_image = ((ImageObject) il.getList().getSelectedValue()).getURL();

                    int width = (int) (main_panel.getWidth() * 0.3);
                    left_panel.setSize((int) (width * 2), main_panel.getHeight());
                    System.out.println("Width (int): " + width);

                    image_preview.setIcon(new ImageIcon(ig.getImageFromURL(selected_image, true, width)));
                    main_panel.updateUI();

                } catch (NullPointerException npe) {

                    main_panel.updateUI();

                }

            }
        });

        middle_panel.updateUI();

    }
}