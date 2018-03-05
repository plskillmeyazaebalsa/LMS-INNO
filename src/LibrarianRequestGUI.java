import java.awt.*;
import java.awt.event.*;
import javax.print.DocFlavor;
import javax.swing.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;

public class LibrarianRequestGUI extends JFrame{
    private JButton SendRequest = new JButton("Send request");


    public LibrarianRequestGUI() {
        try {
            JFrame takeBook = new JFrame();
            takeBook.setBounds(100, 100, 250, 200);
            takeBook.setLocationRelativeTo(null);
            takeBook.setResizable(false);
            takeBook.setTitle("Send request");
            takeBook.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            Container containerTB = takeBook.getContentPane();
            containerTB.setLayout(new BorderLayout());

            Object[][] books = {{"Lol,kek,cheburek", new Integer(5)}};
            String[] columnNames = {"User", "Amount"};

            JTable table = new JTable(books, columnNames);
            JScrollPane listScroller = new JScrollPane(table);
            table.setFillsViewportHeight(true);
            listScroller.setPreferredSize(new Dimension(100,100));
            containerTB.add(listScroller, BorderLayout.CENTER);

            SendRequest.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int index = table.getSelectedRow();
                    if(index != -1){
                        takeBook.setVisible(false);
                        String message = "You're bitch";
                        JOptionPane.showMessageDialog(null, message, "New Window", JOptionPane.PLAIN_MESSAGE);
                    }
                    else{
                        String message = "Select a book!\n";
                        JOptionPane.showMessageDialog(null, message, "ERROR", JOptionPane.PLAIN_MESSAGE);
                    }
                }
            });
            SendRequest.setPreferredSize(new Dimension(250, 40));
            containerTB.add(SendRequest, BorderLayout.SOUTH);
            takeBook.setVisible(true);
        }
        catch (Exception e){
            System.out.println("Error in Send Request " + e.toString());
        }
    }

}