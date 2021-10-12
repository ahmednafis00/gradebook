public class GradingComponent {
    private String mName;
    private int mQuantity;
    private int mWeight;
    private String[] mItems;
    private int[] mGrades;

    public GradingComponent(String name, int quantity, int weight) {
        mName = name;
        mQuantity = quantity;
        mWeight = weight;
        mItems = new String[mQuantity];
        mGrades = new int[mQuantity];

        for (int i = 0; i < mQuantity; ++i) {
            mItems[i] = mName + " " + i;
        }
    }

    public void addGrades(int position, int grade) {
        mGrades[position] = grade;
    }

    public String getName() {
        return mName;
    }

    public int getQuantity() {
        return mQuantity;
    }

    public int getWeight() {
        return mWeight;
    }
}
