package com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel;

public class JobViewConfiguration {

    private boolean showAvatars;

    private boolean showCulpritName;

    public boolean getShowAvatars() {
        return showAvatars;
    }

    public void setShowAvatars(boolean showAvatars) {
        this.showAvatars = showAvatars;
    }

    public boolean getShowCulpritName() {
        return showCulpritName;
    }

    public void setShowCulpritName(boolean showCulpritName) {
        this.showCulpritName = showCulpritName;
    }

}
