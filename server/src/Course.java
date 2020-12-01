import java.util.Vector;

public class Course {
    private String mName;
    private String mInstitution;
    private int mCredits;
    private Vector<TcpUser> mStudents;
    private Vector<GradingComponent> mComponents;

    public Course(String name, String institution, int credits) {
        mName = name;
        mInstitution = institution;
        mCredits = credits;
        mStudents = new Vector<>();
        mComponents = new Vector<>();
    }

    public void setName(String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public String getInstitution() {
        return mInstitution;
    }

    public void setInstitution(String institution) {
        mInstitution = institution;
    }

    public void setCredits(int credits) {
        mCredits = credits;
    }

    public int getCredits() {
        return mCredits;
    }

    public void addStudent(TcpUser student) {
        mStudents.addElement(student);
    } 

    public Vector<TcpUser> getStudents() {
        return mStudents;
    }

    public void addComponent(String name, int quantity, int weight) {
        GradingComponent newComponent = new GradingComponent(name, quantity, weight);
        mComponents.addElement(newComponent);
    }

    public Vector<GradingComponent> getComponents() {
        return mComponents;
    }
}
