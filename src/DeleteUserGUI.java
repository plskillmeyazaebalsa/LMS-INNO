import javax.swing.*;
import javax.xml.crypto.Data;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * used to delete users
 */
public class DeleteUserGUI extends JFrame {
    private JButton delete = new JButton("Delete");

    /**
     * gui constructor
     */
    public DeleteUserGUI() {
        try {
            //init window and set size
            JFrame tasks = new JFrame();
            tasks.setBounds(100, 100, 500, 400);
            tasks.setLocationRelativeTo(null);
            tasks.setResizable(false);
            tasks.setTitle("Delete user");
            tasks.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            Container containerTB = tasks.getContentPane();
            containerTB.setLayout(new BorderLayout());

            //find all user info
            String[] columnNames = {"Name", "Login", "Debt", "Type", "Address"};
            ArrayList<ArrayList<String>> users = new ArrayList<>();

            try{
                Statement statement = Database.connection.createStatement();
                ResultSet rs = statement.executeQuery("select * from users order by id");

                int count = 0;
                while (rs.next()){
                    users.add(new ArrayList<String>());
                    users.get(count).add(rs.getString("name"));
                    users.get(count).add(rs.getString("phoneNumber"));
                    users.get(count).add(rs.getString("debt"));

                    String userType = "";
                    if(rs.getBoolean("isLibrarian")){
                        userType = "Librarian";
                    }else if(rs.getBoolean("isFacultyMember")){
                        userType = "Faculty Member";
                    }else{
                        userType = "Patron";
                    }

                    users.get(count).add(userType);
                    users.get(count).add(rs.getString("address"));

                    count++;
                }
            }catch (Exception e){
                System.out.println("Error in deleteuserGUI " + e.toString());
            }

            //set user info into table
            Object[][] usersAr = new Object[users.size()][5];
            for (int i = 0; i < users.size(); i++) {
                for (int j = 0; j < 5; j++) {
                    usersAr[i][j] = users.get(i).get(j);
                }
            }

            //set table size
            JTable table = new JTable(usersAr, columnNames);
            JScrollPane listScroller = new JScrollPane(table);
            table.setFillsViewportHeight(true);
            listScroller.setPreferredSize(new Dimension(100,100));
            containerTB.add(listScroller, BorderLayout.CENTER);

            //delete button, remove user from system
            delete.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int index = table.getSelectedRow();
                    if(index != -1){
                        Patron patron = Database.getPatronByNumber(users.get(index).get(1));
                        patron.DeleteUserDB(CurrentSession.user.id);
                    }
                    else{
                        String message = "Select a student!\n";
                        JOptionPane.showMessageDialog(null, message, "ERROR", JOptionPane.PLAIN_MESSAGE);
                    }
                }
            });

            //set delete button size
            delete.setPreferredSize(new Dimension(250, 40));
            containerTB.add(delete, BorderLayout.SOUTH);
            tasks.setVisible(true);
        }
        catch (Exception e){
            System.out.println("Error in Tasks " + e.toString());
        }
    }

}