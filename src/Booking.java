import java.sql.*;
import java.util.Calendar;
import java.util.Date;

public class Booking {

    Statement statement;
//    String userName = "root";
//    String password = "enaca2225";
//    String connectionUrl = "jdbc:mysql://localhost:3306/project?useSSL=false";


//    public static void main(String[] args) throws SQLException, ClassNotFoundException {
//        Booking booking = new Booking();
//        Document document = new Book();
//        document.id = 19;
//        document.localId = 1;
//        User user = new Patron();
//        user.id = 5;
//        //booking.checkOut(document, user);
//        //booking.renewBook(document);
//        //booking.returnBook(document, user);
//    }
    public static long setDate = 1520197200000L;
    public static boolean useCustomDate = false;


    public Booking() throws ClassNotFoundException, SQLException {
//
//        Class.forName("com.mysql.jdbc.Driver");
//        Connection connection = DriverManager.getConnection(connectionUrl, userName, password);
//        statement = connection.createStatement();

        Database database = new Database();
        statement = database.connection.createStatement();
    }

    public int checkOut(Document document, User user) {
        try {

            if (takeCopy(document, user) && document.isCanBeTaken()) {
                //Crete current date
                java.util.Date date = new java.util.Date();
                if(useCustomDate)
                    date.setTime(setDate);
                java.sql.Timestamp timestamp = new java.sql.Timestamp(date.getTime());

                //Crete date of returning
                java.util.Date returnDay = new java.util.Date();
                if(useCustomDate)
                    returnDay.setTime(setDate);
                Calendar day = Calendar.getInstance();
                day.setTime(returnDay);
                day.add(Calendar.DATE, bookingTerm(document,user));
                returnDay = day.getTime();
                java.sql.Timestamp timestamp1 = new java.sql.Timestamp(returnDay.getTime());

                //Crete line in Booking
                statement.executeUpdate("INSERT into booking(document_id, user_id, time, returnTime) values(" + Integer.toString(document.id) +", " + Integer.toString(user.id) +", '"+
                   timestamp + "', '" + timestamp1 +"');" );

                //Change status of document
                PreparedStatement preparedStatement = Database.connection.prepareStatement("UPDATE documents SET isActive = ? WHERE id = ?");
                preparedStatement.setBoolean(1, false);
                preparedStatement.setInt(2, document.id);

                //Get line from Documents
                String type = getType(document);

                //Change number of available documents
                changeNumber(false, type, document);

                //Term of booking
                int term = bookingTerm(document, user);

                return term;
            }
        } catch (Exception e) {
            System.out.println("Error in checkOut booking " + e.toString());
        }
        return -1;
    }

    public void returnBook(Document document, User user) throws SQLException {
        //Get line from Booking
        statement.executeQuery("SELECT*FROM booking WHERE document_id = '" + document.id + "'");

        //Current date
        java.util.Date date = new java.util.Date();

        //Get date of booking
        java.util.Date bookingDate = new java.util.Date();
        ResultSet rec = statement.getResultSet();
        if (rec.next()) {
            bookingDate = rec.getDate("time");
        }

        //Count the term of booking
        int term = bookingTerm(document, user);

        //Add term to date of booking
        Calendar c = Calendar.getInstance();
        c.setTime(bookingDate);
        c.add(Calendar.DATE, term);
        bookingDate = c.getTime();

        //Check overdue
        if (!date.before(bookingDate)) {
            int overdue = countOverdueCost(document);
            statement.executeUpdate("UPDATE users set debt = '" + overdue + "' WHERE id = '" + user.id + "'");
        }

        //Delete record from Booking
        statement.executeUpdate("DELETE FROM booking WHERE document_id = '" + document.id + "' AND user_id = '" + user.id + "'");

        //Delete request if it is a request book
        if(Database.isRequestDocument(document)){
            statement.executeUpdate("DELETE FROM request WHERE id_document = '" + document.id + "' AND id_user = '" + user.id + "'");
        }

        //Change status of document
        PreparedStatement preparedStatement = Database.connection.prepareStatement("UPDATE documents SET isActive = ? WHERE id = ?");
        preparedStatement.setBoolean(1, true);
        preparedStatement.setInt(2, document.id);

        //Change number of available documents
        changeNumber(true, getType(document), document);

    }

