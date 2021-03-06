import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.xml.crypto.Data;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

class LibrarianDocumentGUI extends JFrame{
    //init all gui elements
    private JButton EditBook = new JButton("EditBook");
    private JButton DeleteBook = new JButton("Delete Book");
    private JButton AddBook = new JButton("Add Book");
    private JButton Create = new JButton("Create Copy");
    private JButton Queue = new JButton("Queue");
    private JButton outstandingRequest = new JButton("Outstanding Request");
    private JTable table;
    private JScrollPane listScroller;
    private TableRowSorter<TableModel> rowSorter;
    private JTextField jtfFilter = new JTextField();
    private JButton jbtFilter = new JButton("Filter");
    private JLabel jLabel1 = new JLabel("Select search criteria");
    private JComboBox selectForSearch;
    private JLabel jLabel = new JLabel("Search");

    /**
     * init GUI
     * @param user_id uses to refresh window
     */
    public LibrarianDocumentGUI(int user_id) {
        JFrame menuWindow = new JFrame();
        if(Database.isLibrarianPriv1(user_id))
            menuWindow.setBounds(100, 100, 300, 310);
        if(Database.isLibrarianPriv2(user_id))
            menuWindow.setBounds(100, 100, 300, 400);
        if(Database.isLibrarianPriv3(user_id) | Database.isAdmin(user_id))
            menuWindow.setBounds(100, 100, 300, 443);
        menuWindow.setLocationRelativeTo(null);
        menuWindow.setResizable(false);
        menuWindow.setTitle("Librarian");
        menuWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                menuWindow.dispose();
            }
        });
        Container containerM = menuWindow.getContentPane();
        containerM.setLayout(new FlowLayout());
        ArrayList<Document> documents = Database.getAllDocuments();
        Object[][] books = new Object[documents.size()][];

        //parse in table
        for (int i = 0; i < documents.size(); i++) {
            books[i] = new Object[5];
            books[i][0] = documents.get(i).name;
            books[i][1] = documents.get(i).authors;
            books[i][2] = documents.get(i).location;
            books[i][3] = documents.get(i).price;
            books[i][4] = documents.get(i).type;
        }

        String[] columnNames = {"Name", "Authors", "Location", "Price", "Type"};
        String[] selecting = {"All","Name", "Authors", "Location", "Price", "Type"};

        //init search and elements in gui box
        selectForSearch = new JComboBox(selecting);
        selectForSearch.setSelectedIndex(0);
        selectForSearch.setPreferredSize(new Dimension(130, 20));
        jLabel1.setPreferredSize(new Dimension(130, 20));
        JPanel panel1 = new JPanel(new FlowLayout());
        panel1.add(jLabel1);
        panel1.add(selectForSearch);
        panel1.setPreferredSize(new Dimension(290, 25));
        containerM.add(panel1);
        DefaultTableModel model = new DefaultTableModel(books, columnNames);
        table = new JTable(model);
        listScroller = new JScrollPane(table);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        rowSorter = new TableRowSorter<>(table.getModel());
        table.setRowSorter(rowSorter);
        jtfFilter.setPreferredSize(new Dimension(220, 20));
        jLabel.setPreferredSize(new Dimension(50, 20));
        JPanel panel = new JPanel(new FlowLayout());
        panel.add(jLabel);
        panel.add(jtfFilter);
        panel.setPreferredSize(new Dimension(290, 25));
        containerM.add(panel);
        listScroller.setPreferredSize(new Dimension(290, 118));
        containerM.add(listScroller);

        EditBook.setPreferredSize(new Dimension(290, 40));
        containerM.add(EditBook);

        /**
         * edit book button
         */
        EditBook.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = table.getSelectedRow();
                if (index != -1) {
                    menuWindow.dispose();
                    CurrentSession.editDocument = documents.get(index);

                    //select correct document's type
                    if (documents.get(index).type == DocumentType.book) {
                        AddBookGUI book = new AddBookGUI(user_id);
                    } else if (documents.get(index).type == DocumentType.journal) {
                        AddJournalGUI journal = new AddJournalGUI(user_id);
                    } else if (documents.get(index).type == DocumentType.av_material) {
                        AddAVmaterialGUI AVmaterial = new AddAVmaterialGUI(user_id);
                    }
                } else {
                    String message = "No row is selected\n" + "You need to select one to edit";
                    JOptionPane.showMessageDialog(null, message, "ERROR", JOptionPane.PLAIN_MESSAGE);
                }
            }
        });

        //has user enough prevs
        if(Database.isLibrarianPriv2(user_id) || Database.isLibrarianPriv3(user_id) || Database.isAdmin(user_id)) {
            AddBook.setPreferredSize(new Dimension(290, 40));
            containerM.add(AddBook);

            //close window
            AddBook.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(Database.isLibrarianPriv2(CurrentSession.user.id)) {
                        menuWindow.dispose();
                        AddDocumentGUI books = new AddDocumentGUI(user_id);
                    }
                    else{
                        System.out.println("Error in AddBookGUI: user doesn't have access to add new document");
                    }
                }
            });
            Create.setPreferredSize(new Dimension(290, 40));
            containerM.add(Create);

            //creatings copies
            Create.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int index = table.getSelectedRow();
                    if(index != -1){
                        menuWindow.dispose();
                        Document document = documents.get(index);
                        document.location = "its not important";
                        document.addCopies(1, CurrentSession.user.id);
                        String message = "You created one copy of document";
                        JOptionPane.showMessageDialog(null, message, "New Window", JOptionPane.PLAIN_MESSAGE);
                    }
                    else{
                        String message = "Select a book!\n";
                        JOptionPane.showMessageDialog(null, message, "ERROR", JOptionPane.PLAIN_MESSAGE);
                    }
                    LibrarianDocumentGUI restart = new LibrarianDocumentGUI(user_id);
                }
            });

            //send outstanding request
            outstandingRequest.setPreferredSize(new Dimension(290, 40));
            containerM.add(outstandingRequest);
            outstandingRequest.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int index = table.getSelectedRow();
                    if(index != -1){
                        Librarian librarian = (Librarian)CurrentSession.user;
                        Database.sendOutstandingRequest(documents.get(index), librarian);
                        String message = "Done!\n";
                        JOptionPane.showMessageDialog(null, message, "SUCCESS", JOptionPane.PLAIN_MESSAGE);

                    }
                }
            });
        }

        //delete book
        if(Database.isLibrarianPriv3(user_id) || Database.isAdmin(user_id)){
            DeleteBook.setPreferredSize(new Dimension(290, 40));
            containerM.add(DeleteBook);
            DeleteBook.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int index = table.getSelectedRow();
                    if (index != -1) {
                        menuWindow.dispose();
                        documents.get(index).DeleteFromDB(CurrentSession.user.id);
                        String message = "Book succesfully deleted!";
                        JOptionPane.showMessageDialog(null, message, "New Window", JOptionPane.PLAIN_MESSAGE);
                    } else {
                        String message = "Select a book!\n";
                        JOptionPane.showMessageDialog(null, message, "ERROR", JOptionPane.PLAIN_MESSAGE);
                    }
                    LibrarianDocumentGUI restart = new LibrarianDocumentGUI(user_id);
                }
            });
        }

        //init filter
        jtfFilter.getDocument().addDocumentListener(new DocumentListener(){
            @Override
            public void insertUpdate(DocumentEvent e) {
                String text = jtfFilter.getText();
                if (text.trim().length() == 0) {
                    rowSorter.setRowFilter(null);
                } else {
                    int no = selectForSearch.getSelectedIndex();
                    if(no == 0)
                        rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                    else
                        rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, no-1));
                }
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                String text = jtfFilter.getText();
                if (text.trim().length() == 0) {
                    rowSorter.setRowFilter(null);
                } else {
                    int no = selectForSearch.getSelectedIndex();
                    if(no == 0)
                        rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                    else
                        rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, no-1));
                }
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });

        //get document queue
        Queue.setPreferredSize(new Dimension(290, 40));
        containerM.add(Queue);
        Queue.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                menuWindow.dispose();
                int index = table.getSelectedRow();
                CurrentSession.editDocument = documents.get(index);
                DocumentQueueGUI queue = new DocumentQueueGUI();
            }
        });

        //close window
        menuWindow.setVisible(true);
    }
}