package net.neutrinosoft.brainiac;

import net.neutrinosoft.brainiac.common.ManagerActivityZone;

public class UserActivity {
    private ManagerActivityZone activityZone;
    private double percent;

    public UserActivity(ManagerActivityZone activityZone, double percent) {
        this.activityZone = activityZone;
        this.percent = percent;
    }

    public ManagerActivityZone getActivityZone() {
        return activityZone;
    }

    public void setActivityZone(ManagerActivityZone activityZone) {
        this.activityZone = activityZone;
    }

    public double getPercent() {
        return percent;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }
}
