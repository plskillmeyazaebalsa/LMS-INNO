public class Patron extends User {
    public boolean isFacultyMember;
    public int debt;

    public Patron(String name, String password, String phoneNumber, String address, boolean isFacultyMember, int debt){
        this.name = name;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.debt = debt;
        this.isFacultyMember = isFacultyMember;
    }
    public Patron(){

    }
}
