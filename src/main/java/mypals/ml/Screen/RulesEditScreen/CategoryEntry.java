package mypals.ml.Screen.RulesEditScreen;

public class CategoryEntry {
    public String name;
    public Boolean selected = false;

    public CategoryEntry(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setSelected(boolean bl) {
        selected = bl;
    }
}
