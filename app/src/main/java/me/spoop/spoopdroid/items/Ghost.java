package me.spoop.spoopdroid.items;

import android.location.Location;

/**
 * A spoopy little ghost.
 * @author James Chambers
 */
public class Ghost {

    private String id;
    private String name;
    private String user;
    private String drawable;
    private GhostLocation location;

    private static class GhostLocation {
        private String longitude;
        private String latitude;

        public String getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = Double.toString(latitude);
        }

        public String getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = Double.toString(longitude);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getDrawable() {
        return drawable;
    }

    public void setDrawable(String drawable) {
        this.drawable = drawable;
    }

    public Location getLocation() {
        Location result = new Location("ghostData");
        result.setLongitude(Double.parseDouble(location.getLongitude()));
        result.setLatitude(Double.parseDouble(location.getLatitude()));
        return result;
    }

    public void setLocation(Location location) {
        this.location = new GhostLocation();
        this.location.setLatitude(location.getLatitude());
        this.location.setLongitude(location.getLongitude());
    }

}
