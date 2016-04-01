package net.neutrinosoft.brainiac;

public class UserActivity {
    private ManagerActivityZone activityZone;
    private float percent;

    public UserActivity(ManagerActivityZone activityZone, float percent) {
        this.activityZone = activityZone;
        this.percent = percent;
    }

    public ManagerActivityZone getActivityZone() {
        return activityZone;
    }

    public void setActivityZone(ManagerActivityZone activityZone) {
        this.activityZone = activityZone;
    }

    public float getPercent() {
        return percent;
    }

    public void setPercent(float percent) {
        this.percent = percent;
    }
}