    public void renewBook(Document document, User user) throws SQLException {
        //Get line from Booking
        statement.executeQuery("SELECT*FROM booking WHERE document_id = '" + document.id + "'");

        //Check can we renew book
        boolean isRenew = false;
        ResultSet rec = statement.getResultSet();
        if (rec.next()) {
            isRenew = rec.getBoolean("is_renew");
        }

        //Current date
        java.util.Date date = new java.util.Date();
        java.sql.Timestamp timestamp = new java.sql.Timestamp(date.getTime());

        //Crete new date of returning
        java.util.Date returnDay = new java.util.Date();
        Calendar day = Calendar.getInstance();
        day.setTime(returnDay);
        day.add(Calendar.DATE, bookingTerm(document,user));
        returnDay = day.getTime();
        java.sql.Timestamp timestamp1 = new java.sql.Timestamp(returnDay.getTime());

        //Renew document
        if (!isRenew) {
            statement.executeUpdate("UPDATE booking set time = '" + timestamp + "', is_renew = '" + 1 + "', returnTime = '"+ timestamp + "' WHERE document_id = '" + document.id + "'");
        }

    }

    private String getType(Document document) throws SQLException {
        statement.executeQuery("SELECT*FROM documents WHERE id = '" + document.id + "'");
        ResultSet line = statement.getResultSet();
        String type = "";
        if (line.next()) {
            type = line.getString("type");
        }
        return type;
    }

    private int countOverdueCost(Document document) throws SQLException {
        int days = countOverdue(document);
        int overdue = days * 100;

        ResultSet line;

        String type = getType(document);
        int id = Database.getCorrectIdInLocalDatabase(document.id);

        if (type.equals("books")) {
            statement.executeQuery("SELECT*FROM books WHERE id = '" + id + "'");
        }
        if (type.equals("journals")) {
            statement.executeQuery("SELECT*FROM journals WHERE id = '" + id + "'");
        }
        if (type.equals("av_materials")) {
            statement.executeQuery("SELECT*FROM av_materials WHERE id = '" + id + "'");
        }
        line = statement.getResultSet();
        int cost = 0;
        if (line.next()) {
            cost = line.getInt("cost");
        }

        if (overdue > cost) {
            overdue = cost;
        }

        return overdue;
    }

    public int countOverdue(Document document){
        //Get line from Booking
        try {
            statement.executeQuery("SELECT*FROM booking WHERE document_id = '" + document.id + "'");

        //Current date
        java.util.Date date = new java.util.Date();

        //Get date of booking
        java.util.Date bookingDate = new java.util.Date();
        ResultSet rec = statement.getResultSet();


        if (rec.next()) {
            bookingDate = rec.getDate("time");
        }

        int days = (int) (bookingDate.getTime() - date.getTime() / (1000 * 60 * 60 * 24));

        return days;

        } catch (SQLException e) {
            System.out.println("Error in countOverdue: " + e.toString());
        }

        return -1;
    }

    private void changeNumber(boolean add, String type, Document document) throws SQLException {
        int one;
        if (add) {
            one = 1;
        } else {
            one = -1;
        }
        //Change number of available documents
        if (type.equals("books")) {//If it's book
            int counter = Database.getAmountOfCurrentBook((Book)document)+ one;
            statement.executeUpdate("UPDATE books set number = '" + counter + "' WHERE id ='" + document.localId + "'");
        }
        if (type.equals("journals")) {//If it's journal
            int counter = Database.getAmountOfCurrentJournal((Journal)document)+ one;
            statement.executeUpdate("UPDATE journals set number = '" + counter + "' WHERE id ='" + document.localId + "'");
        }
        if (type.equals("av_materials")) {//If it's av material
            int counter = Database.getAmountOfCurrentAvmaterial((AVmaterial)document)+ one;
            statement.executeUpdate("UPDATE av_materials set number = '" + counter + "' WHERE id ='" + document.localId + "'");
        }
    }

    private int bookingTerm(Document document, User user) throws SQLException {
        ResultSet rec;
        //Get type of document
        String type = getType(document);

        //Is user faculty member
        statement.executeQuery("SELECT*FROM users WHERE id = '" + user.id + "'");
        rec = statement.getResultSet();
        boolean isFaculty = false;
        if (rec.next()) {
            isFaculty = rec.getBoolean("isFacultyMember");
        }

        int term;
        if (type.equals("books")) {
            int id_books = Database.getCorrectIdInLocalDatabase(document.id);
            statement.executeQuery("SELECT*FROM books WHERE id = '" + id_books + "'");
            rec = statement.getResultSet();
            boolean isBestSeller = false;
            if (rec.next()) {
                isBestSeller = rec.getBoolean("isBestSeller");
            }
            if (isFaculty) {
                term = 28;
            } else {
                if (isBestSeller) {
                    term = 14;
                } else {
                    term = 21;
                }
            }
        } else {
            term = 14;
        }
        return term;
    }

    private boolean takeCopy(Document document, User user) throws SQLException {
        statement.executeQuery("SELECT*FROM booking WHERE user_id = '" + user.id + "'");
        ResultSet rec = statement.getResultSet();
        int id = 0;
        int localId = 0;

        while (rec.next()) {
            id = rec.getInt("document_id");
            localId = Database.getCorrectIdInLocalDatabase(id);
            if (localId == document.localId) {
                return false;
            }
        }
        return true;

    }
}