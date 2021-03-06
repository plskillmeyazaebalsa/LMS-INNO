import java.awt.*;
import java.awt.event.*;
import javax.print.DocFlavor;
import javax.swing.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;

/**
 * used to accept or decline some type of request(checkout, return document and register users)
 */
public class LibrarianTasksGUI extends JFrame{
    private JButton accept = new JButton("Accept");
    private JButton decline = new JButton("Decline");

    /**
     * window constructor
     */
    public LibrarianTasksGUI() {
        try {
            //init GUI
            JFrame tasks = new JFrame();
            tasks.setBounds(100, 100, 500, 400);
            tasks.setLocationRelativeTo(null);
            tasks.setResizable(false);
            tasks.setTitle("Tasks");
            tasks.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            Container containerTB = tasks.getContentPane();
            containerTB.setLayout(new BorderLayout());

            //Creating table and insert into libtask info
            String[] columnNames = {"Document", "Document's Position", "Username", "Login", "Type"};
            EventManager eventManager = new EventManager();
            ArrayList<LibTask> libTasks = eventManager.GetElements(false);
            Object[][] tasksA = new Object[libTasks.size()][];
            for (int i = 0; i < libTasks.size(); i++) {
                tasksA[i] = new Object[5];
                tasksA[i][0] = libTasks.get(i).document.name;
                tasksA[i][1] = libTasks.get(i).document.location;
                tasksA[i][2] = libTasks.get(i).user.name;
                tasksA[i][3] = libTasks.get(i).user.phoneNumber;
                tasksA[i][4] = libTasks.get(i).type;
            }

            //set table properties
            JTable table = new JTable(tasksA, columnNames);
            JScrollPane listScroller = new JScrollPane(table);
            table.setFillsViewportHeight(true);
            listScroller.setPreferredSize(new Dimension(500,280));
            containerTB.add(listScroller, BorderLayout.NORTH);

            //accept task and execute it (button)
            accept.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int index = table.getSelectedRow();
                    if(index != -1){
                        eventManager.ExecuteQuery(libTasks.get(index));
                    }
                    else{
                        String message = "Select a book!\n";
                        JOptionPane.showMessageDialog(null, message, "ERROR", JOptionPane.PLAIN_MESSAGE);
                    }
                }
            });

            //decline task and delete it from db (button)
            decline.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int index = table.getSelectedRow();
                    if(index != -1){
                        eventManager.DeleteQuery(libTasks.get(index));
                    }
                    else{
                        String message = "Select a book!\n";
                        JOptionPane.showMessageDialog(null, message, "ERROR", JOptionPane.PLAIN_MESSAGE);
                    }
                }
            });

            //set button size
            accept.setPreferredSize(new Dimension(500, 40));
            containerTB.add(accept, BorderLayout.CENTER);
            containerTB.add(accept);
            decline.setPreferredSize(new Dimension(500, 40));
            containerTB.add(decline, BorderLayout.SOUTH);
            tasks.setVisible(true);
        }
        catch (Exception e){
            System.out.println("Error in Tasks " + e.toString());
        }
    }

}