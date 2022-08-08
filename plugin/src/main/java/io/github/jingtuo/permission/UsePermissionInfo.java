package io.github.jingtuo.permission;

import java.util.Objects;

public class UsePermissionInfo {

    private String name;

    private String user;

    private String userType;

    public UsePermissionInfo() {
    }

    public UsePermissionInfo(String name, String user, String userType) {
        this.name = name;
        this.user = user;
        this.userType = userType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof UsePermissionInfo) {
            UsePermissionInfo other = (UsePermissionInfo) obj;
            return name.equals(other.getName()) && user.equals(other.getUser())
                    && userType.equals(other.getUserType());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, user, userType);
    }
}
