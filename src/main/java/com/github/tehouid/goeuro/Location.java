package com.github.tehouid.goeuro;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * The class that represents the location returned by goeuro API which is later written to CSV file
 */
@JsonPropertyOrder({"id", "name", "type", "latitude", "longitude"})
public class Location {
    /**
     * The id of the location
     */
    private final Integer id;
    /**
     * The name of the location
     */
    private final String name;
    /**
     * The type of the location
     */
    private final String type;
    /**
     * The latitude of the location
     */
    private final Double latitude;
    /**
     * The longtitud of the location
     */
    private final Double longitude;

    /**
     * Internal builder class
     */
    public static class Builder {
        private final Integer id;

        private String name;
        private String type;
        private Double latitude;
        private Double longitude;

        /**
         * Creates a builder object with given id
         * @param idValue
         */
        public Builder(Integer idValue) {
            id = idValue;
        }

        public Builder name(String val) {
            name = val;
            return this;
        }

        public Builder type(String val) {
            type = val;
            return this;
        }

        public Builder latitude(Double val) {
            latitude = val;
            return this;
        }

        public Builder longitude(Double val) {
            longitude = val;
            return this;
        }

        /**
         * Method to generate the object
         * @return  <code>Location</code> object
         */
        public Location build() {
            return new Location(this);
        }

    }

    /**
     * Private constructor used by <code>build</code> method of internal
     * <code>Builder</code>
     *
     * @param   builder The <code>Builder</code> object from which all the fields of
     *          this object are set
     */
    private Location(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.type = builder.type;
        this.latitude = builder.latitude;
        this.longitude = builder.longitude;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    /**
     * Returns the string representation of this Location in the following form:
     * <pre>
     * {@code
     * Location: {
     *     _id: ID
     *     name: NAME
     *     type: TYPE
     *     latitude: LATITUDE
     *     longitude: LONGITUDE
     * }
     * }
     * </pre>
     * @return the string representation of Location
     */
    @Override
    public String toString() {
        return "Location: " + "{\n_id: " + this.id + "\nname: " + this.name + "\ntype: " + this.type +
                "\nlatitude: " + this.latitude + "\nlongitude: " + this.longitude + "\n}";
    }
}
