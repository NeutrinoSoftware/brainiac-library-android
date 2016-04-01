package net.neutrinosoft.brainiac;

import java.util.List;

public class IndicatorsState {
    private UserActivity activities;
    private List<String> colors;

    public IndicatorsState(UserActivity activities, List<String> colors) {
        this.activities = activities;
        this.colors = colors;
    }

    public UserActivity getActivities() {
        return activities;
    }

    public void setActivities(UserActivity activities) {
        this.activities = activities;
    }

    public List<String> getColors() {
        return colors;
    }

    public void setColors(List<String> colors) {
        this.colors = colors;
    }
}
