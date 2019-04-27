package uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects;

/**
 * Item used to display items within the LearnFragment
 */
public class LearnItem {
    private String learnItemName;
    private String learnItemDescription;

    public LearnItem(String learnItemName, String learnItemDescription){
        this.learnItemDescription = learnItemDescription;
        this.learnItemName = learnItemName;
    }

    public String getLearnItemName() {
        return learnItemName;
    }

    public void setLearnItemName(String learnItemName) {
        this.learnItemName = learnItemName;
    }

    public String getLearnItemDescription() {
        return learnItemDescription;
    }

    public void setLearnItemDescription(String learnItemDescription) {
        this.learnItemDescription = learnItemDescription;
    }




}
