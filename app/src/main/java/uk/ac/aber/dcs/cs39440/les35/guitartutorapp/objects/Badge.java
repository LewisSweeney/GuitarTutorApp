package uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects;

/**
 * Class for storing badge data for access by the Badges fragment
 */
public class Badge {

    private String name;
    private String description;
    private int achievementLimit;
    private StatType type;

    public Badge(String name, String description, int achievementLimit, StatType type){
        this.name = name;
        this.description = description;
        this.achievementLimit = achievementLimit;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getAchievementLimit() {
        return achievementLimit;
    }

    public void setAchievementLimit(int achievementLimit) {
        this.achievementLimit = achievementLimit;
    }

    public StatType getType() {
        return type;
    }

    public void setType(StatType type) {
        this.type = type;
    }
}
