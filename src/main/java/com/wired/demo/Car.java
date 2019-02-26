package com.wired.demo;

import java.io.Serializable;
import java.util.*;

public class Car implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     *
     */

    private Long time;
    private Date currentTime;

    private String carCode;

    private Map<String, Serializable> values;


    /**
     * <p>Default Constructor</p>
     */
    public Car() {
        this.values = new HashMap<>();
        this.currentTime = Constants.FIRST_JANUARY_1970;
        time = System.currentTimeMillis();
    }

    /**
     * <p>Default Constructor</p>
     */
    public Car(String carCode, Long time) {
        this.values = new HashMap<>();
        this.currentTime = new Date(time);
        this.time = time;
        setCarCode(carCode);
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Date getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(Date currentTime) {
        this.currentTime = currentTime;
    }


    public String getCarCode() {
        return carCode;
    }

    public void setCarCode(String carCode) {
        this.carCode = carCode;
    }

    public Map<String, Serializable> getValues() {
        return values;
    }

    public Car setValues(String carPlate, String carStatus, String carIsMoving, String carOwner, String carNumber, String functionCode) {
        this.values = new HashMap<>();
        values.put(Constants.CAR_PLATE, carPlate);
        values.put(Constants.CAR_STATUS, carStatus);
        values.put(Constants.CAR_IS_MOVING, carIsMoving);
        values.put(Constants.CAR_OWNER, carOwner);
        values.put(Constants.CAR_NUMBER, carNumber);
        values.put(Constants.FUNCTION_CODE, functionCode);
        return this;
    }

    /*
     * CAR STATUS
     *  "0" - online
     *  "1" - offline
     *  "2" - out of service
     */


    public String getCarStatus() {
        return (String) values.get(Constants.CAR_STATUS);
    }

    public void setCarStatus(String status) {
        values.put(Constants.CAR_STATUS, status);
    }

    /*
     * CAR IS MOVING
     *  "0" - yes
     *  "1" - no
     */

    public String getCarIsMoving() {
        return (String) values.get(Constants.CAR_IS_MOVING);
    }

    public void setCarIsMoving(String moving) {
        values.put(Constants.CAR_IS_MOVING, moving);
    }

    /*
     * CAR OWNER
     */
    public String getCarOwner() {
        return (String) values.get(Constants.CAR_OWNER);
    }

    public void setCarOwner(String owner) {
        values.put(Constants.CAR_OWNER, owner);
    }

    /*
     * CAR NUMBER
     */
    public String getCarNumber() {
        return (String) values.get(Constants.CAR_NUMBER);
    }

    public void setCarNumber(String number) {
        values.put(Constants.CAR_NUMBER, number);
    }

    /*
     * CAR PLATE
     */
    public String getCarPlate() {
        return (String) values.get(Constants.CAR_PLATE);
    }

    public void setCarPlate(String plate) {
        values.put(Constants.CAR_PLATE, plate);
    }

    public void setResetValues(Map<String, Serializable> values) {
        this.values = values;
    }

    public Car mergeValues(Car dh) {
        values.putAll(dh.getValues());
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Car car = (Car) o;
        return Objects.equals(time, car.time) &&
                Objects.equals(currentTime, car.currentTime) &&
                Objects.equals(carCode, car.carCode) &&
                Objects.equals(values, car.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, currentTime, carCode, values);
    }

    public Map<String, Serializable> difference(Car other) {
        Map<String, Serializable> diff = new HashMap<>();
        Set<Map.Entry<String, Serializable>> newVal = new HashSet<Map.Entry<String, Serializable>>((values.entrySet()));
        if (other != null) {
            Set<Map.Entry<String, Serializable>> oldVal = new HashSet<Map.Entry<String, Serializable>>((other.values.entrySet()));
            newVal.removeAll(oldVal);
            for (Map.Entry<String, Serializable> entry : newVal) {
                diff.put(entry.getKey(), entry.getValue());
            }
        }
        return diff;
    }


    @Override
    public String toString() {
        return "Car{" +
                "time=" + time +
                ", currentTime=" + currentTime +
                ", carCode='" + carCode + '\'' +
                ", values=" + values +
                '}';
    }
}
