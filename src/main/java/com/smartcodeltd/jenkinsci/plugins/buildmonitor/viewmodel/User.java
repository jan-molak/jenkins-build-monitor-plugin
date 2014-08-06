package com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel;

public class User {

    private String name;

    private String avatar;

    public User(String name, String avatar) {
        this.name = name;
        this.avatar = avatar;
    }

    public User(String name) {
		this.name = name;
	}

	public String getName() {
        return name;
    }

    public String getAvatar() {
        return avatar;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof User)) {
            return false;
        }
        User other = (User) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

}
